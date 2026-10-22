package com.khazoda.llc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.khazoda.llc.Critter.YELLOW_FLAT_FOOTED_FLY;

abstract class CritterParticle extends SingleQuadParticle {
  private static final Set<CritterParticle> AMBIENT = Collections.newSetFromMap(new WeakHashMap<>());
  private static final Set<CritterParticle> WALKING = Collections.newSetFromMap(new WeakHashMap<>());
  private static final Set<CritterParticle> BREAKING = Collections.newSetFromMap(new WeakHashMap<>());
  private static final Set<CritterParticle> MOB_DEATH = Collections.newSetFromMap(new WeakHashMap<>());
  private static final FacingCameraMode FLAT_GROUND_FACING = (rotation, camera, partialTick) -> rotation.rotationX(-Mth.HALF_PI);
  protected final Critter species;
  private final TextureAtlasSprite[] frames = new TextureAtlasSprite[4];
  private final float initialSize;
  private final double groundOffset = variation(0.010F, 0.014F);
  protected float animation;

  protected int fadeTicks;
  protected boolean stopped;

  protected double groundY;

  protected CritterParticle(ClientLevel level, double x, double y, double z, Critter species, SpawnCause cause) {
    super(level, x, y, z, sprite(species, 0));
    particles(cause).add(this);
    this.species = species;

    frames[0] = sprite;
    for (int i = 1; i < frames.length; i++) {
      frames[i] = sprite(species, i);
    }
    float size = switch (species) {
      case LADYBIRD -> 0.75F;
      case YELLOW_FLAT_FOOTED_FLY -> 0.65F;
      default -> 1F;
    };
    initialSize = quadSize = variation(0.05F, 0.1F) * size;
    animation = random.nextFloat() * frames.length;
    fadeTicks = ticks(10, 15);

    setPos(x, y + groundOffset, z);
    groundY = yo = this.y;
    roll = oRoll = random.nextFloat() * Mth.TWO_PI;
    alpha = 0;
    hasPhysics = false;
  }

  private static Set<CritterParticle> particles(SpawnCause cause) {
    return switch (cause) {
      case SpawnCause.AMBIENT -> AMBIENT;
      case SpawnCause.WALK -> WALKING;
      case SpawnCause.BREAK -> BREAKING;
      case SpawnCause.MOB_DEATH -> MOB_DEATH;
    };
  }

  static boolean hasSpace(ClientLevel level, SpawnCause cause) {
    Set<CritterParticle> critters = particles(cause);
    critters.removeIf(particle -> particle.level != level || !particle.isAlive());
    int limit = CritterConfig.CONFIG.get(switch (cause) {
      case SpawnCause.AMBIENT -> CritterConfig.MAX_AMBIENT_PARTICLES;
      case SpawnCause.WALK -> CritterConfig.MAX_WALKING_PARTICLES;
      case SpawnCause.BREAK -> CritterConfig.MAX_BREAKING_PARTICLES;
      case SpawnCause.MOB_DEATH -> CritterConfig.MAX_MOB_DEATH_PARTICLES;
    });
    return critters.size() < limit;
  }

  private static TextureAtlasSprite sprite(Critter species, int frame) {
    return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES)
        .getSprite(Identifier.fromNamespaceAndPath("llc", species.texture + "_" + frame));
  }

  protected final int lifespan(int normalTicks, int transitionTicks, SpawnCause cause) {
    int activeTicks = normalTicks - transitionTicks;
    return switch (cause) {
      case SpawnCause.WALK, SpawnCause.MOB_DEATH -> normalTicks;
      case SpawnCause.BREAK -> normalTicks * 2;
      case SpawnCause.AMBIENT -> transitionTicks + ticks(activeTicks * 20, activeTicks * 55);
    };
  }

  protected abstract void animate();

  @Override
  public final void tick() {
    xo = x;
    yo = y;
    zo = z;
    oRoll = roll;

    if (++age >= lifetime) {
      remove();
      return;
    }

    animate();
    int ticksLeft = lifetime - age;
    if (stopped && species != YELLOW_FLAT_FOOTED_FLY && y > groundY) {
      double descent = Math.max(0.025, (y - groundY) / ticksLeft);
      setPos(x, Math.max(groundY, y - descent), z);
    }

    float fade = Math.min(1, ticksLeft / (float) fadeTicks);
    // keep opacity on first stopped tick then fade evenly from current alpha
    if (!stopped) alpha = Math.min(age / 3F, fade);
    else if (ticksLeft < fadeTicks) alpha *= ticksLeft / (ticksLeft + 1F);
    quadSize = initialSize * (0.5F + 0.5F * fade);
  }

  protected final boolean moveAlongGround(float step, double height, boolean checkFlightPath) {
    double nextX = x - Mth.sin(roll) * step;
    double nextZ = z - Mth.cos(roll) * step;

    var ground = level.clip(new ClipContext(new Vec3(nextX, groundY + 0.25, nextZ), new Vec3(nextX, groundY - 0.35, nextZ), ClipContext.Block.COLLIDER, Fluid.NONE, Minecraft.getInstance().player));

    if (ground.getType() != HitResult.Type.BLOCK || ground.isInside() || ground.getDirection() != Direction.UP) {
      fadeOut();
      return false;
    }

    double nextGroundY = ground.getLocation().y + groundOffset;
    double nextY = nextGroundY + height;

    if (!level.getFluidState(BlockPos.containing(nextX, nextGroundY, nextZ)).isEmpty() || checkFlightPath && pathBlocked(nextX, nextY, nextZ, Fluid.NONE)) {
      fadeOut();
      return false;
    }

    groundY = nextGroundY;
    setPos(nextX, nextY, nextZ);
    return true;
  }

  protected final void moveThroughAir(float forward, float sideways, double climb) {
    double nextX = x - Mth.sin(roll) * forward - Mth.cos(roll) * sideways;
    double nextZ = z - Mth.cos(roll) * forward + Mth.sin(roll) * sideways;
    double nextY = y + climb;
    if (pathBlocked(nextX, nextY, nextZ, Fluid.ANY)) {
      fadeOut();
      return;
    }
    setPos(nextX, nextY, nextZ);
  }

  private boolean pathBlocked(double nextX, double nextY, double nextZ, Fluid fluids) {
    var hit = level.clip(
        new ClipContext(new Vec3(x, y, z),
            new Vec3(nextX, nextY, nextZ), ClipContext.Block.COLLIDER, fluids, Minecraft.getInstance().player));
    return hit.getType() != HitResult.Type.MISS;
  }

  private void fadeOut() {
    if (stopped) return;
    stopped = true;
    fadeTicks = Math.min(fadeTicks, lifetime - age);
    lifetime = age + fadeTicks;
  }

  @Override
  public FacingCameraMode getFacingCameraMode() {
    return FLAT_GROUND_FACING;
  }

  @Override
  protected void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partialTick) {
    super.extractRotatedQuad(state, rotation, x, y, z, partialTick);
    if (!species.airborne) return;

    rotation.rotateY(Mth.PI);
    int bottomColour = ARGB.colorFromFloat(alpha, rCol * 0.75F, gCol * 0.75F, bCol * 0.75F);
    // big ass call for reverse U so underside matches top of critter
    state.add(getLayer(), x, y, z, rotation.x, rotation.y, rotation.z, rotation.w, getQuadSize(partialTick), getU1(), getU0(), getV0(), getV1(), bottomColour, getLightCoords(partialTick));
  }

  @Override
  protected Layer getLayer() {
    return Layer.TRANSLUCENT;
  }

  protected final int ticks(int min, int max) {
    return random.nextIntBetweenInclusive(min, max);
  }

  protected final float variation(float min, float max) {
    return min + random.nextFloat() * (max - min);
  }

  protected final void setFrame(int frame) {
    setSprite(frames[frame % frames.length]);
  }
}

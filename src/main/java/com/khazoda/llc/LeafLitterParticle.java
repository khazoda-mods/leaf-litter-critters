package com.khazoda.llc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import static com.khazoda.llc.Critter.SHIELDBUG;
import static com.khazoda.llc.Critter.YELLOW_FLAT_FOOTED_FLY;

final class LeafLitterParticle extends CritterParticle {
  private final float speed;
  private final float frameDistance;
  private final int airDelay;
  private final int airDuration;
  private final int airPeriod;
  private final float airHeight;
  private float currentSpeed;
  private float targetSpeed;
  private int nextTurn;

  private LeafLitterParticle(ClientLevel level, double x, double y, double z, Critter species, SpawnCause cause) {
    super(level, x, y, z, species, cause);
    boolean shieldbug = species == SHIELDBUG;
    speed = switch (species) {
      case WORM -> variation(0.0015F, 0.0045F);
      case MILLIPEDE -> variation(0.0025F, 0.005F);
      default -> variation(0.015F, 0.05F);
    };
    currentSpeed = targetSpeed = speed;
    frameDistance = switch (species) {
      case WORM -> 0.005F;
      case MILLIPEDE -> 0.015F;
      default -> 0.05F;
    };
    airDelay = ticks(5, 10);
    airDuration = shieldbug ? ticks(20, 35) : ticks(5, 10);
    airPeriod = airDuration + (shieldbug ? ticks(5, 15) : ticks(5, 10));
    airHeight = shieldbug ? variation(0.3F, 0.55F) : variation(0.05F, 0.15F);
    nextTurn = ticks(5, 10);
    int usualLifetime = species.airborne ? airDelay + (shieldbug ? ticks(1, 2) : ticks(2, 5)) * airPeriod + fadeTicks : ticks(25, 45);
    lifetime = lifespan(usualLifetime, species.airborne ? airDelay + fadeTicks : 0, cause);
    setFrame(species.airborne ? 0 : (int) animation);
  }

  static CritterParticle create(ClientLevel level, BlockPos pos, double x, double z, SpawnCause cause) {
    Critter species = CritterPools.choose(level, pos);
    double y = pos.getY();
    return species == YELLOW_FLAT_FOOTED_FLY ? new YellowFlyParticle(level, x, y, z, cause) : new LeafLitterParticle(level, x, y, z, species, cause);
  }

  @Override
  protected void animate() {
    if (species.airborne) moveInArc();
    else crawl();
  }

  private void changePace() {
    if (age >= nextTurn) {
      if (species != SHIELDBUG || random.nextBoolean()) roll += variation(-0.5F, 0.5F);
      targetSpeed = random.nextFloat() < 0.15F ? 0 : speed * variation(0.5F, 1);
      nextTurn = age + ticks(5, 10);
    }
    currentSpeed = Mth.lerp(0.35F, currentSpeed, targetSpeed);
  }

  private void crawl() {
    if (!stopped) {
      changePace();
      if (moveAlongGround(currentSpeed, 0, false)) animation += currentSpeed / frameDistance;
    }
    setFrame((int) animation);
  }

  private void moveInArc() {
    boolean shieldbug = species == SHIELDBUG;
    int cycleTick = Math.floorMod(age - airDelay, airPeriod);
    // only start flying or jumping if there is time to land before fading out
    boolean inAir = age >= airDelay && cycleTick < airDuration && age + airDuration - 1 - cycleTick < lifetime - fadeTicks;
    float progress = cycleTick / (float) (airDuration - 1);
    if (!stopped) {
      if (shieldbug && !inAir) changePace();
      float step;
      if (shieldbug) step = inAir ? speed : currentSpeed;
      else step = inAir ? speed * 2 * (1 - progress) : 0;
      float angle = shieldbug ? Mth.PI * cycleTick / (airDuration - 1) : Mth.PI * (1 - (1 - progress) * (1 - progress));
      double height = inAir ? Math.max(0, airHeight * Mth.sin(angle)) : 0;
      if (moveAlongGround(step, height, shieldbug && inAir) && shieldbug && !inAir) {
        animation += step / frameDistance;
      }
    }
    if (shieldbug) {
      int frame;
      if (y > groundY + 0.001) frame = 2 + (int) (animation + age * 0.75F) % 2;
      else if (inAir && !stopped) frame = 1;
      else frame = (int) animation % 2;
      setFrame(frame);
    } else {
      int frame = 0;
      if (stopped && y > groundY) frame = 3;
      else if (!stopped && inAir) {
        if (cycleTick == 0) frame = 1;
        else if (cycleTick == airDuration - 1) frame = 3;
        else frame = 2;
      }
      setFrame(frame);
    }
  }
}

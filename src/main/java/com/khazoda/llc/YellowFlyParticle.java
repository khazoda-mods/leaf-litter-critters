package com.khazoda.llc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import static com.khazoda.llc.Critter.YELLOW_FLAT_FOOTED_FLY;

/**
 * created in LeafLitterParticle.create() for yellow flies, with separate movement for hovering and darting between spots
 */
final class YellowFlyParticle extends CritterParticle {
  private static final int DART_TICKS = 5;

  private double dartClimb;
  private int dartUntil = DART_TICKS + 1;
  private int nextDart;

  YellowFlyParticle(ClientLevel level, double x, double y, double z, SpawnCause cause) {
    super(level, x, y, z, YELLOW_FLAT_FOOTED_FLY, cause);
    nextDart = dartUntil + ticks(16, 24);
    dartClimb = variation(0.25F, 0.4F) / DART_TICKS;
    lifetime = lifespan(ticks(80, 110) + fadeTicks, fadeTicks, cause);
  }

  @Override
  protected void animate() {
    if (stopped) return;
    if (lifetime - age <= fadeTicks + 15) {
      moveThroughAir(0.06F, 0, 0.015);
      return;
    }

    if (age >= nextDart) {
      roll += (random.nextBoolean() ? 1 : -1) * variation(0.7F, 2.5F);
      dartClimb = (groundY + variation(0.25F, 0.6F) - y) / DART_TICKS;
      dartUntil = age + DART_TICKS;
      nextDart = dartUntil + ticks(15, 25);
    }
    if (age < dartUntil) {
      float progress = (age - dartUntil + DART_TICKS) / (float) DART_TICKS;
      float movement = (float) (DART_TICKS * (Mth.smoothstep(progress + 1F / DART_TICKS) - Mth.smoothstep(progress)));
      moveThroughAir((age > DART_TICKS ? 0.06F : 0) * movement, 0, dartClimb * movement);
    } else {
      hover();
    }
  }

  // round figure eight hover animation
  private void hover() {
    float step = Mth.TWO_PI / (nextDart - dartUntil);
    float phase = (age - dartUntil) * step;
    float sin = Mth.sin(phase);
    float nextSin = Mth.sin(phase + step);
    float divisor = 2 - sin * sin;
    float nextDivisor = 2 - nextSin * nextSin;
    float forward = (nextSin / nextDivisor - sin / divisor) * 0.02F;
    float sideways = (Mth.sin(2 * (phase + step)) / nextDivisor - Mth.sin(2 * phase) / divisor) * 0.014142F;
    moveThroughAir(forward, sideways, 0);
  }

  @Override
  protected void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partialTick) {
    if (this.y > groundY) setFrame(2 + (int) (animation + (age - 1 + partialTick) * 1.75F) % 2);
    super.extractRotatedQuad(state, rotation, x, y, z, partialTick);
  }
}

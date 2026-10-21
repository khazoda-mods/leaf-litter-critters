package com.khazoda.llc;

import com.khazoda.core.config.KhazConfig;
import com.khazoda.core.config.KhazConfig.Entry;
import net.minecraft.client.Minecraft;

public final class CritterConfig {
  static final Entry<Double> AMBIENT_SPAWN_RATE = KhazConfig.decimal(
          "ambient_spawn_rate", 0.5, 0, Double.MAX_VALUE,
          "0 is off, 0.5 is normal, 1 is double, be careful if setting high values for all of these settings.")
      .localOnly();

  static final Entry<Double> WALKING_SPAWN_RATE = KhazConfig.decimal(
          "walking_spawn_rate", 0.5, 0, Double.MAX_VALUE,
          "0 is off, 0.5 is normal, 1 is double")
      .localOnly();

  static final Entry<Double> BREAKING_SPAWN_RATE = KhazConfig.decimal(
          "breaking_spawn_rate", 0.5, 0, Double.MAX_VALUE,
          "0 is off, 0.5 is normal, 1 is double")
      .localOnly();

  static final Entry<Integer> MAX_AMBIENT_PARTICLES = KhazConfig.integer(
          "max_ambient_particles", 64, 0, Integer.MAX_VALUE,
          "maximum critters at once, 0 disables them")
      .localOnly();

  static final Entry<Integer> MAX_WALKING_PARTICLES = KhazConfig.integer(
          "max_walking_particles", 64, 0, Integer.MAX_VALUE,
          "maximum critters at once, 0 disables them")
      .localOnly();

  static final Entry<Integer> MAX_BREAKING_PARTICLES = KhazConfig.integer(
          "max_breaking_particles", 64, 0, Integer.MAX_VALUE,
          "maximum critters at once, 0 disables them")
      .localOnly();

  static final Entry<Boolean> YELLOW_FLIES_ALL_BIOMES = KhazConfig.bool(
          "yellow_flies_all_biomes", false,
          "allow yellow flies outside dappled forests (but not pale gardens)")
      .localOnly();

  public static final KhazConfig CONFIG = KhazConfig.of("Leaf Litter Critters", "llc",
      Minecraft.getInstance().gameDirectory.toPath().resolve("config"),
      AMBIENT_SPAWN_RATE, WALKING_SPAWN_RATE, BREAKING_SPAWN_RATE,
      MAX_AMBIENT_PARTICLES, MAX_WALKING_PARTICLES, MAX_BREAKING_PARTICLES, YELLOW_FLIES_ALL_BIOMES);

  private CritterConfig() {
  }

  static double spawnRate(SpawnCause cause) {
    return switch (cause) {
      case AMBIENT -> CONFIG.get(AMBIENT_SPAWN_RATE) / 128;
      case WALK -> CONFIG.get(WALKING_SPAWN_RATE) / 2;
      case BREAK -> CONFIG.get(BREAKING_SPAWN_RATE) * 1.5;
    };
  }
}

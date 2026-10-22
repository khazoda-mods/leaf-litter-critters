package com.khazoda.llc;

import com.khazoda.core.config.KhazConfig;
import com.khazoda.core.config.KhazConfig.Entry;
import net.minecraft.client.Minecraft;

public final class CritterConfig {
  static final Entry<Boolean> BITESIZE_BUGS = KhazConfig.bool(
          "bitesize_bugs", false,
          "use two-pixel textures for critters instead")
      .localOnly();

  static final Entry<Boolean> YELLOW_FLIES_ALL_BIOMES = KhazConfig.bool(
          "yellow_flies_all_biomes", false,
          "allow yellow flies outside dappled forests (but not pale gardens)")
      .localOnly();

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

  static final Entry<Double> MOB_DEATH_SPAWN_RATE = KhazConfig.decimal(
          "mob_death_spawn_rate", 0.5, 0, Double.MAX_VALUE,
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

  static final Entry<Integer> MAX_MOB_DEATH_PARTICLES = KhazConfig.integer(
          "max_mob_death_particles", 64, 0, Integer.MAX_VALUE,
          "maximum critters at once, 0 disables them")
      .localOnly();

  public static final KhazConfig CONFIG = KhazConfig.of("Leaf Litter Critters", "llc",
      Minecraft.getInstance().gameDirectory.toPath().resolve("config"),
      BITESIZE_BUGS, YELLOW_FLIES_ALL_BIOMES,
      AMBIENT_SPAWN_RATE, WALKING_SPAWN_RATE, BREAKING_SPAWN_RATE, MOB_DEATH_SPAWN_RATE,
      MAX_AMBIENT_PARTICLES, MAX_WALKING_PARTICLES, MAX_BREAKING_PARTICLES, MAX_MOB_DEATH_PARTICLES);

  private CritterConfig() {
  }

  static double spawnRate(SpawnCause cause) {
    return switch (cause) {
      case SpawnCause.AMBIENT -> CONFIG.get(AMBIENT_SPAWN_RATE) / 128;
      case SpawnCause.WALK -> CONFIG.get(WALKING_SPAWN_RATE) / 2;
      case SpawnCause.BREAK -> CONFIG.get(BREAKING_SPAWN_RATE) * 1.5;
      case SpawnCause.MOB_DEATH -> CONFIG.get(MOB_DEATH_SPAWN_RATE) * 1.5;
    };
  }
}

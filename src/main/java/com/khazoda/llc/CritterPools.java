package com.khazoda.llc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Map;

import static com.khazoda.llc.Critter.*;

/**
 * chooses which critters to spawn based on the biome
 */
final class CritterPools {
  private static final Critter[] DEFAULT = {
      WOODLOUSE, LADYBIRD, WORM, SHIELDBUG, FROGHOPPER, MILLIPEDE
  };
  // repeated entries give a species more spawn weight in the choose function
  private static final Map<ResourceKey<Biome>, Critter[]> BIOMES = Map.of(
      Biomes.FOREST, new Critter[]{
          WOODLOUSE, LADYBIRD, LADYBIRD, WORM,
          SHIELDBUG, FROGHOPPER, MILLIPEDE
      },
      Biomes.DARK_FOREST, new Critter[]{
          WOODLOUSE, WOODLOUSE, WORM, WORM,
          MILLIPEDE, MILLIPEDE, LADYBIRD, SHIELDBUG, FROGHOPPER
      },
      Biomes.WOODED_BADLANDS, new Critter[]{
          WOODLOUSE, WOODLOUSE, LADYBIRD, LADYBIRD, WORM,
          SHIELDBUG, SHIELDBUG, SHIELDBUG, SHIELDBUG,
          FROGHOPPER, FROGHOPPER, FROGHOPPER, FROGHOPPER,
          MILLIPEDE, MILLIPEDE
      },
      Biomes.PALE_GARDEN, new Critter[]{
          WOODLOUSE
      },
      ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("dappled_forest")), new Critter[]{
          WOODLOUSE, LADYBIRD, WORM, SHIELDBUG,
          FROGHOPPER, MILLIPEDE, YELLOW_FLAT_FOOTED_FLY
      }
  );

  private CritterPools() {
  }

  static Critter choose(ClientLevel level, BlockPos pos) {
    var biome = level.getBiome(pos).unwrapKey().orElse(null);
    Critter[] pool = biome == null ? DEFAULT : BIOMES.getOrDefault(biome, DEFAULT);
    return pool[level.getRandom().nextInt(pool.length)];
  }
}

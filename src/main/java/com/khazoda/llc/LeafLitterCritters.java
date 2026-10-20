package com.khazoda.llc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * called by BlockMixin and ClientLevelMixin to share spawn checks and critter scattering for ambient, walk and break spawns
 */
public final class LeafLitterCritters {
  private LeafLitterCritters() {
  }

  public static void scatter(ClientLevel level, BlockPos pos, BlockState litter, float chance, SpawnCause cause) {
    if (!canSpawn(level, pos)) return;
    Minecraft minecraft = Minecraft.getInstance();
    RandomSource random = level.getRandom();

    int pieces = litter.getValue(SegmentableBlock.AMOUNT);
    Direction facing = litter.getValue(LeafLitterBlock.FACING);
    for (int i = 0; i < pieces; i++) {
      if (random.nextFloat() >= chance) continue;
      if (!CritterParticle.hasSpace(level, cause)) return;
      Direction quadrant = facing;
      for (int j = random.nextInt(pieces); j > 0; j--) quadrant = quadrant.getCounterClockWise();
      double x = pos.getX() + (quadrant == Direction.SOUTH || quadrant == Direction.EAST ? 0.5 : 0) + random.nextDouble() * 0.5;
      double z = pos.getZ() + (quadrant == Direction.SOUTH || quadrant == Direction.WEST ? 0.5 : 0) + random.nextDouble() * 0.5;
      minecraft.particleEngine.add(LeafLitterParticle.create(level, pos, x, z, cause));
    }
  }

  private static boolean canSpawn(ClientLevel level, BlockPos pos) {
    Minecraft minecraft = Minecraft.getInstance();
    ParticleStatus setting = minecraft.options.particles().get();
    return setting != ParticleStatus.MINIMAL && minecraft.getCameraEntity() != null
        && minecraft.getCameraEntity().distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 32 * 32
        && (setting != ParticleStatus.DECREASED || !level.getRandom().nextBoolean());
  }
}

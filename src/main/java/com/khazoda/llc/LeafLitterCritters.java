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
 * called by BlockMixin and ClientLevelMixin to share spawn checks and critter scattering for ambient, walk, break and mob death spawns
 */
public final class LeafLitterCritters {
  private LeafLitterCritters() {
  }

  // scatters critters across a leaf litter block
  public static void scatter(ClientLevel level, BlockPos pos, BlockState litter, SpawnCause cause) {
    scatter(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, litter.getValue(SegmentableBlock.AMOUNT), litter.getValue(LeafLitterBlock.FACING), cause);
  }

  // scatters critters around a position for leaf litter or mob deaths
  public static void scatter(ClientLevel level, double centerX, double y, double centerZ, int pieces, Direction facing, SpawnCause cause) {
    BlockPos pos = BlockPos.containing(centerX, y, centerZ);
    if (!canSpawn(level, pos)) return;
    double rate = CritterConfig.spawnRate(cause);
    double radius = cause == SpawnCause.MOB_DEATH ? 0.25 : 0.5;
    Minecraft minecraft = Minecraft.getInstance();
    RandomSource random = level.getRandom();

    for (int i = 0; i < pieces; i++) {
      for (double remaining = rate; remaining > 0; remaining--) {
        if (random.nextFloat() >= remaining) break;
        if (!CritterParticle.hasSpace(level, cause)) return;
        Direction quadrant = facing;
        for (int j = random.nextInt(pieces); j > 0; j--) quadrant = quadrant.getCounterClockWise();
        double x = centerX - radius + (quadrant == Direction.SOUTH || quadrant == Direction.EAST ? radius : 0) + random.nextDouble() * radius;
        double z = centerZ - radius + (quadrant == Direction.SOUTH || quadrant == Direction.WEST ? radius : 0) + random.nextDouble() * radius;
        minecraft.particleEngine.add(LeafLitterParticle.create(level, pos, x, y, z, cause));
      }
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

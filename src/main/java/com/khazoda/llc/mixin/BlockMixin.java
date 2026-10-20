package com.khazoda.llc.mixin;

import com.khazoda.llc.LeafLitterCritters;
import com.khazoda.llc.SpawnCause;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * spawns ambient critters with long lifespan in leaf litter over time
 */
@Mixin(Block.class)
public abstract class BlockMixin {
  @Inject(method = "animateTick", at = @At("HEAD"))
  private void llc$ambient(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
    if (level instanceof ClientLevel client && state.getBlock() instanceof LeafLitterBlock) {
      LeafLitterCritters.scatter(client, pos, state, 1 / 256F, SpawnCause.AMBIENT);
    }
  }
}

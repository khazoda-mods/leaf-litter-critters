package com.khazoda.llc.mixin;

import com.khazoda.llc.LeafLitterCritters;
import com.khazoda.llc.SpawnCause;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * spawns critters when entities walk on leaf litter or when leaf litter is broken
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
  @Inject(method = "tickNonPassenger", at = @At("TAIL"))
  private void llc$walk(Entity entity, CallbackInfo ci) {
    // check each entity once every ten ticks (entity.getId() offset) spreads work out
    if ((entity.tickCount + entity.getId()) % 10 != 0) return;
    if (!(entity instanceof LivingEntity) || !entity.onGround() || entity.isSpectator() || !entity.isAlive()) return;

    double dx = entity.getX() - entity.xo;
    double dz = entity.getZ() - entity.zo;
    if (dx * dx + dz * dz < 0.0001) return;

    ClientLevel level = (ClientLevel) (Object) this;
    BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() + 0.05, entity.getZ());
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof LeafLitterBlock)) return;

    LeafLitterCritters.scatter(level, pos, state, SpawnCause.WALK);
  }

  @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"))
  private void llc$break(BlockPos pos, BlockState blockState, CallbackInfo ci) {
    if (!(blockState.getBlock() instanceof LeafLitterBlock)) return;
    LeafLitterCritters.scatter((ClientLevel) (Object) this, pos, blockState, SpawnCause.BREAK);
  }
}

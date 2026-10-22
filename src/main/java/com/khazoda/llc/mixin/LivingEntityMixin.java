package com.khazoda.llc.mixin;

import com.khazoda.llc.LeafLitterCritters;
import com.khazoda.llc.SpawnCause;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
  @Inject(method = "handleEntityEvent", at = @At("HEAD"))
  private void llc$onEntityEvent(byte id, CallbackInfo ci) {
    if (id != EntityEvent.DEATH) return;
    LivingEntity entity = (LivingEntity) (Object) this;
    if (!entity.onGround() || !entity.typeHolder().is(EntityTypeTags.ZOMBIES) || !(entity.level() instanceof ClientLevel level)) return;
    LeafLitterCritters.scatter(level, entity.getX(), entity.getY(), entity.getZ(), 12, entity.getDirection(), SpawnCause.MOB_DEATH);
  }
}

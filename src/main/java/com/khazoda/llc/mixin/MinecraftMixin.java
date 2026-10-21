package com.khazoda.llc.mixin;

import com.khazoda.llc.CritterConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  @Inject(method = "<init>", at = @At("TAIL"))
  private void llc$config(CallbackInfo ci) {
    CritterConfig.CONFIG.load();
  }
}

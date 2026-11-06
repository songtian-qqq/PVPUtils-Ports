package com.pvp_utils.mixin.client;

import com.pvp_utils.Config;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.fabricmc.fabric.impl.networking.client.ClientCommonNetworkAddon")
public class ClientNetworkMixin {
    @Inject(method = "handleRegistration", at = @At("HEAD"), cancellable = true)
    private void onHandleRegistration(ResourceLocation channel, CallbackInfo ci) {
        if (Config.modifyChannels) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUnregistration", at = @At("HEAD"), cancellable = true)
    private void onHandleUnregistration(ResourceLocation channel, CallbackInfo ci) {
        if (Config.modifyChannels) {
            ci.cancel();
        }
    }
}

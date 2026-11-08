package com.pvp_utils.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pvp_utils.Config;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.x 的屏幕叠加效果（火焰/水/方块遮挡）由 renderFire 改为提交式 submitFire。
 */
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
    private static void pvp_utils$hideFireOverlay(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (Config.hideFireOverlay) {
            ci.cancel();
        }
    }
}

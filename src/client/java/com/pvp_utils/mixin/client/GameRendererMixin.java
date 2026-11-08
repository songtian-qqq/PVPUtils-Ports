package com.pvp_utils.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pvp_utils.Config;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V"
            )
    )
    private void pvp_utils$renderSkijaUnderlay(net.minecraft.client.DeltaTracker deltaTracker,
                                             boolean renderLevel, CallbackInfo ci) {
        com.pvp_utils.client.render.skia.SkijaRenderer.renderMainSubmitted();
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void hideHurtShake(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (Config.hideHurtShake) ci.cancel();
    }

    @WrapOperation(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;hurtTime:F", opcode = Opcodes.GETFIELD))
    private float pvp_utils$legacy17HurtTiltTime(CameraEntityRenderState entityState, Operation<Float> original) {
        float hurtTime = original.call(entityState);
        return Config.legacy17Animations && Config.legacy17HurtTilt ? Math.max(hurtTime - 1.0F, 0.0F) : hurtTime;
    }
}

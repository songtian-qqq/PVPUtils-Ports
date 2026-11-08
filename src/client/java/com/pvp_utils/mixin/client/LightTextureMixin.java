package com.pvp_utils.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pvp_utils.client.modules.impl.Render.GammaOverrideManager;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.x 中 LightTexture 已被 LightmapRenderStateExtractor + LightmapRenderState 取代，
 * gamma 取值发生在 extract(...) 里（Options.gamma().get() 的第一个 Double.floatValue）。
 */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightTextureMixin {
    @ModifyExpressionValue(
            method = "extract",
            at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0)
    )
    private float overrideGamma(float gamma) {
        return GammaOverrideManager.apply(gamma);
    }
}

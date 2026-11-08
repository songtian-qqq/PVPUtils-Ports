package com.pvp_utils.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.pvp_utils.Config;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.x 移除了 OverlayTexture#setupOverlayColor（受击遮罩改为渲染状态直传），
 * 因此改为在贴图构造完成后写入自定义受击颜色。
 */
@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin {
    @Unique private static final int VANILLA_HIT_COLOR = 0xB2FF0000;

    @Shadow private DynamicTexture texture;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pvp_utils$applyHitColor(CallbackInfo ci) {
        if (!Config.hitColor) return;
        int color = hitColorArgb();
        NativeImage pixels = texture.getPixels();
        if (pixels == null) return;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                pixels.setPixel(x, y, color);
            }
        }
        texture.upload();
    }

    @Unique
    private int hitColorArgb() {
        return ((clampColor(Config.hitColorAlpha) & 0xFF) << 24)
                | ((clampColor(Config.hitColorRed) & 0xFF) << 16)
                | ((clampColor(Config.hitColorGreen) & 0xFF) << 8)
                | (clampColor(Config.hitColorBlue) & 0xFF);
    }

    @Unique
    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

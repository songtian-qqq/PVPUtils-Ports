package com.pvp_utils.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pvp_utils.client.modules.impl.Tool.Zoom.ZoomManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.x 中 GameRenderer#getFov 已被移除，视场角由 Camera#calculateFov 计算，注入点随之迁移。
 */
@Mixin(Camera.class)
public abstract class ZoomGameRendererMixin {
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float pvp_utils$applyZoomFov(float fov) {
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return fov / ZoomManager.getZoomDivisor(tickDelta);
    }
}

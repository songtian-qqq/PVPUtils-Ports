package com.pvp_utils.mixin.client;

import com.pvp_utils.client.compat.GpuApi;
import com.pvp_utils.client.render.skia.SkijaRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vulkan 图形后端下 Skija 的合成入口。
 *
 * <p>GL 后端时 PVPUtils 是在 {@code Minecraft#renderFrame} 的 "帧已 blit 到窗口" 之后直接用 GL 往默认
 * 帧缓冲里画 Skija 图层；Vulkan 后端没有这条路径，所以改在 GUI 抽取（{@code Gui#extractRenderState}）
 * 结束时把 CPU 栅格结果作为一张纹理塞进 {@link GuiRenderState}，交给原版 GUI 管线正常合成。
 */
@Mixin(Gui.class)
public abstract class SkijaVulkanGuiMixin {
    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void pvp_utils$compositeSkijaRaster(DeltaTracker deltaTracker, boolean renderHud,
            boolean renderScreen, CallbackInfo ci) {
        if (!GpuApi.isVulkan()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        var window = minecraft.getWindow();
        if (window == null || window.isMinimized()) return;
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        if (guiWidth <= 0 || guiHeight <= 0) return;

        GuiGraphicsExtractor extractor = new GuiGraphicsExtractor(minecraft, guiRenderState,
                (int) minecraft.mouseHandler.getScaledXPos(window),
                (int) minecraft.mouseHandler.getScaledYPos(window));
        SkijaRenderer.renderVulkanFrame(extractor, guiWidth, guiHeight);
    }
}

package com.pvp_utils.client.render.skia;

import com.pvp_utils.client.compat.GpuApi;
import com.pvp_utils.client.modules.impl.Render.ClockHudRenderer;
import com.pvp_utils.client.modules.impl.Render.DynamicIsland.DynamicIslandRenderer;
import com.pvp_utils.client.modules.impl.Render.HudEditOverlay;
import com.pvp_utils.client.modules.impl.Render.KeystrokesRenderer;
import com.pvp_utils.client.modules.impl.Render.MusicInfoHudRenderer;
import com.pvp_utils.client.modules.impl.Render.PingHudRenderer;
import com.pvp_utils.client.modules.impl.Render.PotionStatusRenderer;
import com.pvp_utils.client.modules.impl.Render.SkiaTextHudRenderer;
import com.pvp_utils.client.modules.impl.Render.TpsHudRenderer;
import net.minecraft.client.Minecraft;

public final class SkijaFrameRenderer {
    private SkijaFrameRenderer() {
    }

    public static void render() {
        // Vulkan 后端下 Skija 没有可用的 GL 上下文，整帧合成改由 SkijaRenderer#renderVulkanFrame
        // 在 GUI 抽取阶段统一完成（CPU 栅格 → 原版 GUI 管线贴图）。
        if (GpuApi.isVulkan()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow().isMinimized() || client.gui.overlay() != null) return;
        SkijaRenderer.renderOverlay(canvas -> drawFrameLayers());
    }

    /**
     * 一帧内所有 Skija 图层（HUD、Skija 界面、ClickGUI、插件渲染），GL 直连与 CPU 栅格后端共用。
     */
    public static void drawFrameLayers() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow().isMinimized() || client.gui.overlay() != null) return;
        if (client.level != null && !client.gui.hud.isHidden()) {
            SkijaRenderer.draw(PotionStatusRenderer.getInstance()::renderSkija);
            SkijaRenderer.draw(KeystrokesRenderer.getInstance()::renderSkija);
            SkijaRenderer.draw(DynamicIslandRenderer.getInstance()::renderSkija);
            SkijaRenderer.draw(hudCanvas -> SkiaTextHudRenderer.renderSkija(hudCanvas,
                    PingHudRenderer.getInstance(),
                    TpsHudRenderer.getInstance(),
                    ClockHudRenderer.getInstance()));
            SkijaRenderer.draw(MusicInfoHudRenderer.getInstance()::renderSkija);
            SkijaRenderer.draw(hudCanvas -> com.pvp_utils.client.plugin.PluginManager.INSTANCE.render(
                    hudCanvas, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight()));
            LiquidGlassRenderer.getInstance().tick();
        }
        if (client.gui.screen() instanceof SkijaScreen screen) SkijaRenderer.draw(screen::renderSkija);
        SkijaRenderer.renderSubmitted();
        SkijaRenderer.draw(HudEditOverlay.getInstance()::renderSkija);
    }

    public static void close() {
        LiquidGlassRenderer.getInstance().destroy();
        GlassCaptureRenderer.getInstance().destroy();
        SkijaRenderer.close();
    }
}

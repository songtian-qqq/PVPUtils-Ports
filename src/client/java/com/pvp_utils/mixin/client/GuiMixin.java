package com.pvp_utils.mixin.client;

import com.pvp_utils.Config;
import com.pvp_utils.client.modules.impl.Tool.TitleDetector;
import com.pvp_utils.client.modules.impl.Render.NotificationOverlay;
import com.pvp_utils.client.modules.impl.Combat.HitMarkerRenderer;
import com.pvp_utils.client.modules.impl.Render.KeystrokesRenderer;
import com.pvp_utils.client.modules.impl.Render.ArmorHudRenderer;
import com.pvp_utils.client.modules.impl.Render.ArraylistRenderer;
import com.pvp_utils.client.modules.impl.Render.PotionStatusRenderer;
import com.pvp_utils.client.modules.impl.Render.TargetHudRenderer;
import com.pvp_utils.client.modules.impl.Render.FallDamagePredictor;
import com.pvp_utils.client.modules.impl.Render.DiggingStatusRenderer;
import com.pvp_utils.client.modules.impl.Render.DamageNumberRenderer;
import com.pvp_utils.client.modules.impl.Render.ClockHudRenderer;
import com.pvp_utils.client.modules.impl.Render.PingHudRenderer;
import com.pvp_utils.client.modules.impl.Render.TpsHudRenderer;
import com.pvp_utils.client.modules.impl.Render.HudEditOverlay;
import com.pvp_utils.client.modules.impl.Render.ItemUseStatusRenderer;
import com.pvp_utils.client.modules.impl.Render.LyricsDisplayRenderer;
import com.pvp_utils.client.modules.impl.Render.MusicInfoHudRenderer;
import com.pvp_utils.client.modules.impl.Tool.BlockCountDisplayRenderer;
import com.pvp_utils.client.modules.impl.Optimize.BetterScoreboard.BetterScoreboardRenderer;

import com.pvp_utils.client.render.skia.SkijaScreen;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {
    @ModifyExpressionValue(
            method = "extractCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z")
    )
    private boolean pvp_utils$showCrosshairInThirdPerson(boolean original, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        return true;
    }

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Component title, CallbackInfo ci) {
        TitleDetector.check(title != null ? title.getString() : null, null);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void onSetSubtitle(Component subtitle, CallbackInfo ci) {
        TitleDetector.check(null, subtitle != null ? subtitle.getString() : null);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        boolean skiaScreenOpen = mc.gui.screen() instanceof SkijaScreen;
        if (((Hud) (Object) this).isHidden()) return;

        if (!skiaScreenOpen) {
            NotificationOverlay.getInstance().extractRenderState(guiGraphics);
        }
        HitMarkerRenderer.getInstance().extractRenderState(guiGraphics);
        TargetHudRenderer.getInstance().extractRenderState(guiGraphics);
        ItemUseStatusRenderer.getInstance().extractRenderState(guiGraphics);
        DamageNumberRenderer.getInstance().extractRenderState(guiGraphics);
        FallDamagePredictor.getInstance().extractRenderState(guiGraphics);
        DiggingStatusRenderer.getInstance().extractRenderState(guiGraphics);
        ArraylistRenderer.getInstance().extractRenderState(guiGraphics);
        KeystrokesRenderer.getInstance().extractRenderState(guiGraphics);
        ArmorHudRenderer.getInstance().extractRenderState(guiGraphics);
        PotionStatusRenderer.getInstance().extractRenderState(guiGraphics);
        BetterScoreboardRenderer.getInstance().extractRenderState(guiGraphics);
        PingHudRenderer.getInstance().extractRenderState(guiGraphics);
        TpsHudRenderer.getInstance().extractRenderState(guiGraphics);
        ClockHudRenderer.getInstance().extractRenderState(guiGraphics);
        HudEditOverlay.getInstance().extractRenderState(guiGraphics, null);

        guiGraphics.extractDeferredElements(0, 0, 0.0f);
        LyricsDisplayRenderer.getInstance().extractRenderState(guiGraphics);
        guiGraphics.extractDeferredElements(0, 0, 0.0f);
        MusicInfoHudRenderer.getInstance().extractRenderState(guiGraphics);
        guiGraphics.extractDeferredElements(0, 0, 0.0f);
        BlockCountDisplayRenderer.getInstance().extractRenderState(guiGraphics, null);
        guiGraphics.extractDeferredElements(0, 0, 0.0f);
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void pvp_utils$hideVanillaPotionEffects(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PotionStatusRenderer.getInstance().shouldHideVanillaEffects()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void pvp_utils$hideVignette(GuiGraphicsExtractor guiGraphics, @Nullable Entity entity, CallbackInfo ci) {
        if (Config.hideVignette) {
            ci.cancel();
        }
    }

    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
    private void pvp_utils$hideBossBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Config.hideBossBar) {
            ci.cancel();
        }
    }
}

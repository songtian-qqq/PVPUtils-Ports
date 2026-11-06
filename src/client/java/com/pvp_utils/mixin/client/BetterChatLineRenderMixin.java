package com.pvp_utils.mixin.client;

import com.mojang.authlib.GameProfile;
import com.pvp_utils.Config;
import com.pvp_utils.client.modules.impl.Render.BetterChat.BetterChatLineProfileAccessor;
import com.pvp_utils.client.modules.impl.Render.BetterChat.BetterChatRenderState;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.10 chat line port: the ChatGraphicsAccess indirection used by 1.21.11 does not exist yet,
 * the per line text is drawn directly by ChatComponent#method_71991(GuiGraphics, ..., Line).
 */
@Mixin(ChatComponent.class)
public abstract class BetterChatLineRenderMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique private static final int AVATAR_SIZE = 8;
    @Unique private static final int AVATAR_GAP = 2;
    @Unique private static final int LINE_SHIFT = AVATAR_SIZE + AVATAR_GAP;

    @Unique
    private boolean pvp_utils$hasAvatar(GuiMessage.Line line) {
        if (!Config.betterChat || !Config.betterChatAvatar || line == null) {
            return false;
        }
        return ((BetterChatLineProfileAccessor) (Object) line).pvp_utils$getOwnerProfile() != null;
    }

    @Inject(
            method = "method_71991",
            at = @At("HEAD")
    )
    private void pvp_utils$drawAvatar(int yOffset, GuiGraphics graphics, float alpha, int textX, int unused1, int textBase,
                                      GuiMessage.Line line, int unused2, float partialTick, CallbackInfo ci) {
        boolean avatar = pvp_utils$hasAvatar(line);
        BetterChatRenderState.setShiftingAvatarLine(avatar);
        if (!avatar || graphics == null) {
            return;
        }
        GameProfile profile = ((BetterChatLineProfileAccessor) (Object) line).pvp_utils$getOwnerProfile();
        try {
            PlayerSkin skin = this.minecraft.getSkinManager().createLookup(profile, false).get();
            PlayerFaceRenderer.draw(graphics, skin, textX, textBase + yOffset - 1, AVATAR_SIZE);
        } catch (Throwable ignored) {
        }
    }

    @Inject(
            method = "method_71991",
            at = @At("RETURN")
    )
    private void pvp_utils$clearAvatarShift(int yOffset, GuiGraphics graphics, float alpha, int textX, int unused1, int textBase,
                                            GuiMessage.Line line, int unused2, float partialTick, CallbackInfo ci) {
        BetterChatRenderState.setShiftingAvatarLine(false);
    }

    @ModifyArg(
            method = "method_71991",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V"
            ),
            index = 2
    )
    private int pvp_utils$shiftTextX(int x) {
        return BetterChatRenderState.isShiftingAvatarLine() ? x + LINE_SHIFT : x;
    }
}

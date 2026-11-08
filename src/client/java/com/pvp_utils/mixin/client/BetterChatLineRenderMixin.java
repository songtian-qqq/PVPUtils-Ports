package com.pvp_utils.mixin.client;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 26.x 聊天逐行绘制被内联进 ChatComponent#extractRenderState 的私有 lambda，
 * 且该 lambda 只拿到 ChatGraphicsAccess（仅支持位姿/矩形填充/文本），
 * 无法再拿到 GuiGraphicsExtractor 提交头像 blit，故 26.2 上暂不注入头像与缩进。
 * 保留空 Mixin 以维持 mixin 配置一致性。
 */
@Mixin(ChatComponent.class)
public abstract class BetterChatLineRenderMixin {
}

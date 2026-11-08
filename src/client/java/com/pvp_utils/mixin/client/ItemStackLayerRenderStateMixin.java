package com.pvp_utils.mixin.client;

import com.pvp_utils.Config;
import com.pvp_utils.client.modules.impl.Tool.HeldItemPositionManager;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.2 物品渲染：{@link ItemStackRenderState.LayerRenderState} 收集渲染状态，
 * 由 {@code LayerRenderState#submit(PoseStack, SubmitNodeCollector, int, int, int)} 统一
 * 提交 {@code SubmitNodeCollector#submitItem}。该类不再持有 displayContext 字段，
 * 上下文只能从 submitItem 的参数中取，故改用 {@link ModifyArgs} 一次性改写 tint 数组与 quad 列表。
 */
@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public class ItemStackLayerRenderStateMixin {

    @ModifyArgs(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V",
                    ordinal = 0
            )
    )
    private void pvp_utils$adjustItemSubmit(Args args) {
        ItemDisplayContext displayContext = args.get(1);
        int[] colors = args.get(5);
        List<BakedQuad> quads = args.get(6);

        if (Config.heldItemPosition) {
            int[] alpha = HeldItemPositionManager.applyHeldItemAlpha(colors);
            if (alpha != colors) {
                args.set(5, alpha);
            }
        }
        if (!Config.legacy17Animations || displayContext != ItemDisplayContext.GUI
                || quads == null || quads.isEmpty()) {
            return;
        }
        List<BakedQuad> filtered = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (quad.direction() != Direction.SOUTH) {
                filtered.add(quad);
            }
        }
        if (filtered.size() != quads.size()) {
            args.set(6, filtered);
        }
    }
}

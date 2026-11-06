package com.pvp_utils.mixin.client;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.pvp_utils.client.modules.impl.Render.CustomEnchantmentGlint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * RenderType$CompositeRenderType draws through RenderPass#bindSampler(String, GpuTextureView) in
 * 1.21.9/1.21.10. The render type name is declared on RenderStateShard, hence the accessor cast.
 */
@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public class RenderTypeMixin {
    @ModifyArgs(
            method = "draw(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;bindSampler(Ljava/lang/String;Lcom/mojang/blaze3d/textures/GpuTextureView;)V"
            )
    )
    private void pvp_utils$replaceGlintTexture(Args args) {
        if (!"Sampler0".equals(args.get(0))) {
            return;
        }
        String name = ((RenderStateShardAccessor) (Object) this).pvp_utils$getName();
        GpuTextureView textureView = CustomEnchantmentGlint.textureViewFor(name);
        if (textureView != null) {
            args.set(1, textureView);
        }
    }
}

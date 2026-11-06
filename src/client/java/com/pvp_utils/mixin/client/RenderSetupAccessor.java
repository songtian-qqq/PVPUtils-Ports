package com.pvp_utils.mixin.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface RenderSetupAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard pvp_utils$getTextureState();
}

package com.pvp_utils.mixin.client;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * RenderType no longer declares its own "name" field in 1.21.9/1.21.10; the field lives on
 * RenderStateShard, so it has to be reached through the declaring class instead of @Shadow.
 */
@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("name")
    String pvp_utils$getName();
}

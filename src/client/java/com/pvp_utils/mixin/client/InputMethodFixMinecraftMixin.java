package com.pvp_utils.mixin.client;

import com.pvp_utils.client.modules.impl.Optimize.InputMethodFix.InputMethodFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.x 移除了 Minecraft#setWindowActive，窗口激活状态改为在 tick 中轮询；
 * 切换界面改走 setScreenAndShow。
 */
@Mixin(Minecraft.class)
public abstract class InputMethodFixMinecraftMixin {
    @Unique private Boolean pvp_utils$lastWindowActive;

    @Inject(method = "tick", at = @At("TAIL"))
    private void pvp_utils$pollWindowActive(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        boolean active = client.isWindowActive();
        if (pvp_utils$lastWindowActive != null && pvp_utils$lastWindowActive == active) {
            return;
        }
        pvp_utils$lastWindowActive = active;
        InputMethodFix.onWindowActiveChanged(active, client);
    }

    @Inject(method = "setScreenAndShow", at = @At("TAIL"))
    private void pvp_utils$onScreenChanged(Screen screen, CallbackInfo ci) {
        InputMethodFix.onScreenChanged(screen, (Minecraft) (Object) this);
    }
}

package com.pvp_utils.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.resources.server.DownloadedPackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 26.x 移除了 server.packs.DownloadQueue#method_55485，改为在资源包下载目录入口
 * （DownloadedPackSource 构造时的 cache 根目录）按账号 UUID 隔离，等价实现多开防串包。
 */
@Mixin(DownloadedPackSource.class)
public class DownloadQueueMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Path pvp_utils$isolatePackCache(Path cacheRoot, Minecraft client, Path originalCacheRoot, GameConfig.UserData userData) {
        UUID id = null;
        User user = userData != null ? userData.user : null;
        if (user != null) {
            id = user.getProfileId();
        }
        if (id == null) {
            try {
                Minecraft instance = Minecraft.getInstance();
                User fallback = instance == null ? null : instance.getUser();
                if (fallback != null) {
                    id = fallback.getProfileId();
                }
            } catch (Throwable ignored) {
            }
        }
        if (id == null) {
            return cacheRoot;
        }
        return cacheRoot.resolve(id.toString());
    }
}

package com.pvp_utils.client.compat;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Locale;

/**
 * 图形后端探测。
 *
 * <p>Minecraft 26.x 起客户端自带两套 {@code GpuBackend} 实现：{@code com.mojang.blaze3d.opengl.GlBackend}
 * （名称 "OpenGL"）与 {@code com.mojang.blaze3d.vulkan.VulkanBackend}（名称 "Vulkan"），玩家可在
 * 视频设置里切换。PVPUtils 的部分渲染路径（Skija 的 GL 直连后端、自定义 GLSL、FBO 抓帧、液体玻璃）
 * 只在 OpenGL 后端可用，在 Vulkan 后端执行会命中 LWJGL 的 "No GLCapabilities instance" 之类的异常。
 *
 * <p>因此所有 GL 专用代码在动手前都要先问一句 {@link #isVulkan()}，Vulkan 下走无 GL 的替代实现。
 */
public final class GpuApi {
    public enum Backend {
        OPENGL,
        VULKAN
    }

    /** 排障/测试开关：-Dpvp_utils.forceVulkan=true / -Dpvp_utils.forceOpenGl=true */
    private static final String FORCE_VULKAN = "pvp_utils.forceVulkan";
    private static final String FORCE_OPENGL = "pvp_utils.forceOpenGl";

    private static Backend cached;

    private GpuApi() {
    }

    public static synchronized Backend backend() {
        if (cached == null) {
            cached = detect();
        }
        return cached;
    }

    public static boolean isVulkan() {
        return backend() == Backend.VULKAN;
    }

    public static boolean isOpenGl() {
        return backend() == Backend.OPENGL;
    }

    /** 供日志使用的后端描述，探测失败也不会抛异常。 */
    public static String describe() {
        try {
            GpuDevice device = RenderSystem.tryGetDevice();
            if (device != null && device.getDeviceInfo() != null) {
                var info = device.getDeviceInfo();
                return info.backendName() + " / " + info.name();
            }
        } catch (Throwable ignored) {
        }
        try {
            return RenderSystem.getBackendDescription();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static Backend detect() {
        if (Boolean.getBoolean(FORCE_VULKAN)) {
            return Backend.VULKAN;
        }
        if (Boolean.getBoolean(FORCE_OPENGL)) {
            return Backend.OPENGL;
        }

        String backendName = null;
        try {
            GpuDevice device = RenderSystem.tryGetDevice();
            if (device != null && device.getDeviceInfo() != null) {
                backendName = device.getDeviceInfo().backendName();
            }
        } catch (Throwable ignored) {
        }
        if (backendName == null || backendName.isBlank()) {
            try {
                backendName = RenderSystem.getBackendDescription();
            } catch (Throwable ignored) {
            }
        }
        return looksLikeVulkan(backendName) ? Backend.VULKAN : Backend.OPENGL;
    }

    private static boolean looksLikeVulkan(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).contains("vulkan");
    }
}

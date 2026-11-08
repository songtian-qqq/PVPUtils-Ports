package com.pvp_utils.client.render.skia;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pvp_utils.PVPUtils;
import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Surface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;

/**
 * Skija 的 CPU 栅格后端。
 *
 * <p>Skija 的 {@code DirectContext} 只有 GL / Metal / D3D12 后端，没有 Vulkan 后端。因此在 Vulkan 图形
 * 后端下不能复用 {@code BackendRenderTarget.makeGL(...)} 那套直连 FBO 的画法，改为：
 * 画到 {@code Surface.makeRasterN32Premul} 的 CPU 画布 → 读回像素 → 写成 RGBA 的
 * {@link DynamicTexture} → 交给原版 GUI 管线（{@code GuiGraphicsExtractor#blit}）贴到屏幕上。
 *
 * <p>绘制代码（所有 {@code SkijaUi} / 页面 / HUD painter）完全不用改。
 */
final class SkijaRasterCanvas {
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath("pvp_utils", "skija_overlay");

    private Surface surface;
    private Bitmap bitmap;
    private NativeImage image;
    private DynamicTexture texture;
    private int width = -1;
    private int height = -1;
    private boolean failed;
    private boolean registered;

    Canvas begin(int requestedWidth, int requestedHeight) {
        if (failed) return null;
        int w = Math.max(1, requestedWidth);
        int h = Math.max(1, requestedHeight);
        try {
            if (surface == null || width != w || height != h) {
                releaseSurface();
                surface = Surface.makeRasterN32Premul(w, h);
                bitmap = new Bitmap();
                if (!bitmap.allocN32Pixels(w, h, false)) {
                    throw new IllegalStateException("Bitmap.allocN32Pixels failed");
                }
                ensureTexture(w, h);
                width = w;
                height = h;
            }
            if (surface == null || bitmap == null || image == null) {
                failed = true;
                return null;
            }
            Canvas canvas = surface.getCanvas();
            canvas.clear(0x00000000);
            return canvas;
        } catch (Throwable throwable) {
            failed = true;
            PVPUtils.LOGGER.error("Skija 栅格后端初始化失败，Vulkan 兼容模式下停用 Skija UI", throwable);
            releaseSurface();
            return null;
        }
    }

    /** 把画布内容读回并上传到 GPU 纹理。 */
    boolean flush() {
        if (failed || surface == null || bitmap == null || image == null || texture == null) return false;
        try {
            Image snapshot = surface.makeImageSnapshot();
            try {
                if (!snapshot.readPixels(bitmap)) {
                    return false;
                }
            } finally {
                snapshot.close();
            }
            copyPixels(bitmap.readPixels());
            texture.upload();
            return true;
        } catch (Throwable throwable) {
            failed = true;
            PVPUtils.LOGGER.error("Skija 栅格后端上传失败，Vulkan 兼容模式下停用 Skija UI", throwable);
            return false;
        }
    }

    net.minecraft.client.renderer.texture.DynamicTexture texture() {
        return texture;
    }

    Identifier textureId() {
        return registered ? TEXTURE_ID : null;
    }

    void close() {
        releaseSurface();
        if (registered) {
            try {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            } catch (Throwable ignored) {
            }
            registered = false;
        }
        texture = null;
    }

    private void ensureTexture(int w, int h) {
        Minecraft minecraft = Minecraft.getInstance();
        if (registered) {
            minecraft.getTextureManager().release(TEXTURE_ID);
            registered = false;
        }
        if (image != null) {
            image.close();
            image = null;
        }
        image = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        texture = new DynamicTexture(() -> "pvp_utils:skija_overlay", image);
        minecraft.getTextureManager().register(TEXTURE_ID, texture);
        registered = true;
    }

    /**
     * Skia 的 N32 在 little-endian 上是 BGRA(8) 且预乘 alpha，而原版 GUI 管线用的是非预乘 RGBA，
     * 所以这里顺手做一次 BGRA→RGBA + 反预乘。
     */
    private void copyPixels(byte[] source) {
        ByteBuffer target = image.getPixelBytes();
        int pixels = width * height;
        for (int i = 0; i < pixels; i++) {
            int s = i * 4;
            int b = source[s] & 0xFF;
            int g = source[s + 1] & 0xFF;
            int r = source[s + 2] & 0xFF;
            int a = source[s + 3] & 0xFF;
            if (a != 0 && a != 255) {
                r = Math.min(255, (r * 255 + a / 2) / a);
                g = Math.min(255, (g * 255 + a / 2) / a);
                b = Math.min(255, (b * 255 + a / 2) / a);
            } else if (a == 0) {
                r = 0;
                g = 0;
                b = 0;
            }
            int d = i * 4;
            target.put(d, (byte) r);
            target.put(d + 1, (byte) g);
            target.put(d + 2, (byte) b);
            target.put(d + 3, (byte) a);
        }
    }

    private void releaseSurface() {
        if (bitmap != null) {
            bitmap.close();
            bitmap = null;
        }
        if (surface != null) {
            surface.close();
            surface = null;
        }
        if (image != null) {
            image.close();
            image = null;
        }
        width = -1;
        height = -1;
    }
}

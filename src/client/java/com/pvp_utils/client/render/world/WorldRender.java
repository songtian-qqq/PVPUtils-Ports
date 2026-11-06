package com.pvp_utils.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.10 port of the 1.21.11 gizmo based world renderer.
 * The gizmo API (net.minecraft.gizmos) only exists in 1.21.11, so the same public
 * surface is kept here but implemented as a per frame command queue that is flushed
 * through the vanilla line / filled box render types.
 */
public final class WorldRender {
    private static final Matrix4f MODEL_VIEW = new Matrix4f();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final List<Command> COMMANDS = new ArrayList<>();
    private static Vec3 cameraPosition = Vec3.ZERO;
    private static boolean projectionReady;

    private WorldRender() {
    }

    public static void capture(Camera camera, Matrix4fc modelViewMatrix, Matrix4fc projectionMatrix) {
        if (camera == null) {
            projectionReady = false;
            return;
        }
        cameraPosition = camera.position();
        MODEL_VIEW.set(modelViewMatrix);
        PROJECTION.set(projectionMatrix);
        projectionReady = true;
    }

    public static void beginFrame() {
        COMMANDS.clear();
    }

    public static void line(Vec3 from, Vec3 to, int color, float width) {
        line(from, to, color, width, false);
    }

    public static void line(Vec3 from, Vec3 to, int color, float width, boolean throughWalls) {
        if (from == null || to == null) {
            return;
        }
        COMMANDS.add(new LineCommand(from, to, color, throughWalls));
    }

    public static void box(AABB box, int color) {
        if (box == null) {
            return;
        }
        COMMANDS.add(new BoxCommand(box, color, 0, false));
    }

    public static void filledBox(AABB box, int color) {
        filledBox(box, color, false);
    }

    public static void filledBox(AABB box, int color, boolean throughWalls) {
        if (box == null) {
            return;
        }
        COMMANDS.add(new BoxCommand(box, 0, color, throughWalls));
    }

    public static void box(AABB box, int outlineColor, int fillColor) {
        if (box == null) {
            return;
        }
        COMMANDS.add(new BoxCommand(box, outlineColor, fillColor, false));
    }

    public static void face(AABB box, Vec3 viewer, int color, float width) {
        double x = viewer.x - (box.minX + box.maxX) * 0.5;
        double y = viewer.y - (box.minY + box.maxY) * 0.5;
        double z = viewer.z - (box.minZ + box.maxZ) * 0.5;
        double ax = Math.abs(x);
        double ay = Math.abs(y);
        double az = Math.abs(z);
        double offset = 0.002;
        Vec3 a;
        Vec3 b;
        Vec3 c;
        Vec3 d;

        if (ax >= ay && ax >= az) {
            double faceX = x >= 0.0 ? box.maxX + offset : box.minX - offset;
            a = new Vec3(faceX, box.minY, box.minZ);
            b = new Vec3(faceX, box.maxY, box.minZ);
            c = new Vec3(faceX, box.maxY, box.maxZ);
            d = new Vec3(faceX, box.minY, box.maxZ);
        } else if (ay >= az) {
            double faceY = y >= 0.0 ? box.maxY + offset : box.minY - offset;
            a = new Vec3(box.minX, faceY, box.minZ);
            b = new Vec3(box.maxX, faceY, box.minZ);
            c = new Vec3(box.maxX, faceY, box.maxZ);
            d = new Vec3(box.minX, faceY, box.maxZ);
        } else {
            double faceZ = z >= 0.0 ? box.maxZ + offset : box.minZ - offset;
            a = new Vec3(box.minX, box.minY, faceZ);
            b = new Vec3(box.minX, box.maxY, faceZ);
            c = new Vec3(box.maxX, box.maxY, faceZ);
            d = new Vec3(box.maxX, box.minY, faceZ);
        }

        line(a, b, color, width);
        line(b, c, color, width);
        line(c, d, color, width);
        line(d, a, color, width);
    }

    public static void face(AABB box, Direction direction, int color, float width) {
        double offset = 0.002;
        Vec3 a;
        Vec3 b;
        Vec3 c;
        Vec3 d;

        switch (direction) {
            case UP -> {
                double y = box.maxY + offset;
                a = new Vec3(box.minX, y, box.minZ);
                b = new Vec3(box.maxX, y, box.minZ);
                c = new Vec3(box.maxX, y, box.maxZ);
                d = new Vec3(box.minX, y, box.maxZ);
            }
            case DOWN -> {
                double y = box.minY - offset;
                a = new Vec3(box.minX, y, box.minZ);
                b = new Vec3(box.maxX, y, box.minZ);
                c = new Vec3(box.maxX, y, box.maxZ);
                d = new Vec3(box.minX, y, box.maxZ);
            }
            case EAST -> {
                double x = box.maxX + offset;
                a = new Vec3(x, box.minY, box.minZ);
                b = new Vec3(x, box.maxY, box.minZ);
                c = new Vec3(x, box.maxY, box.maxZ);
                d = new Vec3(x, box.minY, box.maxZ);
            }
            case WEST -> {
                double x = box.minX - offset;
                a = new Vec3(x, box.minY, box.minZ);
                b = new Vec3(x, box.maxY, box.minZ);
                c = new Vec3(x, box.maxY, box.maxZ);
                d = new Vec3(x, box.minY, box.maxZ);
            }
            case SOUTH -> {
                double z = box.maxZ + offset;
                a = new Vec3(box.minX, box.minY, z);
                b = new Vec3(box.minX, box.maxY, z);
                c = new Vec3(box.maxX, box.maxY, z);
                d = new Vec3(box.maxX, box.minY, z);
            }
            default -> {
                double z = box.minZ - offset;
                a = new Vec3(box.minX, box.minY, z);
                b = new Vec3(box.minX, box.maxY, z);
                c = new Vec3(box.maxX, box.maxY, z);
                d = new Vec3(box.maxX, box.minY, z);
            }
        }

        line(a, b, color, width);
        line(b, c, color, width);
        line(c, d, color, width);
        line(d, a, color, width);
    }

    /**
     * Flushes the queued commands. Must be called while the level render pass is still
     * recording vertices (fabric WorldRenderEvents.AFTER_TRANSLUCENT).
     */
    public static void flush() {
        if (COMMANDS.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        Camera camera = client == null ? null : client.gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) {
            COMMANDS.clear();
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(camera.rotation());
        PoseStack.Pose pose = poseStack.last();
        Vec3 cam = camera.getPosition();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();

        // pass 1: depth tested
        VertexConsumer lines = null;
        VertexConsumer fills = null;
        for (Command command : COMMANDS) {
            if (lines == null) {
                lines = buffers.getBuffer(RenderType.lines());
                fills = buffers.getBuffer(RenderType.debugFilledBox());
            }
            command.emit(poseStack, pose, lines, fills, cam);
        }
        if (lines != null) {
            buffers.endBatch(RenderType.lines());
            buffers.endBatch(RenderType.debugFilledBox());
        }

        // pass 2: previously re-drawn without depth test.
        // 1.21.9+ moved depth state into immutable RenderPipelines, and RenderType construction
        // from mods is no longer public API, so a no-depth-test line RenderType cannot be created
        // here. Through-wall geometry is therefore drawn in the depth-tested pass only.

        COMMANDS.clear();
    }

    public static ScreenPoint project(Vec3 position) {
        if (!projectionReady) {
            return null;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return null;
        }

        Vector4f clip = new Vector4f(
                (float) (position.x - cameraPosition.x),
                (float) (position.y - cameraPosition.y),
                (float) (position.z - cameraPosition.z),
                1.0f
        );
        clip.mul(MODEL_VIEW).mul(PROJECTION);
        if (clip.w <= 0.01f) {
            return null;
        }

        float x = clip.x / clip.w;
        float y = clip.y / clip.w;
        if (x < -1.0f || x > 1.0f || y < -1.0f || y > 1.0f) {
            return null;
        }
        return new ScreenPoint(
                (x * 0.5f + 0.5f) * client.getWindow().getGuiScaledWidth(),
                (0.5f - y * 0.5f) * client.getWindow().getGuiScaledHeight()
        );
    }

    public record ScreenPoint(float x, float y) {
    }

    private interface Command {
        boolean throughWalls();

        void emit(PoseStack poseStack, PoseStack.Pose pose, VertexConsumer lines, VertexConsumer fills, Vec3 cam);
    }

    private record LineCommand(Vec3 from, Vec3 to, int color, boolean throughWalls) implements Command {
        @Override
        public void emit(PoseStack poseStack, PoseStack.Pose pose, VertexConsumer lines, VertexConsumer fills, Vec3 cam) {
            vertex(lines, pose, from, cam, color);
            vertex(lines, pose, to, cam, color);
        }
    }

    private record BoxCommand(AABB box, int outlineColor, int fillColor, boolean throughWalls) implements Command {
        @Override
        public void emit(PoseStack poseStack, PoseStack.Pose pose, VertexConsumer lines, VertexConsumer fills, Vec3 cam) {
            if (fillColor != 0) {
                ShapeRenderer.addChainedFilledBoxVertices(
                        poseStack,
                        fills,
                        box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z,
                        box.maxX - cam.x, box.maxY - cam.y, box.maxZ - cam.z,
                        ((fillColor >> 16) & 0xFF) / 255.0f,
                        ((fillColor >> 8) & 0xFF) / 255.0f,
                        (fillColor & 0xFF) / 255.0f,
                        ((fillColor >>> 24) & 0xFF) / 255.0f
                );
            }
            if (outlineColor != 0) {
                ShapeRenderer.renderLineBox(
                        pose,
                        lines,
                        box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z,
                        box.maxX - cam.x, box.maxY - cam.y, box.maxZ - cam.z,
                        ((outlineColor >> 16) & 0xFF) / 255.0f,
                        ((outlineColor >> 8) & 0xFF) / 255.0f,
                        (outlineColor & 0xFF) / 255.0f,
                        ((outlineColor >>> 24) & 0xFF) / 255.0f
                );
            }
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 position, Vec3 cam, int color) {
        consumer.addVertex(pose, (float) (position.x - cam.x), (float) (position.y - cam.y), (float) (position.z - cam.z))
                .setColor(color)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}

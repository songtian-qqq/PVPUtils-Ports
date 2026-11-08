package com.pvp_utils.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
     * 26.x 采用提交式渲染：几何必须在关卡渲染的 submit 阶段通过
     * {@link SubmitNodeCollector#submitCustomGeometry} 交给渲染器，
     * 不能再直接写入 VertexConsumer。由 WorldRenderLevelRendererMixin 在
     * LevelRenderer#submitFeatures 头部调用，传入的是恒等 PoseStack（顶点为相机相对坐标）。
     */
    public static void flush(PoseStack poseStack, SubmitNodeCollector collector) {
        if (COMMANDS.isEmpty()) {
            return;
        }
        List<Command> commands = List.copyOf(COMMANDS);
        COMMANDS.clear();
        Vec3 cam = cameraPosition;

        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) -> {
            for (Command command : commands) {
                if (command.outlineColor() != 0) {
                    command.emitLines(pose, consumer, cam);
                }
            }
        });
        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, consumer) -> {
            for (Command command : commands) {
                if (command.fillColor() != 0) {
                    command.emitFills(pose, consumer, cam);
                }
            }
        });

        // 穿透墙体（throughWall）需要关闭深度测试的管线；26.x 的 RenderPipeline 不可由模组构造，
        // 因此穿透几何暂与普通几何共用深度测试管线。
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

        int outlineColor();

        int fillColor();

        void emitLines(PoseStack.Pose pose, VertexConsumer lines, Vec3 cam);

        void emitFills(PoseStack.Pose pose, VertexConsumer fills, Vec3 cam);
    }

    private record LineCommand(Vec3 from, Vec3 to, int color, boolean throughWalls) implements Command {
        @Override
        public int outlineColor() {
            return color;
        }

        @Override
        public int fillColor() {
            return 0;
        }

        @Override
        public void emitLines(PoseStack.Pose pose, VertexConsumer lines, Vec3 cam) {
            vertex(lines, pose, from, cam, color);
            vertex(lines, pose, to, cam, color);
        }

        @Override
        public void emitFills(PoseStack.Pose pose, VertexConsumer fills, Vec3 cam) {
        }
    }

    private record BoxCommand(AABB box, int outlineColor, int fillColor, boolean throughWalls) implements Command {
        @Override
        public void emitLines(PoseStack.Pose pose, VertexConsumer lines, Vec3 cam) {
            if (outlineColor == 0) {
                return;
            }
            float x1 = (float) (box.minX - cam.x);
            float y1 = (float) (box.minY - cam.y);
            float z1 = (float) (box.minZ - cam.z);
            float x2 = (float) (box.maxX - cam.x);
            float y2 = (float) (box.maxY - cam.y);
            float z2 = (float) (box.maxZ - cam.z);

            // 4 条平行于 X
            line(lines, pose, x1, y1, z1, x2, y1, z1, outlineColor);
            line(lines, pose, x1, y2, z1, x2, y2, z1, outlineColor);
            line(lines, pose, x1, y1, z2, x2, y1, z2, outlineColor);
            line(lines, pose, x1, y2, z2, x2, y2, z2, outlineColor);
            // 4 条平行于 Y
            line(lines, pose, x1, y1, z1, x1, y2, z1, outlineColor);
            line(lines, pose, x2, y1, z1, x2, y2, z1, outlineColor);
            line(lines, pose, x1, y1, z2, x1, y2, z2, outlineColor);
            line(lines, pose, x2, y1, z2, x2, y2, z2, outlineColor);
            // 4 条平行于 Z
            line(lines, pose, x1, y1, z1, x1, y1, z2, outlineColor);
            line(lines, pose, x2, y1, z1, x2, y1, z2, outlineColor);
            line(lines, pose, x1, y2, z1, x1, y2, z2, outlineColor);
            line(lines, pose, x2, y2, z1, x2, y2, z2, outlineColor);
        }

        @Override
        public void emitFills(PoseStack.Pose pose, VertexConsumer fills, Vec3 cam) {
            if (fillColor == 0) {
                return;
            }
            float x1 = (float) (box.minX - cam.x);
            float y1 = (float) (box.minY - cam.y);
            float z1 = (float) (box.minZ - cam.z);
            float x2 = (float) (box.maxX - cam.x);
            float y2 = (float) (box.maxY - cam.y);
            float z2 = (float) (box.maxZ - cam.z);

            quad(fills, pose, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, fillColor); // -Z
            quad(fills, pose, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, fillColor); // +Z
            quad(fills, pose, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, fillColor); // -X
            quad(fills, pose, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, fillColor); // +X
            quad(fills, pose, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, fillColor); // +Y
            quad(fills, pose, x1, y1, z2, x1, y1, z1, x2, y1, z1, x2, y1, z2, fillColor); // -Y
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        vertex(consumer, pose, x1, y1, z1, color);
        vertex(consumer, pose, x2, y2, z2, color);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int color) {
        vertex(consumer, pose, x1, y1, z1, color);
        vertex(consumer, pose, x2, y2, z2, color);
        vertex(consumer, pose, x3, y3, z3, color);
        vertex(consumer, pose, x4, y4, z4, color);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 position, Vec3 cam, int color) {
        vertex(consumer, pose,
                (float) (position.x - cam.x), (float) (position.y - cam.y), (float) (position.z - cam.z), color);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int color) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}

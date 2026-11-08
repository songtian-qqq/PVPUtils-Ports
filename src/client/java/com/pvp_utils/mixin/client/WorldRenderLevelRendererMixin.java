package com.pvp_utils.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.pvp_utils.client.modules.impl.Tool.FireballLandingPredictor;
import com.pvp_utils.client.modules.impl.Tool.ProjectileTrajectoryPredictor;
import com.pvp_utils.client.render.world.CustomBlockOutlineRenderer;
import com.pvp_utils.client.render.world.WorldRender;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRenderLevelRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void pvp_utils$renderWorld(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelView, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        WorldRender.capture(camera, modelView, cameraState.projectionMatrix);
        WorldRender.beginFrame();
        FireballLandingPredictor.render();
        ProjectileTrajectoryPredictor.render(deltaTracker.getGameTimeDeltaPartialTick(false));
        CustomBlockOutlineRenderer.render();
    }

    /**
     * 26.x 的关卡几何全部在 submit 阶段提交；submitFeatures 内部使用恒等 PoseStack
     * （顶点为相机相对坐标），这里在头部把排队的预测线/描边提交给渲染器。
     */
    @Inject(method = "submitFeatures", at = @At("HEAD"))
    private void pvp_utils$submitWorldRender(LevelRenderState levelRenderState, SubmitNodeCollector collector, boolean renderBlockOutline, CallbackInfo ci) {
        WorldRender.flush(new PoseStack(), collector);
    }
}

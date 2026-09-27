package com.vescagaming.guardianbanner.client;

import com.vescagaming.guardianbanner.network.ClaimSyncPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        value = Dist.CLIENT,
        modid = "guardianbanner",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ClaimHighlightRenderer {
    private ClaimHighlightRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        for (ClaimSyncPacket.Entry entry : ClientClaims.entries()) {
            int chunkX = (int) entry.chunk();
            int chunkZ = (int) (entry.chunk() >> 32);

            double minX = chunkX * 16.0 - camera.x;
            double minZ = chunkZ * 16.0 - camera.z;
            double maxX = minX + 16.0;
            double maxZ = minZ + 16.0;

            float danger = entry.active()
                    ? Math.max(0.0f, Math.min(1.0f,
                    1.0f - entry.hp() / Math.max(1.0f, entry.maxHp())))
                    : 1.0f;
            float red = danger;
            float green = 1.0f - danger;

            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    new AABB(
                            minX, -camera.y, minZ,
                            maxX, 256.0 - camera.y, maxZ
                    ),
                    red, green, 0.0f, 1.0f
            );

            BlockPos marker = BlockPos.of(entry.marker());
            double markerX = marker.getX() + 0.5 - camera.x;
            double markerY = marker.getY() - camera.y;
            double markerZ = marker.getZ() + 0.5 - camera.z;

            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    new AABB(
                            markerX - 0.35, markerY, markerZ - 0.35,
                            markerX + 0.35, markerY + 2.8, markerZ + 0.35
                    ),
                    red, green, 0.0f, 1.0f
            );
        }

        bufferSource.endBatch(RenderType.lines());
    }
}

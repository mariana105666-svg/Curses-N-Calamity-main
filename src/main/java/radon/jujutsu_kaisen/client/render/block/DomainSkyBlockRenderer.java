package radon.jujutsu_kaisen.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import radon.jujutsu_kaisen.block.entity.DomainSkyBlockEntity;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.client.render.domain.DomainRenderDispatcher;

public class DomainSkyBlockRenderer implements BlockEntityRenderer<DomainSkyBlockEntity> {

    @Override
    public void render(
            @NotNull DomainSkyBlockEntity be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ResourceLocation domain = be.getDomain();
        if (domain == null) return;

        poseStack.pushPose();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(renderType(domain));

        renderInsideCube(matrix, vc);

        poseStack.popPose();
    }

    private static void renderInsideCube(Matrix4f m, VertexConsumer v) {
        quad(m, v, 0,0,0, 0,1,0, 0,1,1, 0,0,1);
        quad(m, v, 1,0,1, 1,1,1, 1,1,0, 1,0,0);
        quad(m, v, 0,0,1, 1,0,1, 1,0,0, 0,0,0);
        quad(m, v, 0,1,0, 1,1,0, 1,1,1, 0,1,1);
        quad(m, v, 1,0,0, 1,1,0, 0,1,0, 0,0,0);
        quad(m, v, 0,0,1, 0,1,1, 1,1,1, 1,0,1);
    }

    private static void quad(
            Matrix4f m, VertexConsumer v,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4
    ) {
        v.vertex(m, x1, y1, z1).endVertex();
        v.vertex(m, x2, y2, z2).endVertex();
        v.vertex(m, x3, y3, z3).endVertex();
        v.vertex(m, x4, y4, z4).endVertex();
    }

    private static RenderType renderType(ResourceLocation domain) {
        return JJKRenderTypes.skybox(DomainRenderDispatcher.get(domain));
    }

    @Override
    public boolean shouldRender(@NotNull DomainSkyBlockEntity be, @NotNull Vec3 cameraPos) {
        return Vec3.atCenterOf(be.getBlockPos())
                .closerThan(cameraPos, this.getViewDistance() * 2);
    }
}
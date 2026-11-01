package radon.jujutsu_kaisen.client.render.entity.effect;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.entity.HollowWickerBasketEntity;

public class HollowWickerBasketRenderer extends EntityRenderer<HollowWickerBasketEntity> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/hollow_wicker_basket.png");

            
    public HollowWickerBasketRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(HollowWickerBasketEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, entity.getBbHeight() * 0.9D, 0.0D);

        float radius = HollowWickerBasketEntity.RADIUS;

        final int stacks = 64;
        final int slices = 64;

        final float tileU = 4.0f;
        final float tileV = 4.0f;
        VertexConsumer consumer = buffer.getBuffer(JJKRenderTypes.hollow_wicker_basket(TEXTURE));
        //VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        float[][][] positions = new float[stacks + 1][slices + 1][3];
        float[][][] uvs = new float[stacks + 1][slices + 1][2];

        for (int i = 0; i <= stacks; i++) {
            float phi = (float) Math.PI * i / stacks;
            float v = (i / (float) stacks) * tileV;

            for (int j = 0; j <= slices; j++) {
                float theta = (float) (2.0 * Math.PI * j / slices);
                float u = (j / (float) slices) * tileU;

                float x = (float) (radius * Math.sin(phi) * Math.cos(theta));
                float y = (float) (radius * Math.cos(phi));
                float z = (float) (radius * Math.sin(phi) * Math.sin(theta));

                positions[i][j][0] = x;
                positions[i][j][1] = y;
                positions[i][j][2] = z;

                uvs[i][j][0] = u;
                uvs[i][j][1] = v;
            }
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f posMat = pose.pose();
        Matrix3f normalMat = pose.normal();

        final float alpha = 0.2f; // transparency (1.0 = opaque)

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                float[] v1 = positions[i][j];
                float[] v2 = positions[i][j + 1];
                float[] v3 = positions[i + 1][j + 1];
                float[] v4 = positions[i + 1][j];

                float[] uv1 = uvs[i][j];
                float[] uv2 = uvs[i][j + 1];
                float[] uv3 = uvs[i + 1][j + 1];
                float[] uv4 = uvs[i + 1][j];
addQuadDualSided(consumer, posMat, normalMat,
    v1, uv1, v2, uv2, v3, uv3, v4, uv4,
    alpha, packedLight, radius);
            }
        }

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }
    private static void putVertex(VertexConsumer consumer, Matrix4f mat, Matrix3f normalMat,
                                  float[] pos, float[] uv, float[] normal,
                                  float alpha, int light) {
        consumer.vertex(mat, pos[0], pos[1], pos[2])
                .color(1f, 1f, 1f, alpha)
                .uv(uv[0], uv[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMat, normal[0], normal[1], normal[2])
                .endVertex();
    }

 private static void addQuadDualSided(VertexConsumer consumer, Matrix4f mat, Matrix3f normalMat,
                                     float[] v1, float[] uv1,
                                     float[] v2, float[] uv2,
                                     float[] v3, float[] uv3,
                                     float[] v4, float[] uv4,
                                     float alpha, int light, float radius) {
    float[] n1 = normalize(v1, radius);
    float[] n2 = normalize(v2, radius);
    float[] n3 = normalize(v3, radius);
    float[] n4 = normalize(v4, radius);

    // outside (original radius)
    putVertex(consumer, mat, normalMat, v1, uv1, n1, alpha, light);
    putVertex(consumer, mat, normalMat, v2, uv2, n2, alpha, light);
    putVertex(consumer, mat, normalMat, v3, uv3, n3, alpha, light);
    putVertex(consumer, mat, normalMat, v4, uv4, n4, alpha, light);

    final float shrink = 0.999f;
    float[] v1i = { v1[0] * shrink, v1[1] * shrink, v1[2] * shrink };
    float[] v2i = { v2[0] * shrink, v2[1] * shrink, v2[2] * shrink };
    float[] v3i = { v3[0] * shrink, v3[1] * shrink, v3[2] * shrink };
    float[] v4i = { v4[0] * shrink, v4[1] * shrink, v4[2] * shrink };

    putVertex(consumer, mat, normalMat, v4i, uv4, negate(n4), alpha, light);
    putVertex(consumer, mat, normalMat, v3i, uv3, negate(n3), alpha, light);
    putVertex(consumer, mat, normalMat, v2i, uv2, negate(n2), alpha, light);
    putVertex(consumer, mat, normalMat, v1i, uv1, negate(n1), alpha, light);
}



    private static float[] normalize(float[] pos, float radius) {
        float x = pos[0], y = pos[1], z = pos[2];
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len == 0f) return new float[]{0, 1, 0};
        return new float[]{x / len, y / len, z / len};
    }

    private static float[] negate(float[] v) {
        return new float[]{-v[0], -v[1], -v[2]};
    }

    @Override
    public ResourceLocation getTextureLocation(HollowWickerBasketEntity entity) {
        return TEXTURE;
    }
}

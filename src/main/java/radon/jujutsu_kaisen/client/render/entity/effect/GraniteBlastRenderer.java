package radon.jujutsu_kaisen.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.entity.effect.GraniteBlastEntity;

public class GraniteBlastRenderer<T extends GraniteBlastEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/cursed_energy_bomb.png");

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 32;

    private static final float START_RADIUS = 1.0F;

    private static final float START_FLARE_SHARE = 1.0F / 3.0F;

    private static final int FLARE_STEPS = 10;

    private static final float BEAM_RADIUS = 0.5F;

    private static final float SPIN_SPEED = 18.0F;

    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.30F, 0.55F, 1.0F);

    private boolean clearerView = false;

    private Vector3f color = DEFAULT_COLOR;

    public GraniteBlastRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T pEntity) {
        return TEXTURE;
    }

    private static Vector3f beamColor(GraniteBlastEntity entity, LivingEntity owner) {
        Vector3f fixed = entity.getBeamColor();

        return fixed != null ? fixed : cursedEnergyColor(owner);
    }

    private static Vector3f cursedEnergyColor(LivingEntity owner) {
        Vector3f color = ParticleColors.getCursedEnergyColor(owner);

        if (color.x <= 0.0F && color.y <= 0.0F && color.z <= 0.0F) return DEFAULT_COLOR;

        return color;
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        if (!(pEntity.getOwner() instanceof LivingEntity owner)) return;

        this.clearerView = owner instanceof Player && Minecraft.getInstance().player == owner &&
                Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;

        this.color = beamColor(pEntity, owner);

        double collidePosX = pEntity.prevCollidePosX + (pEntity.collidePosX - pEntity.prevCollidePosX) * pPartialTick;
        double collidePosY = pEntity.prevCollidePosY + (pEntity.collidePosY - pEntity.prevCollidePosY) * pPartialTick;
        double collidePosZ = pEntity.prevCollidePosZ + (pEntity.collidePosZ - pEntity.prevCollidePosZ) * pPartialTick;
        double posX = pEntity.xo + (pEntity.getX() - pEntity.xo) * pPartialTick;
        double posY = pEntity.yo + (pEntity.getY() - pEntity.yo) * pPartialTick;
        double posZ = pEntity.zo + (pEntity.getZ() - pEntity.zo) * pPartialTick;
        float yaw = pEntity.prevYaw + (pEntity.renderYaw - pEntity.prevYaw) * pPartialTick;
        float pitch = pEntity.prevPitch + (pEntity.renderPitch - pEntity.prevPitch) * pPartialTick;

        float length = (float) Math.sqrt(Math.pow(collidePosX - posX, 2)
                + Math.pow(collidePosY - posY, 2) + Math.pow(collidePosZ - posZ, 2));

        int frame = Mth.floor((pEntity.animation - 1 + pPartialTick) * 2);

        if (frame < 0) {
            frame = pEntity.getFrames() * 2;
        }

        float roll = pEntity.shouldSpin() ? (pEntity.getTime() + pPartialTick) * SPIN_SPEED : 0.0F;

        float scale = pEntity.getScale();

        pPoseStack.pushPose();
        pPoseStack.scale(scale, scale, scale);
        pPoseStack.translate(0.0F, pEntity.getBbHeight() / 2.0F, 0.0F);

        VertexConsumer consumer = pBuffer.getBuffer(JJKRenderTypes.glow(this.getTextureLocation(pEntity)));

        if (pEntity.shouldRenderStartFlare()) {
            this.renderStart(frame, pPoseStack, consumer, pPackedLight);
        }

        if (pEntity.getTime() > pEntity.getCharge()) {
            float flare = pEntity.getFlareLength() / scale;
            float nearRadius = BEAM_RADIUS * pEntity.getFlareStart();

            this.renderBeam(length / scale, flare, nearRadius, 180.0F / (float) Math.PI * yaw,
                    180.0F / (float) Math.PI * pitch, roll, frame, pPoseStack, consumer, pPackedLight);

            if (pEntity.shouldRenderEndFlare()) {
                pPoseStack.pushPose();
                pPoseStack.translate((collidePosX - posX) / scale, (collidePosY - posY) / scale,
                        (collidePosZ - posZ) / scale);
                this.renderEnd(frame, pEntity.side, pPoseStack, consumer, pPackedLight);
                pPoseStack.popPose();
            }
        }
        pPoseStack.popPose();
    }

    private void renderFlatQuad(float radius, int frame, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        float minU = 16.0F / TEXTURE_WIDTH * frame;
        float minV = 0.0F;
        float maxU = minU + 16.0F / TEXTURE_WIDTH;
        float maxV = minV + 16.0F / TEXTURE_HEIGHT;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        this.drawVertex(matrix4f, matrix3f, consumer, -radius, -radius, 0.0F, minU, minV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -radius, radius, 0.0F, minU, maxV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, radius, radius, 0.0F, maxU, maxV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, radius, -radius, 0.0F, maxU, minV, packedLight);
    }

    private void renderStart(int frame, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        if (this.clearerView) return;

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        this.renderFlatQuad(START_RADIUS * START_FLARE_SHARE, frame, poseStack, consumer, packedLight);
        poseStack.popPose();
    }

    private void renderEnd(int frame, Direction side, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        this.renderFlatQuad(START_RADIUS, frame, poseStack, consumer, packedLight);
        poseStack.popPose();

        if (side == null) return;

        poseStack.pushPose();
        poseStack.mulPose(side.getRotation().mul(Axis.XP.rotationDegrees(90.0F)));
        poseStack.translate(0.0F, 0.0F, -0.01F);
        this.renderFlatQuad(START_RADIUS, frame, poseStack, consumer, packedLight);
        poseStack.popPose();
    }

    private void drawFace(float length, float flare, float nearRadius, int frame,
                          PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        float minU = 0.0F;
        float minV = 16.0F / TEXTURE_HEIGHT + 1.0F / TEXTURE_HEIGHT * frame;
        float maxU = minU + 20.0F / TEXTURE_WIDTH;
        float maxV = minV + 1.0F / TEXTURE_HEIGHT;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        float offset = this.clearerView ? -1.0F : 0.0F;

        float split = Math.min(flare, length);
        float reach = flare <= 0.0F ? 1.0F : split / flare;
        float splitRadius = flare <= 0.0F ? BEAM_RADIUS : widthAt(reach, nearRadius);
        float splitV = length <= 0.0F ? maxV : Mth.lerp(split / length, minV, maxV);

        if (split > offset) {
            float fromY = offset;
            float fromRadius = nearRadius;
            float fromV = minV;

            for (int step = 1; step <= FLARE_STEPS; step++) {
                float along = (float) step / FLARE_STEPS;

                float toY = Mth.lerp(along, offset, split);
                float toRadius = widthAt(reach * along, nearRadius);
                float toV = Mth.lerp(along, minV, splitV);

                this.drawSegment(matrix4f, matrix3f, consumer, fromY, toY, fromRadius, toRadius,
                        minU, maxU, fromV, toV, packedLight);

                fromY = toY;
                fromRadius = toRadius;
                fromV = toV;
            }
        }

        if (length > split) {
            this.drawSegment(matrix4f, matrix3f, consumer, split, length, splitRadius, BEAM_RADIUS,
                    minU, maxU, splitV, maxV, packedLight);
        }
    }

    private static float widthAt(float progress, float nearRadius) {
        float remaining = 1.0F - Mth.clamp(progress, 0.0F, 1.0F);

        return Mth.lerp(1.0F - remaining * remaining, nearRadius, BEAM_RADIUS);
    }

    private void drawSegment(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer,
                             float fromY, float toY, float fromRadius, float toRadius,
                             float minU, float maxU, float fromV, float toV, int packedLight) {
        this.drawVertex(matrix4f, matrix3f, consumer, -fromRadius, fromY, -fromRadius, minU, fromV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, -toRadius, toY, -toRadius, minU, toV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, toRadius, toY, -toRadius, maxU, toV, packedLight);
        this.drawVertex(matrix4f, matrix3f, consumer, fromRadius, fromY, -fromRadius, maxU, fromV, packedLight);
    }

    private void renderBeam(float length, float flare, float nearRadius, float yaw, float pitch,
                            float roll, int frame, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.XN.rotationDegrees(pitch));

        poseStack.mulPose(Axis.YP.rotationDegrees(roll));

        for (int face = 0; face < 4; face++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(face * 90.0F));
            this.drawFace(length, flare, nearRadius, frame, poseStack, consumer, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void drawVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer,
                            float x, float y, float z, float u, float v, int packedLight) {
        consumer.vertex(matrix4f, x, y, z)
                .color(this.color.x, this.color.y, this.color.z, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}

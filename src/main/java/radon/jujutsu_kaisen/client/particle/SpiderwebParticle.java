package radon.jujutsu_kaisen.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.util.HelperMethods;

public class SpiderwebParticle extends TextureSheetParticle {

    private float roll;
    private static final ResourceLocation TEXTURE = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/dismantle.png");
    //private Vec3 offset;
    private boolean textureRand;

    protected SpiderwebParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.lifetime = 1;
        this.hasPhysics = false;
        this.quadSize = (1.5F + this.random.nextFloat()) * 1.5F;
        this.roll = (this.random.nextFloat() - 0.5F) * 360.0F;
        //this.offset = this.getPos();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    // @Override
    // public void tick() {
    //     super.tick();
    //     this.roll += (this.random.nextFloat() - 0.5F) * 20.0F;
    // }

        @Override
    public void tick() {
        super.tick();
        this.roll = (this.random.nextFloat() - 0.5F) * 360.0F;
        //this.offset =  this.getPos();
         this.quadSize = (1.5F + this.random.nextFloat()) * 1.5F;
    }


    @Override
    public void render(@NotNull VertexConsumer buffer,
                       @NotNull Camera camera,
                       float partialTicks) {

        Minecraft mc = Minecraft.getInstance();
        PoseStack ps = new PoseStack();

        Vec3 camPos = camera.getPosition();

        ps.pushPose();
        ps.translate(this.x - camPos.x, this.y - camPos.y, this.z - camPos.z);
        ps.mulPose(Axis.YN.rotationDegrees(camera.getYRot()));
        ps.mulPose(Axis.XP.rotationDegrees(camera.getXRot() - 90.0F));
        ps.mulPose(Axis.YP.rotationDegrees(this.roll));

        ps.scale(1.0F, 1.0F, 0.2F);
        
        RenderType type = RenderType.entityTranslucentEmissive(TEXTURE);
        


        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(type);
        Matrix4f pose = ps.last().pose();

          consumer.vertex(pose, -this.quadSize, 0.0F, -1.0F)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_SKY)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose, -this.quadSize, 0.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_SKY)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose, this.quadSize, 0.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_SKY)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose, this.quadSize, 0.0F, -1.0F)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_SKY)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        mc.renderBuffers().bufferSource().endBatch(type);

        ps.popPose();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return JJKParticleRenderTypes.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet ignored) {
        }

        @Override
        public SpiderwebParticle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                               double x, double y, double z,
                                               double xSpeed, double ySpeed, double zSpeed) {
            return new SpiderwebParticle(level, x, y, z);
        }
    }
}

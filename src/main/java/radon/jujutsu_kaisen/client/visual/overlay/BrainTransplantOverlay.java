package radon.jujutsu_kaisen.client.visual.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.base.IOverlay;

public class BrainTransplantOverlay implements IOverlay {
    private static final RenderType BRAIN_TRANSPLANT = JJKRenderTypes.eyes(new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/overlay/brain_transplant.png"));

    @Override
    public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
        return data.techniques.contains(CursedTechnique.BRAIN_TRANSPLANT);
    }

    @Override
    public <T extends LivingEntity> void render(T entity, ClientVisualHandler.ClientData data, ResourceLocation texture, EntityModel<T> model, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(BRAIN_TRANSPLANT);
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}





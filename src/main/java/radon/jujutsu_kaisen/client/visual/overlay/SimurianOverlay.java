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
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.base.IOverlay;

public class SimurianOverlay implements IOverlay {
    private static final RenderType SIMURIAN = JJKRenderTypes.eyes(new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/overlay/simurian.png"));

    @Override
    public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
        return data.traits.contains(Trait.SIMURIAN);
    }

    @Override
    public <T extends LivingEntity> void render(T entity, ClientVisualHandler.ClientData data, ResourceLocation texture, EntityModel<T> model, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(SIMURIAN);
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}

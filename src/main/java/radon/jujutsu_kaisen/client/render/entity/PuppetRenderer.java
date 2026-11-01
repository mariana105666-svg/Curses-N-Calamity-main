package radon.jujutsu_kaisen.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.entity.PuppetEntity;

public class PuppetRenderer extends HumanoidMobRenderer<PuppetEntity, PlayerModel<PuppetEntity>> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/puppet.png");

    public static final float SCALE = 2.2F / 1.8F;

    public PuppetRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 0.5F * SCALE);

        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager()));
    }

    @Override
    protected void scale(@NotNull PuppetEntity pEntity, @NotNull PoseStack pPoseStack, float pPartialTickTime) {
        pPoseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PuppetEntity pEntity) {
        return TEXTURE;
    }
}

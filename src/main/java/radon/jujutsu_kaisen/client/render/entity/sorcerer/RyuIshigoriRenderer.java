package radon.jujutsu_kaisen.client.render.entity.sorcerer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.layer.JJKOverlayLayer;
import radon.jujutsu_kaisen.client.model.entity.RyuIshigoriModel;
import radon.jujutsu_kaisen.entity.sorcerer.RyuIshigoriEntity;

public class RyuIshigoriRenderer extends HumanoidMobRenderer<RyuIshigoriEntity, RyuIshigoriModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/ryu_ishigori.png");

    public RyuIshigoriRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RyuIshigoriModel(pContext.bakeLayer(RyuIshigoriModel.LAYER)), 0.5F);

        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(pContext.bakeLayer(RyuIshigoriModel.INNER_LAYER)),
                new HumanoidModel<>(pContext.bakeLayer(RyuIshigoriModel.OUTER_LAYER)), pContext.getModelManager()));
        this.addLayer(new JJKOverlayLayer<>(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull RyuIshigoriEntity pEntity) {
        return TEXTURE;
    }
}

package radon.jujutsu_kaisen.client.render.domain;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import radon.jujutsu_kaisen.JujutsuKaisen;

public class DefaultDomainRenderer extends DomainRenderer {
        private static final ResourceLocation TEXTURE = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/domain/domain_default.png");

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
        //return MissingTextureAtlasSprite.getLocation();
    }
}

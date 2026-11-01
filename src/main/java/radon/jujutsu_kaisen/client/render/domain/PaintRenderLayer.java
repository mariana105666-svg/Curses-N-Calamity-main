package radon.jujutsu_kaisen.client.render.domain;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.util.RenderUtil;

public class PaintRenderLayer extends DomainRenderLayer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/domain/paint.png");
    private static final int DELAY = 60;

    @Override
    public boolean shouldRender(int time) {
        //System.out.println(time);
        return time >= DELAY;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}

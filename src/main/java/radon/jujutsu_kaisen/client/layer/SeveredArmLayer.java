package radon.jujutsu_kaisen.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.client.limb.VoxelArm;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.limb.Limb;
import radon.jujutsu_kaisen.limb.Wound;

public class SeveredArmLayer<T extends AbstractClientPlayer, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    public SeveredArmLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(@NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int light, @NotNull T player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ClientVisualHandler.ClientData data = ClientVisualHandler.get(player);

        if (data == null) return;

        M model = this.getParentModel();

        for (Limb limb : Limb.values()) {
            if (limb.isExtra()) continue;

            float remaining = data.limbs[limb.ordinal()];

            if (remaining >= 1.0F || remaining <= 0.0F) continue;

            ModelPart arm = inner(model, limb);
            ModelPart sleeve = outer(model, limb);

            pose.pushPose();
            arm.translateAndRotate(pose);

            VoxelArm.render(pose, buffers, light, this.getTextureLocation(player), arm, sleeve, remaining,
                    data.cuts[limb.ordinal()], data.wounds[limb.ordinal()] == Wound.RAGGED, seed(player, limb));

            pose.popPose();
        }
    }

    public static ModelPart inner(PlayerModel<?> model, Limb limb) {
        return switch (limb) {
            case RIGHT_ARM, RIGHT_EXTRA_ARM -> model.rightArm;
            case LEFT_ARM, LEFT_EXTRA_ARM -> model.leftArm;
            case RIGHT_LEG -> model.rightLeg;
            case LEFT_LEG -> model.leftLeg;
        };
    }

    public static ModelPart outer(PlayerModel<?> model, Limb limb) {
        return switch (limb) {
            case RIGHT_ARM, RIGHT_EXTRA_ARM -> model.rightSleeve;
            case LEFT_ARM, LEFT_EXTRA_ARM -> model.leftSleeve;
            case RIGHT_LEG -> model.rightPants;
            case LEFT_LEG -> model.leftPants;
        };
    }

    public static long seed(LivingEntity player, Limb limb) {
        return player.getUUID().getLeastSignificantBits() ^ (limb.ordinal() * 0x9E3779B97F4A7C15L);
    }
}

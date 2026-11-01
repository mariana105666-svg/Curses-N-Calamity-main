package radon.jujutsu_kaisen.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import radon.jujutsu_kaisen.client.layer.SeveredArmLayer;
import radon.jujutsu_kaisen.client.limb.VoxelArm;
import radon.jujutsu_kaisen.limb.Limb;
import radon.jujutsu_kaisen.limb.Wound;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.client.render.entity.PuppetRenderer;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Shadow protected abstract void setModelProperties(AbstractClientPlayer player);

    @Inject(
            method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jujutsu_kaisen$rightStump(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo ci) {
        this.jujutsu_kaisen$stump(pose, buffers, light, player, Limb.RIGHT_ARM, ci);
    }

    @Inject(
            method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jujutsu_kaisen$leftStump(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo ci) {
        this.jujutsu_kaisen$stump(pose, buffers, light, player, Limb.LEFT_ARM, ci);
    }

    private void jujutsu_kaisen$stump(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, Limb limb, CallbackInfo ci) {
        ClientVisualHandler.ClientData data = ClientVisualHandler.get(player);

        if (data == null) return;

        float remaining = data.limbs[limb.ordinal()];

        if (remaining >= 1.0F) return;

        ci.cancel();

        PlayerRenderer self = (PlayerRenderer) (Object) this;
        PlayerModel<AbstractClientPlayer> model = self.getModel();

        this.setModelProperties(player);
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        ModelPart arm = limb == Limb.RIGHT_ARM ? model.rightArm : model.leftArm;
        ModelPart sleeve = limb == Limb.RIGHT_ARM ? model.rightSleeve : model.leftSleeve;

        arm.xRot = 0.0F;

        pose.pushPose();
        arm.translateAndRotate(pose);

        VoxelArm.render(pose, buffers, light, self.getTextureLocation(player), arm, sleeve, remaining,
                data.cuts[limb.ordinal()], data.wounds[limb.ordinal()] == Wound.RAGGED,
                SeveredArmLayer.seed(player, limb));

        pose.popPose();
    }

    @Inject(
            method = "scale(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            at = @At("TAIL")
    )
    private void jujutsu_kaisen$puppetScale(AbstractClientPlayer player, PoseStack pose, float partialTick, CallbackInfo ci) {
        ClientVisualHandler.ClientData data = ClientVisualHandler.get(player);

        if (data == null || data.toggled == null) return;

        for (Ability puppet : JJKAbilities.getPuppetAbilities()) {
            if (!data.toggled.contains(puppet)) continue;

            float scale = radon.jujutsu_kaisen.client.render.entity.PuppetRenderer.SCALE;

            pose.scale(scale, scale, scale);
            return;
        }
    }

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jujutsu_kaisen$puppetSkin(AbstractClientPlayer player, CallbackInfoReturnable<ResourceLocation> cir) {
        ClientVisualHandler.ClientData data = ClientVisualHandler.get(player);

        if (data == null || data.toggled == null) return;

        for (Ability puppet : JJKAbilities.getPuppetAbilities()) {
            if (data.toggled.contains(puppet)) {
                cir.setReturnValue(PuppetRenderer.TEXTURE);
                return;
            }
        }
    }
}

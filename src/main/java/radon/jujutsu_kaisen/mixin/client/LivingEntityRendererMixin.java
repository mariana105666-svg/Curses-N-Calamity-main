package radon.jujutsu_kaisen.mixin.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import radon.jujutsu_kaisen.ability.base.ActivePose;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.MixinData;
import radon.jujutsu_kaisen.client.PoseApplier;
import radon.jujutsu_kaisen.client.PoseLimb;
import radon.jujutsu_kaisen.client.PoseResolver;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;speed(F)F"))
    public float speed(WalkAnimationState instance, float pPartialTick) {
        return MixinData.isCustomWalkAnimation ? MixinData.walkAnimationSpeed : instance.speed(pPartialTick);
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;position(F)F"))
    public float position(WalkAnimationState instance, float pPartialTick) {
        return MixinData.isCustomWalkAnimation ? MixinData.walkAnimationPosition : instance.position(pPartialTick);
    }


    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FF" +
                "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                "Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/client/model/EntityModel;" +
                "renderToBuffer(" +
                "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                "Lcom/mojang/blaze3d/vertex/VertexConsumer;" +
                "IIFFFF)V"
        )
    )
    private void jjk$applyFinalPose(
        T entity,
        float yaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        if (!(((LivingEntityRenderer<?, ?>)(Object)this).getModel() instanceof HumanoidModel<?> model))
            return;

        ClientVisualHandler.ClientData data = ClientVisualHandler.get(entity);
        if (data == null) return;

        //ClientActivePose pose = JJKPoseHandler.resolve(entity, data);
        boolean poseHappened = false;


        for (PoseLimb limb : PoseLimb.values()) {
            ActivePose target =
                PoseResolver.resolveForLimb(data.activePoses, limb
                );
            if (target != null) {
                System.out.println("applying transformation");
                PoseApplier.applyTransform(model, limb, target);
                poseHappened = true;
            }
        }
        if (poseHappened && !data.traits.contains(Trait.PERFECT_BODY)) {

            System.out.println("got pose");
            // this.leftArmPose  = HumanoidModel.ArmPose.EMPTY;
            // this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            //pose.pose.apply(model, data, pose);
        }

        if (model instanceof PlayerModel<?> player) {
            player.rightSleeve.copyFrom(player.rightArm);
            player.leftSleeve.copyFrom(player.leftArm);
        }

    }
}

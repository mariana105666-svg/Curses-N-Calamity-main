package radon.jujutsu_kaisen.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.overlay.PerfectBodyOverlay;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart rightArm;

    @Shadow public HumanoidModel.ArmPose rightArmPose;

    @Shadow public HumanoidModel.ArmPose leftArmPose;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))

    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {

        ClientVisualHandler.ClientData data = ClientVisualHandler.get(pEntity);

        if (data == null) return;

        if (!(pEntity instanceof LivingEntity)) return;
        //         System.out.println("entity name " + pEntity.getName().toString());
        // IJJKPose pose = PoseHandler.resolve(pEntity, data);
        // if (pose != null && !data.traits.contains(Trait.PERFECT_BODY)) {
        //     System.out.println("ok 1");
        //     // this.leftArmPose  = HumanoidModel.ArmPose.EMPTY;
        //     // this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        //     pose.apply((HumanoidModel<?>)(Object)this, data);
        // }
        //LivingEntity living = (LivingEntity) pEntity;

       // ISorcererData cap = living.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        //if (cap == null) return;
        //if (cap.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get()) && !cap.hasTrait(Trait.PERFECT_BODY)) {
        //  this.rightArm.xRot = (float) Math.toRadians(-75.0F);
        //     this.leftArm.xRot  = (float) Math.toRadians(-75.0F);

        //     this.rightArm.yRot = (float) Math.toRadians(-40.0F);
        //     this.leftArm.yRot  = (float) Math.toRadians(40.0F);

        //     this.rightArm.zRot = (float) Math.toRadians(15.0F);
        //     this.leftArm.zRot  = (float) Math.toRadians(-15.0F);
        //     return;
        // }
    //if (!cap.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get()) && cap.hasTrait(Trait.PERFECT_BODY)) {
        if (PerfectBodyOverlay.shouldRenderExtraArms(pEntity, data)) {
            if (this.rightArmPose == HumanoidModel.ArmPose.EMPTY || this.rightArmPose == HumanoidModel.ArmPose.ITEM) {
                this.rightArm.xRot += this.rightArm.xRot * 0.5F - ((float) Math.PI * 0.1F);
                this.rightArm.zRot -= this.rightArm.zRot * 0.5F - ((float) Math.PI * 0.1F);
            }
            if (this.leftArmPose == HumanoidModel.ArmPose.EMPTY || this.leftArmPose == HumanoidModel.ArmPose.ITEM) {
                this.leftArm.xRot += this.leftArm.xRot * 0.5F - ((float) Math.PI * 0.1F);
                this.leftArm.zRot += this.leftArm.zRot * 0.5F - ((float) Math.PI * 0.1F);
            }
        }
    }
    //}
}

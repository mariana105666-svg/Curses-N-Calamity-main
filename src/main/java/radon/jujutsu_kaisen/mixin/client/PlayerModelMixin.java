package radon.jujutsu_kaisen.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.limb.Limb;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart leftPants;

    private PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void jujutsu_kaisen$hideSeveredArms(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                                 float netHeadYaw, float headPitch, CallbackInfo ci) {
        ClientVisualHandler.ClientData data = ClientVisualHandler.get(entity);

        if (data == null) return;

        if (data.limbs[Limb.RIGHT_ARM.ordinal()] < 1.0F) {
            this.rightArm.visible = false;
            this.rightSleeve.visible = false;
        }

        if (data.limbs[Limb.LEFT_ARM.ordinal()] < 1.0F) {
            this.leftArm.visible = false;
            this.leftSleeve.visible = false;
        }

        if (data.limbs[Limb.RIGHT_LEG.ordinal()] < 1.0F) {
            this.rightLeg.visible = false;
            this.rightPants.visible = false;
        }

        if (data.limbs[Limb.LEFT_LEG.ordinal()] < 1.0F) {
            this.leftLeg.visible = false;
            this.leftPants.visible = false;
        }
    }
}

package radon.jujutsu_kaisen.client.visual.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.ActivePose;

import radon.jujutsu_kaisen.ability.base.ITransformation;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.PoseApplier;
import radon.jujutsu_kaisen.client.PoseLimb;
import radon.jujutsu_kaisen.client.PoseResolver;
import radon.jujutsu_kaisen.client.PoseTarget;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.visual.PerfectBodyVisual;
import radon.jujutsu_kaisen.client.visual.base.IOverlay;
import radon.jujutsu_kaisen.client.layer.SeveredArmLayer;
import radon.jujutsu_kaisen.client.limb.VoxelArm;
import radon.jujutsu_kaisen.limb.Limb;
import radon.jujutsu_kaisen.limb.Wound;
import net.minecraft.client.model.geom.ModelPart;
import radon.jujutsu_kaisen.mixin.client.ILivingEntityRendererAccessor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Map;
import java.util.Optional;

public class PerfectBodyOverlay implements IOverlay {
    private static boolean severed(ClientVisualHandler.ClientData data, Limb limb) {
        return data.limbs[limb.ordinal()] < 1.0F;
    }

    private static void whole(ClientVisualHandler.ClientData data, Limb limb, ModelPart arm, ModelPart sleeve,
                              PoseStack poseStack, VertexConsumer skin, int packedLight) {
        sleeve.copyFrom(arm);

        if (data.limbs[limb.ordinal()] < 1.0F) return;

        arm.render(poseStack, skin, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private static void stump(LivingEntity entity, ClientVisualHandler.ClientData data, Limb limb,
                              ModelPart arm, ModelPart sleeve, PoseStack poseStack, MultiBufferSource buffer,
                              ResourceLocation texture, int packedLight) {
        float remaining = data.limbs[limb.ordinal()];

        if (remaining >= 1.0F || remaining <= 0.0F) return;

        poseStack.pushPose();
        arm.translateAndRotate(poseStack);

        VoxelArm.render(poseStack, buffer, packedLight, texture, arm, sleeve, remaining,
                data.cuts[limb.ordinal()], data.wounds[limb.ordinal()] == Wound.RAGGED,
                SeveredArmLayer.seed(entity, limb));

        poseStack.popPose();
    }

    private static <T extends LivingEntity> void renderItems(T entity, ClientVisualHandler.ClientData data, HumanoidModel<T> model, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(entity);

        if (!optional.isPresent()) return;

        ICuriosItemHandler inventory = optional.resolve().orElseThrow();

        Optional<SlotResult> rightHand = inventory.findCurio("right_hand", 0);
        Optional<SlotResult> leftHand = inventory.findCurio("left_hand", 0);

        ItemStack right = rightHand.isPresent() && !severed(data, Limb.RIGHT_EXTRA_ARM) ? rightHand.get().stack() : ItemStack.EMPTY;
        ItemStack left = leftHand.isPresent() && !severed(data, Limb.LEFT_EXTRA_ARM) ? leftHand.get().stack() : ItemStack.EMPTY;

        if (right.isEmpty() && left.isEmpty()) return;

        poseStack.pushPose();

        if (model.young) {
            poseStack.translate(0.0F, 0.75F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }

        if (!right.isEmpty()) {
            poseStack.pushPose();
            model.translateToHand(HumanoidArm.RIGHT, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.translate((float) 1 / 16.0F, 0.125F, -0.625F);
            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(entity, right, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        if (!left.isEmpty()) {
            poseStack.pushPose();
            model.translateToHand(HumanoidArm.LEFT, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.translate((float) -1 / 16.0F, 0.125F, -0.625F);
            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(entity, left, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static boolean shouldRenderExtraArms(LivingEntity entity, ClientVisualHandler.ClientData data) {
        if (Minecraft.getInstance().player == entity && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) return false;

        for (Ability ability : data.toggled) {
            if (!(ability instanceof ITransformation transformation)) continue;
            if (transformation.getBodyPart() != ITransformation.Part.BODY || !transformation.isReplacement()) continue;
            return false;
        }
        for (Ability puppet : JJKAbilities.getPuppetAbilities()) {
            if (data.toggled.contains(puppet)) return false;
        }
        return data.traits.contains(Trait.PERFECT_BODY);
    }

    @Override
    public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
        return shouldRenderExtraArms(entity, data);
    }

    @Override
    public <T extends LivingEntity> void render(T entity, ClientVisualHandler.ClientData data, ResourceLocation texture, EntityModel<T> model, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        VertexConsumer overlay = buffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(JujutsuKaisen.MOD_ID,
                String.format("textures/overlay/mouth_%d.png", data.mouth + 1))));
        model.renderToBuffer(poseStack, overlay, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        if (!(model instanceof PlayerModel<T> humanoid)) return;

        VertexConsumer skin = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.2F, 0.0F);

        humanoid.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        humanoid.leftArmPose = HumanoidModel.ArmPose.EMPTY;

        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());

        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float f2 = f1 - f;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity living) {
            f = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
            f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);

            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;

            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }
            f2 = f1 - f;
        }

        float f6 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
            f6 *= -1.0F;
            f2 *= -1.0F;
        }

        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> renderer))
            return;

        float f7 = ((ILivingEntityRendererAccessor<?, ?>) renderer).invokeGetBob(entity, partialTicks);
        float f8 = 0.0F;
        float f5 = 0.0F;

        if (!shouldSit && entity.isAlive()) {
            f8 = entity.walkAnimation.speed(partialTicks);
            f5 = entity.walkAnimation.position(partialTicks);

            if (entity.isBaby()) {
                f5 *= 3.0F;
            }
            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }
            humanoid.setupAnim(entity, f5, f8, f7, f2, f6);
        boolean poseHappened = false;


        for (PoseLimb limb : PoseLimb.values()) {
            ActivePose target =
                PoseResolver.resolveForLimb(data.activePoses, limb
                );
            if (target != null) {
                PoseApplier.applyTransform(humanoid, limb, target);
                poseHappened = true;
            }
        }


    
        //ActivePose pose = PoseResolver.resolve(entity, data);
        if (poseHappened == true) {
            // this.leftArmPose  = HumanoidModel.ArmPose.EMPTY;
            // this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            //pose.pose.apply(humanoid, data, pose);
        } else {
            if (model.attackTime <= 0) {
                humanoid.rightArm.xRot -= humanoid.rightArm.xRot * 0.5F - ((float) Math.PI * 0.1F);
            }
            humanoid.rightArm.zRot += humanoid.rightArm.zRot * 0.5F - ((float) Math.PI * 0.125F);

            if (model.attackTime <= 0) {
                humanoid.leftArm.xRot -= humanoid.leftArm.xRot * 0.5F - ((float) Math.PI * 0.1F);
            }
            humanoid.leftArm.zRot -= humanoid.leftArm.zRot * 0.5F - ((float) Math.PI * 0.025F);
        }
     
        whole(data, Limb.RIGHT_EXTRA_ARM, humanoid.rightArm, humanoid.rightSleeve, poseStack, skin, packedLight);
        whole(data, Limb.LEFT_EXTRA_ARM, humanoid.leftArm, humanoid.leftSleeve, poseStack, skin, packedLight);

        stump(entity, data, Limb.RIGHT_EXTRA_ARM, humanoid.rightArm, humanoid.rightSleeve,
                poseStack, buffer, texture, packedLight);
        stump(entity, data, Limb.LEFT_EXTRA_ARM, humanoid.leftArm, humanoid.leftSleeve,
                poseStack, buffer, texture, packedLight);

        renderItems(entity, data, humanoid, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}

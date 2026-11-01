package radon.jujutsu_kaisen.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import radon.jujutsu_kaisen.ability.base.ActivePose;

public final class PoseApplier {

    private PoseApplier() {}

    public static void applyTransform(
        HumanoidModel<?> model,
        PoseLimb limb,
        ActivePose pose
    ) {
        ModelPart part = getPart(model, limb);
        if (part == null) return;
        System.out.println("getting part");
        float progress =  ((float)pose.getTicksLeft()/(float)pose.pose.defaultDuration());
        float inverseProgress = 1.0F - progress;
        System.out.println(progress);
        PoseKeyframe p = pose.pose.definition().getKeyframeForProgress(inverseProgress);
        PoseKeyframe pPrev = pose.pose.definition().getPrevKeyframeForProgress(inverseProgress);
        PoseTarget tempTarget = p.targets.get(limb);
        PoseTarget pTarget = null;
        if (pPrev != null) {
            System.out.println("last was found");
            pTarget = pPrev.targets.get(limb);
        }
        else {
            System.out.println("no pose target");
            pTarget = new PoseTarget();
            pTarget.withPosition(part.x, part.y, part.z);
            pTarget.withRotation(part.xRot, part.yRot, part.zRot);
        }

        System.out.println(pTarget != null);
        System.out.println(tempTarget != null);
        PoseTarget target = pTarget.applyTo(tempTarget, progress);
        part.xRot = target.xRot;
        part.yRot = target.yRot;
        part.zRot = target.zRot;

        part.x = target.x;
        part.y = target.y;
        part.z = target.z;
        
    }

    private static ModelPart getPart(HumanoidModel<?> model, PoseLimb limb) {
        return switch (limb) {
            case HEAD -> model.head;
            case BODY -> model.body;
            case RIGHT_ARM -> model.rightArm;
            case LEFT_ARM -> model.leftArm;
            case RIGHT_LEG -> model.rightLeg;
            case LEFT_LEG -> model.leftLeg;
        };
    }
}

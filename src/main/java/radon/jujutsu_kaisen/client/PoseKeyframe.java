package radon.jujutsu_kaisen.client;

import java.util.EnumMap;
public class PoseKeyframe {
    public final float startProgress;
    public final float endProgress;
    private PoseEasing easing = PoseEasing.LINEAR;
    public final EnumMap<PoseLimb, PoseTarget> targets =
        new EnumMap<>(PoseLimb.class);

    public PoseKeyframe(float startProgress, float endProgress) {
        this.startProgress = startProgress;
        this.endProgress = endProgress;
    }

    public PoseKeyframe rotate(PoseLimb limb, float xRot, float yRot, float zRot) {
        targets
            .computeIfAbsent(limb, l -> new PoseTarget())
            .withRotation(
                (float)Math.toRadians(-xRot),
                (float)Math.toRadians(-yRot),
                (float)Math.toRadians(zRot)
            );
        return this;
    }

    public PoseKeyframe easing(PoseEasing easing) {
        this.easing = easing;
        return this;
    }

    public PoseEasing getEasing() {
        return this.easing;
    }

    public PoseKeyframe position(PoseLimb limb, float x, float y, float z) {
        targets
            .computeIfAbsent(limb, l -> new PoseTarget())
            .withPosition(x, y, z);
        return this;
    }

    public boolean affects(PoseLimb limb) {
        return targets.containsKey(limb);
    }


    // public float easedProgress(float progress) {
    //     float t = Mth.clamp(
    //         (progress - startProgress) / (endProgress - startProgress),
    //     );
    //     return JJKPoses.getEasing(easing, t);
    // }
}

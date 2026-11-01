package radon.jujutsu_kaisen.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import radon.jujutsu_kaisen.JujutsuKaisen;

public final class JJKPoses {

    private static final Map<ResourceLocation, JJKPose> REGISTRY = new HashMap<>();

    public static JJKPose register(
        ResourceLocation id,
        PoseDefinition definition,
        int priority,
        int duration,
        boolean autocancel
    ) {
        JJKPose pose = new JJKPose(id, definition, priority, duration, autocancel);
        REGISTRY.put(id, pose);
        return pose;
    }

    public static JJKPose get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Collection<JJKPose> values() {
        return REGISTRY.values();
    }


    private static PoseDefinition define(PoseKeyframe... frames) {
        return new PoseDefinition(List.of(frames));
    }

    public static float applyEasing(PoseEasing easing, float t) {
        if (easing == null) easing = PoseEasing.LINEAR;
        return Mth.clamp(easing.apply(Mth.clamp(t, 0f, 1f)), 0f, 1f);
    }

    public static final JJKPose HOLLOW_WICKER_BASKET =
        register(
            new ResourceLocation(JujutsuKaisen.MOD_ID, "hollow_wicker_basket"),
            define(
                new PoseKeyframe(0.0f, 1.0f)
                    .rotate(PoseLimb.RIGHT_ARM, 75, 40, -15)
                    .rotate(PoseLimb.LEFT_ARM,  75, -40, 15)
                    .easing(PoseEasing.EASE_OUT)
            ),
            10,
            5,
            false
        );

    public static final JJKPose CHARGE_BLAST =
        register(
            new ResourceLocation(JujutsuKaisen.MOD_ID, "charge_blast"),
            define(
                new PoseKeyframe(0.0f, 1.0f)
                    .rotate(PoseLimb.RIGHT_ARM, -95, 0, 5)
                    .easing(PoseEasing.EASE_OUT)
            ),
            10,
            12,
            true
        );

    public static final JJKPose MAHORAGA_WHEEL =
        register(
            new ResourceLocation(JujutsuKaisen.MOD_ID, "mahoraga_wheel"),
            define(
                new PoseKeyframe(0.0f, 0.5f)
                    .rotate(PoseLimb.RIGHT_ARM, 75, 40, -15)
                    .rotate(PoseLimb.LEFT_ARM,  75, -40, 15)
                    .easing(PoseEasing.EASE_OUT),

                new PoseKeyframe(0.5f, 1.0f)
                    .rotate(PoseLimb.RIGHT_ARM, 81.59f, -22.85f, -190.36f)
                    .rotate(PoseLimb.LEFT_ARM,  69.96f, -37.6f,   14.64f)
                    .position(PoseLimb.RIGHT_ARM, 0, -2, -3)
                    .position(PoseLimb.LEFT_ARM,  0,  0, -1)
                    .easing(PoseEasing.EASE_OUT)
            ),
            0,
            20,
            true
        );

    private JJKPoses() {}
}

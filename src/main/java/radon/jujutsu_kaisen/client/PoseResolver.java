package radon.jujutsu_kaisen.client;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import radon.jujutsu_kaisen.ability.base.ActivePose;

public final class PoseResolver {

    private PoseResolver() {}

    public static ActivePose resolveForLimb(
        Map<ResourceLocation, ActivePose> activePoses,
        PoseLimb limb
    ) {
        ActivePose best = null;
        int bestPriority = Integer.MIN_VALUE;
                
        for (ActivePose active : activePoses.values()) {
            JJKPose pose = active.pose;
            System.out.println("limb 2");
            if (pose == null) continue;
            System.out.println("not limb");
            if (!pose.definition().affects(limb)) continue;
            System.out.println("affected limb");
            int priority = pose.priority();
            if (priority > bestPriority) {
                bestPriority = priority;
                best = active;
            }
            System.out.println(
    pose.id() + " affects " + limb + ": " +
    pose.definition().affects(limb)
);
        }

        return best;
    }
}

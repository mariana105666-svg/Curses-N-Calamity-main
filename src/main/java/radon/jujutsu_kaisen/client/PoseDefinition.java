package radon.jujutsu_kaisen.client;

import java.util.ArrayList;
import java.util.List;

public final class PoseDefinition {
    private List<PoseKeyframe> keyframes = new ArrayList<>();

    public PoseDefinition(List<PoseKeyframe> keyframes ) {
        this.keyframes = keyframes;
    }

    public PoseDefinition addKeyframe(PoseKeyframe frame) {
        keyframes.add(frame);
        return this;
    }

    public PoseKeyframe getKeyframeForProgress(float progress) {
        for (PoseKeyframe kf : keyframes) {
            if (progress >= kf.startProgress && progress <= kf.endProgress) {
                return kf;
            }
        }
        return keyframes.isEmpty() ? null : keyframes.get(keyframes.size() - 1);
    }

    
    public PoseKeyframe getPrevKeyframeForProgress(float progress) {
        PoseKeyframe prev = null;
        float bestEnd = -Float.MAX_VALUE;

        for (PoseKeyframe kf : keyframes) {
            if (kf.endProgress < progress && kf.endProgress > bestEnd) {
                bestEnd = kf.endProgress;
                prev = kf;
            }
        }

        return prev;
    }




    public boolean affects(PoseLimb limb) {
        for (PoseKeyframe frame : keyframes) {
            if (frame.affects(limb)) {
                return true;
            }
        }
        return false;
    }

}


package radon.jujutsu_kaisen.ability.base;

import net.minecraft.world.InteractionHand;
import radon.jujutsu_kaisen.client.JJKPose;
import radon.jujutsu_kaisen.client.JJKPoses;

public class ActivePose {
    public final JJKPose pose;
    public final InteractionHand hand;
    public int ticksLeft;
    public int ghostTicks;

    public ActivePose(JJKPose pose, InteractionHand hand, int duration) {
        this.pose = pose;
        this.hand = hand;
        this.ticksLeft = duration;
        this.ghostTicks = -1;
    }

    public ActivePose(JJKPose pose, InteractionHand hand, int duration, int ghostTicks) {
        this.pose = pose;
        this.hand = hand;
        this.ticksLeft = duration;
        this.ghostTicks = ghostTicks;
    }


    public int getTicksLeft() {
        return this.ticksLeft;
    }

    public boolean tick() {
        if (ticksLeft < 0) return true;
        if (ticksLeft > 0) {
            --ticksLeft;
        }
        else if (ticksLeft == 0) {
            if (ghostTicks == -1 && this.pose.autocancel()) {
                this.ghostTicks = this.pose.defaultDuration();
            }
            else if (ghostTicks > 0) {
                --this.ghostTicks;
            }
        }
        return ticksLeft > 0 || this.ghostTicks != 0;
    }
}

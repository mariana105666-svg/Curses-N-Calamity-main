package radon.jujutsu_kaisen.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.entity.PuppetEntity;

public class PuppetFollowGoal extends FollowOwnerGoal {
    private final PuppetEntity puppet;

    public PuppetFollowGoal(PuppetEntity puppet, double speed, float start, float stop) {
        super(puppet, speed, start, stop, false);

        this.puppet = puppet;
    }

    private boolean allowed() {
        if (this.puppet.isWorking()) return false;

        LivingEntity target = this.puppet.getTarget();

        if (target != null && target.isAlive()) return false;

        LivingEntity owner = this.puppet.getOwner();

        return owner != null && JJKAbilities.hasToggled(owner, JJKAbilities.FOLLOW.get());
    }

    @Override
    public boolean canUse() {
        return this.allowed() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.allowed() && super.canContinueToUse();
    }
}

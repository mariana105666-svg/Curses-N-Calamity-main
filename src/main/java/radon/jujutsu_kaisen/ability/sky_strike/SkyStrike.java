package radon.jujutsu_kaisen.ability.sky_strike;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.curse.ZombaCurseEntity;
import radon.jujutsu_kaisen.entity.effect.SkyStrikeEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class SkyStrike extends Ability {
    public static final double RANGE = 50.0D;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return  target != null && this.getTarget(owner) == target;
        //HelperMethods.RANDOM.nextInt(3) == 0 &&
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private @Nullable LivingEntity getTarget(LivingEntity owner) {
        if (RotationUtil.getLookAtHit(owner, RANGE) instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            if (!owner.canAttack(target)) return null;
            
            return target;
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner instanceof Player) {
            owner.swing(InteractionHand.MAIN_HAND);
        }
        

        LivingEntity target = this.getTarget(owner);

        if (target == null) return;

        SkyStrikeEntity strike = new SkyStrikeEntity(owner, this.getPower(owner), target.position());
        owner.level().addFreshEntity(strike);
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        LivingEntity target = this.getTarget(owner);

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public int getCooldown() {
        return 2 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 100.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

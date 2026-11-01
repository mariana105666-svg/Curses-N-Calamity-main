package radon.jujutsu_kaisen.ability.base;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.util.RotationUtil;

public interface IRMBAttack extends IRMBAble {
    double MELEE_RANGE = 5.0D;

    void perform(LivingEntity owner, LivingEntity target);

    default double getRange() {
        return MELEE_RANGE;
    }
    
    default boolean canUse(LivingEntity owner) {
        return !owner.hasEffect(JJKEffects.STAGGER.get());
    }

    default LivingEntity hitEntity(LivingEntity owner) {
        var hit = RotationUtil.getLookAtHit(owner, getRange());
        if (hit instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof LivingEntity target) {
                return target;
            }
        }
        return null;
    }

    @Override
    default boolean onRightClick(LivingEntity owner) {
        if (!canUse(owner)) return false;
        LivingEntity target = hitEntity(owner);
        if (target == null) return false;
        if (!owner.canAttack(target)) return false;
        owner.swing(InteractionHand.OFF_HAND);
        if (!owner.level().isClientSide) {
            perform(owner, target);
        }
        return true;
    }
}

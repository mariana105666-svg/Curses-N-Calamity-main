package radon.jujutsu_kaisen.ability.cursed_energy_discharge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.effect.GraniteRingEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class GraniteRing extends Ability {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;

        return HelperMethods.RANDOM.nextInt(8) == 0
                && owner.distanceTo(target) <= GraniteRingEntity.RADIUS;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    private static boolean isInsideDomain(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return false;

        for (IDomain domain : VeilHandler.getDomains(level, owner.blockPosition())) {
            if (!(domain instanceof DomainExpansionEntity expansion)) continue;

            if (expansion.isRemoved() || !expansion.isAlive()) continue;

            return true;
        }
        return false;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (isInsideDomain(owner)) return Status.FAILURE;

        return super.isTriggerable(owner);
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return;

        owner.level().addFreshEntity(new GraniteRingEntity(owner));

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                JJKSounds.SPARK.get(), SoundSource.MASTER, 1.5F, 0.6F);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 350.0F;
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
    }
}

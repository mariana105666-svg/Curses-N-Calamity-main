package radon.jujutsu_kaisen.ability.puppet_manipulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.JJKPose;
import radon.jujutsu_kaisen.client.JJKPoses;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.entity.effect.ChargeBlastEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class ChargeBlast extends Ability implements Ability.IPosedMove {
    private static final int COOLDOWN = 5 * 20;

    private static final float COST = 120.0F;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;

        return HelperMethods.RANDOM.nextInt(5) == 0 && owner.hasLineOfSight(target)
                && owner.distanceTo(target) <= ChargeBlastEntity.RANGE / 2;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.isPossessing();
    }

    @Override
    public boolean worksThroughBurnout() {
        return true;
    }

    @Override
    public @NotNull JJKPose getArmPose(LivingEntity entity) {
        return JJKPoses.CHARGE_BLAST;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return;

        owner.level().addFreshEntity(new ChargeBlastEntity(owner, this.getPower(owner), 0.0F));

        int count = 12 + (int) (owner.getBbWidth() * owner.getBbHeight()) * 16;

        for (int i = 0; i < count; i++) {
            double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 2)
                    - owner.getLookAngle().scale(0.5D).x;
            double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * owner.getBbHeight();
            double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 2)
                    - owner.getLookAngle().scale(0.5D).z;

            ((ServerLevel) owner.level()).sendParticles(
                    new CursedEnergyParticle.CursedEnergyParticleOptions(ChargeBlastEntity.COLOR, 0.2F, 1.0F, 1),
                    x, y, z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                JJKSounds.SPARK.get(), SoundSource.MASTER, 1.0F, 1.0F);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return COST;
    }

    @Override
    public int getCooldown() {
        return COOLDOWN;
    }
}

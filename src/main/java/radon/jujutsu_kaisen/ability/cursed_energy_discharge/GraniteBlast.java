package radon.jujutsu_kaisen.ability.cursed_energy_discharge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.entity.effect.GraniteBlastEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class GraniteBlast extends Ability implements Ability.ICharged {
    public static final int MAX_CHARGE = 20;

    private static final int MIN_COOLDOWN = 6 * 20;
    private static final int MAX_COOLDOWN = 13 * 20;

    private static final float MIN_COST = 100.0F;
    private static final float MAX_COST = 300.0F;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    private float chargeRatio(LivingEntity owner) {
        return (float) Math.min(MAX_CHARGE, this.getCharge(owner)) / MAX_CHARGE;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;

        return HelperMethods.RANDOM.nextInt(5) == 0 && owner.hasLineOfSight(target)
                && owner.distanceTo(target) <= GraniteBlastEntity.RANGE / 2;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public boolean worksThroughBurnout() {
        return true;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;

        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(this.chargeRatio(owner) * 100)), false);
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return true;

        owner.level().addFreshEntity(new GraniteBlastEntity(owner, this.getPower(owner), this.chargeRatio(owner)));

        int count = 12 + (int) (owner.getBbWidth() * owner.getBbHeight()) * 16;

        for (int i = 0; i < count; i++) {
            double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 2)
                    - owner.getLookAngle().scale(0.5D).x;
            double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * owner.getBbHeight();
            double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 2)
                    - owner.getLookAngle().scale(0.5D).z;

            ((ServerLevel) owner.level()).sendParticles(
                    new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.getCursedEnergyColorBright(owner), 0.2F, 1.0F, 1),
                    x, y, z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                JJKSounds.SPARK.get(), SoundSource.MASTER, 1.0F, 1.0F);

        return true;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return MIN_COST + (MAX_COST - MIN_COST) * this.chargeRatio(owner);
    }

    @Override
    public int getRealCooldown(LivingEntity owner) {
        return Math.round(MIN_COOLDOWN + (MAX_COOLDOWN - MIN_COOLDOWN) * this.chargeRatio(owner));
    }

    @Override
    public int getCooldown() {
        return MIN_COOLDOWN;
    }
}

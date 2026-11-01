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
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.entity.effect.MaximumGraniteBeamEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class MaximumGraniteBeam extends Ability implements Ability.ICharged {
    public static final int MAX_CHARGE = 3 * 20;

    private static final float COST = 1000.0F;

    private static final int COOLDOWN = 60 * 20;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return true;
    }

    private float chargeRatio(LivingEntity owner) {
        return (float) Math.min(MAX_CHARGE, this.getCharge(owner)) / MAX_CHARGE;
    }

    private boolean isFullyCharged(LivingEntity owner) {
        return this.getCharge(owner) >= MAX_CHARGE;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;

        return HelperMethods.RANDOM.nextInt(20) == 0 && owner.hasLineOfSight(target)
                && owner.distanceTo(target) <= MaximumGraniteBeamEntity.RANGE / 2;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public boolean isChantable() {
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
        if (!this.isFullyCharged(owner)) return false;

        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return true;

        owner.level().addFreshEntity(new MaximumGraniteBeamEntity(owner, this.getPower(owner),
                ChantHandler.getOutput(owner, this)));

        int count = 48 + (int) (owner.getBbWidth() * owner.getBbHeight()) * 32;

        for (int i = 0; i < count; i++) {
            double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 3);
            double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * owner.getBbHeight();
            double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 3);

            ((ServerLevel) owner.level()).sendParticles(
                    new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.getCursedEnergyColorBright(owner), 0.4F, 1.0F, 1),
                    x, y, z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                JJKSounds.SPARK.get(), SoundSource.MASTER, 3.0F, 0.5F);

        return true;
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

package radon.jujutsu_kaisen.ability.misc;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;


public class RCT1 extends Ability implements Ability.IChannelened {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return (owner.getHealth() / owner.getMaxHealth()) <= 0.6F;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public void run(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.getEnergy() < cap.getMaxEnergy()*0.02f) return;
        float heal = ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue();
        float power = this.getPower(owner);
        if (cap.hasTrait(Trait.DEATH_PAINTING)) {
        if (owner instanceof Player player) {
            owner.heal(heal * power * 0.20F * this.healMult() + 0.5f);
        } else {
            owner.heal((float) Math.min(1.0F, heal * Math.pow(power * 0.175F, Math.log(power)) * 0.175F) + 0.075f);
        }
        } else {
            if (owner instanceof Player player) {
                owner.heal(heal * power * 0.225F * this.healMult() + 0.75f);
            } else {
                owner.heal((float) Math.min(1.0F, heal * Math.pow(power * 0.175F, Math.log(power)) * 0.15F) + 0.1f);
            }
        }
        if (!(owner.level() instanceof ServerLevel level)) return;
        for (int i = 0; i < 2; i++) {
            cap.delayTickEvent(() -> {
                for (int j = 0; j < 2; j++) {
                    double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).x;
                    double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * (owner.getBbHeight());
                    double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).z;
                    double speed = (owner.getBbHeight() * 0.1F) * HelperMethods.RANDOM.nextDouble();
                    level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.RCT, owner.getBbWidth() * 0.5F,
                               0.2F, 16), x, y, z, 0, 0.0D, speed, 0.0D, 1.0D);
                }
            }, i * 2);
         }
    }

    public float healMult() {
        return 1.25f;
    }

    @Override
    public float getCost(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (owner.getHealth() < owner.getMaxHealth()) {
            float base = (float) ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue() * this.getPower(owner) * this.getMultiplier();

            if (cap.hasTrait(Trait.DEATH_PAINTING)) {
                if (owner instanceof Player) {
                    return base + 6.0F;
                } else {
                    return base + 1.75F;
                }
            }

            if (owner instanceof Player) {
                return base + 9.0F;
            } else {
                return base + 3.0F;
            }
        }
        return 0.0F;
    }

    @Override
    public boolean isUnlockable() {
        return this == JJKAbilities.RCT1.get() || super.isUnlockable();
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        return true;
    }

    @Override
    public boolean canUnlock(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getType() == JujutsuType.SORCERER && super.canUnlock(owner);
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.CURSED_ENERGY_FLOW.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 2.0F);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }

    protected float getMultiplier() {
        return 2.25f;
    }
}

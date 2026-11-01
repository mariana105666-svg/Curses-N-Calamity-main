package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.idle_transfiguration.base.TransfiguredSoul;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.LimboCloneEntity;
import radon.jujutsu_kaisen.entity.idle_transfiguration.base.TransfiguredSoulEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.ResurrectionS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.ScreenFlashS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;

public class OutputRCT extends Ability {
    public static final float RANGE = 10.0F;

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
        if (target == null) return false;
        if (!target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
        ISorcererData cap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getType() == JujutsuType.CURSE && this.getTarget(owner) == target;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.RCT1.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.5F, 3.0F);
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.outputRCTCost.get();
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public boolean canUnlock(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.hasTrait(Trait.RCT_OUTPUT) && super.canUnlock(owner);
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.hasTrait(Trait.RCT_OUTPUT) && super.isDisplayed(owner);
    }

    private @Nullable LivingEntity getTarget(LivingEntity owner) {
        LivingEntity target = (RotationUtil.getExpandedLookAt(owner, RANGE));
        LivingEntity result = null;
        if (target != null) {
            result = target;
        }
        return result;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (!(owner.level() instanceof ServerLevel level)) return;

        LivingEntity target = this.getTarget(owner);

        if (target == null) return;

        ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        for (int i = 0; i < 8; i++) {
            ownerCap.delayTickEvent(() -> {
                for (int j = 0; j < 8; j++) {
                    double x = target.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (target.getBbWidth() * 1.25F) - target.getLookAngle().scale(0.35D).x;
                    double y = target.getY() + HelperMethods.RANDOM.nextDouble() * (target.getBbHeight());
                    double z = target.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (target.getBbWidth() * 1.25F) - target.getLookAngle().scale(0.35D).z;
                    double speed = (target.getBbHeight() * 0.1F) * HelperMethods.RANDOM.nextDouble();
                    level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.RCT, target.getBbWidth() * 0.5F,
                            0.2F, 16), x, y, z, 0, 0.0D, speed, 0.0D, 1.0D);
                }
            }, i * 2);
        }
        float healMult = 1.0F;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.hasTrait(Trait.RCT_OUTPUT)) {
            healMult *= 9.3F;
        }
        if (cap.getType() == JujutsuType.SHIKIGAMI) {
            healMult *= 8.0F;
        }
        float amount = ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue() * this.getPower(owner) * 1.5F * healMult;

        boolean purify = cap.hasNode(ISorcererData.RCT_OUTPUT_PURIFY_NODE);

        if (target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
            ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (targetCap.getType() == JujutsuType.CURSE && !targetCap.hasTrait(Trait.DEATH_PAINTING)) {
                target.hurt(JJKDamageSources.jujutsuAttack(owner, this), amount  * 15.0F);
                return;
            }

            if (targetCap.getType() == JujutsuType.SHIKIGAMI) {
                if (purify) RCT2.purify(target);

                target.heal(amount * 0.25F);
                return;
            }
        }

        if (purify) RCT2.purify(target);

        target.heal(amount);
        if (target.isDeadOrDying() && target.deathTime < 15 && (!(target instanceof TransfiguredSoulEntity) && HelperMethods.expCheck(target) )  ) {
            target.deathTime = 0;
            target.setHealth(amount);
            if (!target.level().isClientSide()) {
                PacketHandler.sendTrackingAndSelf(new ResurrectionS2CPacket(target.getId(), amount), target);
            }
        }
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
    public float getCost(LivingEntity owner) {
        return 65.0F;
    }

    @Override
    public int getCooldown() {
        return 80;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);  
        return cap != null && (cap.hasTrait(Trait.RCT_OUTPUT) || cap.getType() == JujutsuType.SHIKIGAMI ) && cap.getType() != JujutsuType.CURSE && super.isValid(owner);
    }
}

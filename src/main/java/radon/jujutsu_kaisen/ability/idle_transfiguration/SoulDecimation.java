package radon.jujutsu_kaisen.ability.idle_transfiguration;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.IRMBAble;
import radon.jujutsu_kaisen.ability.base.IRMBAttack;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.idle_transfiguration.base.TransfiguredSoulEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class SoulDecimation extends Ability implements Ability.IToggled, Ability.IAttack, IRMBAttack {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.hasToggled(JJKAbilities.IDLE_TRANSFIGURATION.get())) {
            cap.toggle(JJKAbilities.IDLE_TRANSFIGURATION.get());
        }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    @Override
    public LivingEntity hitEntity(LivingEntity owner) {
        //if (JJKAbilities.hasToggled(owner, JJKAbilities.SELF_EMBODIMENT_OF_PERFECTION.get())) {
            LivingEntity domainTarget = RotationUtil.getExpandedLookAt(owner, getRange() * 20.0F);
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            DomainExpansionEntity domain = cap.getSummonByClass(DomainExpansionEntity.class);
            if ((domain != null && domain.sureHitTarget(domainTarget) == domain) && domain.ability == JJKAbilities.SELF_EMBODIMENT_OF_PERFECTION.get() ) {
                return domainTarget;
            }
        //}
        var hit = RotationUtil.getLookAtHit(owner, getRange());
        if (hit instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof LivingEntity target) {
                return target;
            }
        }
        return null;
    }

        @Override
        public void perform(LivingEntity owner, LivingEntity target) {
        MobEffectInstance existing = target.getEffect(JJKEffects.TRANSFIGURED_SOUL.get());
            int amplifier = 0;

            if (existing != null) {
                amplifier = existing.getAmplifier();
            }

        // float attackerStrength = IdleTransfiguration.calculateStrength(owner);
        // float victimStrength = IdleTransfiguration.calculateStrength(target);

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            int required = 1;//Math.round((victimStrength / Math.round(attackerStrength*2/10)) * 2);
            float cost = Math.min(4,amplifier)*50;
            float ownerXP = cap.getExperience();
            ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            float targetXP = targetCap != null ? targetCap.getExperience() : target.getMaxHealth();

            float ratio = Math.max(20.0f, ownerXP) / Math.max(20.0f, targetXP);
            if ((target instanceof TransfiguredSoulEntity || amplifier >= required) && cap.getEnergy() >= cost) {
                if (target.hurt(JJKDamageSources.soulAttack(owner), target.getMaxHealth()*Math.min(6,amplifier*1.5f)/10 * ratio )  ) {
                    target.removeEffect(JJKEffects.TRANSFIGURED_SOUL.get());
                    cap.useEnergy(cost);
                }
            /*} else {
                MobEffectInstance instance = new MobEffectInstance(JJKEffects.TRANSFIGURED_SOUL.get(), 30 * 20, amplifier, false, true, true);
                target.addEffect(instance);

                if (!owner.level().isClientSide) {
                    PacketDistributor.TRACKING_ENTITY.with(() -> target).send(new ClientboundUpdateMobEffectPacket(target.getId(), instance));
                }*/
            }
        }

    @Override
    public boolean attack(DamageSource source, LivingEntity owner, LivingEntity target) {
        if (owner.level().isClientSide) return false;
        if (!HelperMethods.isMelee(source)) return false;
        //if (!owner.getMainHandItem().isEmpty()) return false;
        this.perform(owner, target);
        return true;
    }
}

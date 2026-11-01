package radon.jujutsu_kaisen.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.ability.base.IRMBAble;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import net.minecraft.server.level.ServerLevel;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;


public class AbilityHandler {
    private static final float EFFICIENCY_PER_USE = 0.3F;
    private static final float MASTERY_PER_USE = 0.1F;
    private static final int REWARD_COOLDOWN = 6000;

    private static boolean rewardable(LivingEntity owner, Ability ability) {
        if (ability instanceof Summon<?> summon && !summon.isTamed(owner)) return false;

        return ability.getMenuType() == MenuType.MELEE || ability.isTechnique();
    }

    private static boolean onDemand(Ability ability) {
        return ability instanceof Ability.IAttack || ability instanceof IRMBAble;
    }

    private static void rewardSimpleDomain(LivingEntity owner, Ability ability) {
        if (owner.level().isClientSide) return;
        if (ability != JJKAbilities.SIMPLE_DOMAIN.get()) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;
        if (!cap.hasNode(ISorcererData.SIMPLE_DOMAIN_NODE)) return;

        cap.addSimpleDomainMastery(ISorcererData.SIMPLE_DOMAIN_PER_USE);
    }

    public static void reward(LivingEntity owner, Ability ability) {
        if (owner.level().isClientSide) return;

        if (!rewardable(owner, ability)) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        cap.addProgress(EFFICIENCY_PER_USE);
        cap.addTechniqueMastery(MASTERY_PER_USE);
        radon.jujutsu_kaisen.util.SorcererUtil.checkMastery(owner);
    }

    private static void rewardActivation(LivingEntity owner, Ability ability) {
        if (owner.level().isClientSide) return;

        if (onDemand(ability)) return;

        if (!rewardable(owner, ability)) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (cap.hasRewardCooldown(ability)) return;

        cap.addRewardCooldown(ability, REWARD_COOLDOWN);
        cap.addProgress(EFFICIENCY_PER_USE);
        cap.addTechniqueMastery(MASTERY_PER_USE);
        radon.jujutsu_kaisen.util.SorcererUtil.checkMastery(owner);
    }

    public static void untrigger(LivingEntity owner,Ability ability) {

        owner.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
        if (ability.getActivationType(owner) == Ability.ActivationType.TOGGLED) {
            if (cap.hasToggled(ability)) {
                cap.toggle(ability);
            }
        }
           else if (ability.getActivationType(owner) == Ability.ActivationType.DOMAIN) {
             if (owner instanceof Player player) {    
                if (cap.hasToggled(ability)) {
                    cap.toggle(ability);
                }
                return;
            }

            boolean enemyDomain = false;
            if (owner.level().isClientSide) return;
              for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
                for (DomainExpansionEntity d : domain.getClashers() ) {      
                //for (IDomain domain : VeilHandler.getDomains((ServerLevel) owner.level(), owner.blockPosition())) {
                    if (d.getOwner() != owner) {
                        enemyDomain = true;
                    }
                }
            }
                    if (cap.hasToggled(ability)) {
                        if (enemyDomain == false) {
                            cap.toggle(ability);
                        }
                    }

        }
           else if (ability.getActivationType(owner) == Ability.ActivationType.CHANNELED) {
            if (cap.isChanneling(ability)) {
                cap.channel(ability);
            }
        }
    });
    }

    public static Ability.Status trigger(LivingEntity owner, Ability ability) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        if (cap == null) return Ability.Status.FAILURE;
        Ability.Status status = ability.isTriggerable(owner);


        if (ability.getActivationType(owner) == Ability.ActivationType.INSTANT ) {
            if (status == Ability.Status.SUCCESS) {
                ability.charge(owner);
                ability.addDuration(owner);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                ability.run(owner);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));
                reward(owner, ability);
            }
        } else if (ability.getActivationType(owner) == Ability.ActivationType.TOGGLED) {
            if (status == Ability.Status.SUCCESS || (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                boolean activating = !cap.hasToggled(ability);

                ability.addDuration(owner);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.toggle(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));

                if (activating) {
                    rewardActivation(owner, ability);
                    rewardSimpleDomain(owner, ability);
                }
            }
            return status;

        } else if (ability.getActivationType(owner) == Ability.ActivationType.DOMAIN) {
            if (status == Ability.Status.SUCCESS || (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                // if (!cap.hasToggled(ability)) {
                //     ability.charge(owner);
                // }
                boolean activating = !cap.hasToggled(ability);

                ability.addDuration(owner);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.toggle(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));

                if (activating) {
                    rewardActivation(owner, ability);
                    rewardSimpleDomain(owner, ability);
                }
            }
            return status;
        }

        else if (ability.getActivationType(owner) == Ability.ActivationType.CHANNELED) {
            if (status == Ability.Status.SUCCESS || (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                ability.addDuration(owner);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.channel(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));

                if (!onDemand(ability)) reward(owner, ability);
            }
            return status;
        }
        return status;
    }
}

package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

public class DomainAmplification extends Ability implements Ability.IToggled {

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
         if (target == null || target.isDeadOrDying() || owner.distanceTo(target) > 10.0D || JJKAbilities.hasToggled(owner, JJKAbilities.WHEEL.get())) return false;
         
         if (!target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
            boolean hasDomainUp = false;
            boolean opponentDomainUp = false;
        for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
            for (DomainExpansionEntity d : domain.getClashers() ) {
            if (d.getOwner() == owner   ) { //&& !domain instanceof IIncompleteDomain
                hasDomainUp = true;
                continue;
            }
            else if (d.getOwner() == target)  {
                 opponentDomainUp = true;
                 continue;
            }
        }
        }


        ISorcererData cap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        ISorcererData ownercap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (hasDomainUp && !(cap.getExperience() >= ConfigHolder.SERVER.requiredExperienceForExperienced.get())) {
            return false;
        }
        return (opponentDomainUp || !hasDomainUp ) && cap.hasToggled(JJKAbilities.INFINITY.get());
        //  if (opponentDomainUp && ownercap.hasToggled(this)) {
        //      //System.out.println("TURN THIS OFF");
        //      return false;
        //  }
        //  else {
        //      if (!opponentDomainUp) {
        //          return (!hasDomainUp && cap.hasToggled(JJKAbilities.INFINITY.get()));
        //      }
        //      return false;
        //  }

    }
   
    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public boolean isCursedEnergyColor() {
        return true;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.4F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return technique == null || technique.getDomain() == null ? JJKAbilities.CURSED_ENERGY_FLOW.get() : technique.getDomain();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(4.0F, 1.0F);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.domainAmplificationCost.get();
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class DomainAmplificationForgeEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource() instanceof JJKDamageSources.JujutsuDamageSource source)) return;

            LivingEntity victim = event.getEntity();

            if (!JJKAbilities.hasToggled(victim, JJKAbilities.DOMAIN_AMPLIFICATION.get())) return;

            Ability ability = source.getAbility();

            if (ability == null) return;

            if (ability.isTechnique()) {
                // (ability.getRequirements().contains(JJKAbilities.RCT1.get()) ? 0.8F :
                event.setAmount(event.getAmount() * 0.75F);
            }
        }
    }
}

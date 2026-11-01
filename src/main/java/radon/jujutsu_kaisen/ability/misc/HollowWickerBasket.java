package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.ability.base.Ability.IPosedMove;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.JJKPose;
import radon.jujutsu_kaisen.client.JJKPoses;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.HollowWickerBasketEntity;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.SimpleDomainEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

public class HollowWickerBasket extends Summon<HollowWickerBasketEntity> implements Ability.IPosedMove {
    public HollowWickerBasket() {
        super(HollowWickerBasketEntity.class);
    }

        @Override
        public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
       for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
            for (DomainExpansionEntity d : domain.getClashers() ) {
            if (d.getOwner() == owner) {
                return false;
            }
            }
            // if (!domain.hasSureHitEffect())  {
            //      continue;
            // }

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            CursedTechnique ct = cap.getTechnique();

            if (ct != null && ct.getDomain() != null && !cap.hasBurnout() ) {
                return false;
            }

            return true;
        
        }
        return false;
        
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (cap.hasTrait(Trait.INCARNATED) || ConfigHolder.SERVER.hwbForAll.get()) && super.isValid(owner);
    }

    @Override
    public int getCooldown() {
        return 20 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 1.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    @Override
    public JJKPose getArmPose(LivingEntity entityLiving) {
        return JJKPoses.HOLLOW_WICKER_BASKET;
    }

    @Override
    public boolean canUnlock(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (cap.hasTrait(Trait.INCARNATED) || ConfigHolder.SERVER.hwbForAll.get() ) && super.canUnlock(owner);
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
       ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (cap.hasTrait(Trait.INCARNATED) || ConfigHolder.SERVER.hwbForAll.get()) && super.isDisplayed(owner);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.CURSED_ENERGY_FLOW.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(3.0F, 6.0F);
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.hollowWickerBasketCost.get();
    }

     @Override
    public boolean display() {
        return false;
    }

      @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SimpleDomainForgeEvents {
   @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof DomainExpansionEntity)) return;

        LivingEntity victim = event.getEntity();

        if (victim.level().isClientSide || !JJKAbilities.hasToggled(victim, JJKAbilities.HOLLOW_WICKER_BASKET.get())) return;

        ISorcererData cap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        HollowWickerBasketEntity domain = cap.getSummonByClass(HollowWickerBasketEntity.class);

        if (domain != null) {
            //domain.hurt(event.getSource(), event.getAmount());
            event.setCanceled(true);
        }
    }
    }
   @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.HOLLOW_WICKER_BASKET.get());
    }

   @Override
    public boolean isTenShadows() {
        return false;
    }

    @Override
    public boolean usesHands() {
        return false;
    }


   @Override
    protected HollowWickerBasketEntity summon(LivingEntity owner) {
        return new HollowWickerBasketEntity(owner);
    }
}

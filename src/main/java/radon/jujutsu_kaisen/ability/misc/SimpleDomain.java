package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.SimpleDomainEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

import java.util.List;

public class SimpleDomain extends Summon<SimpleDomainEntity> {
    public SimpleDomain() {
        super(SimpleDomainEntity.class);
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
    public boolean canUnlock(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (!cap.hasTrait(Trait.INCARNATED) || cap.getTechnique() == CursedTechnique.TECHNIQUELESS || ConfigHolder.SERVER.incarnatedSimpleDomain.get()  ) && super.canUnlock(owner);
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
       ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (!cap.hasTrait(Trait.INCARNATED) || cap.getTechnique() == CursedTechnique.TECHNIQUELESS || ConfigHolder.SERVER.incarnatedSimpleDomain.get() ) && super.isDisplayed(owner);
    }


    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.SIMPLE_DOMAIN.get());
    }

    @Override
    public boolean isTenShadows() {
        return false;
    }

    @Override
    protected SimpleDomainEntity summon(LivingEntity owner) {
        return new SimpleDomainEntity(owner);
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
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
    public boolean display() {
        return false;
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
    public int getPointsCost() {
        return ConfigHolder.SERVER.simpleDomainCost.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(3.0F, 4.0F);
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SimpleDomainForgeEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource() instanceof JJKDamageSources.JujutsuDamageSource jjkdmg)) return;

            LivingEntity victim = event.getEntity(); 

            Ability ability = jjkdmg.getAbility();

            if (ability == null) return;

            if (ability.isTechnique()) {

            for (SimpleDomainEntity simple : victim.level().getEntitiesOfClass(SimpleDomainEntity.class, AABB.ofSize(victim.position(), 8.0D, 8.0D, 8.0D))) {
                if (victim.distanceTo(simple) < simple.getRadius()) {
                    DamageSource source = event.getSource();
                    LivingEntity causingEntity = source.getEntity() instanceof LivingEntity ? (LivingEntity) source.getEntity() : null;
                    if  (causingEntity != simple.getOwner() && causingEntity != victim) {
                        event.setAmount(event.getAmount() * 0.85F);
                        simple.hurt(event.getSource(), event.getAmount()*1.25F,false);
                    }
                }
            }
            }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            if (!(event.getSource().getDirectEntity() instanceof DomainExpansionEntity)) return;

            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide || !JJKAbilities.hasToggled(victim, JJKAbilities.SIMPLE_DOMAIN.get())) return;

            ISorcererData cap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            SimpleDomainEntity domain = cap.getSummonByClass(SimpleDomainEntity.class);

            if (domain != null) {
                domain.hurt(event.getSource(), event.getAmount());
                event.setCanceled(true);
            }
        }
    }
}

package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedEnergyNature;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffect;
import radon.jujutsu_kaisen.entity.SimpleDomainEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleDomainEnlargement extends Ability implements Ability.IChannelened {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    // private static void attack(LivingEntity owner, Entity entity) {
    //     if (entity instanceof AbstractArrow || entity instanceof ThrowableItemProjectile) {
    //         owner.lookAt(EntityAnchorArgument.Anchor.EYES, entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D));
    //         owner.swing(InteractionHand.MAIN_HAND, true);
    //         entity.discard();
    //     } else if (entity instanceof LivingEntity) {
    //         if (entity.invulnerableTime > 0) return;

    //         owner.lookAt(EntityAnchorArgument.Anchor.EYES, entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D));
    //     }
    // }


    @Override
    public void run(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();


        SimpleDomainEntity domain = cap.getSummonByClass(SimpleDomainEntity.class);

        if (domain == null) return;

        domain.enlarge();
    }

    // @Override
    // public boolean isValid(LivingEntity owner) {
    //     //if (!owner.level().isClientSide) {
    //         ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //         SimpleDomainEntity domain = cap.getSummonByClass(SimpleDomainEntity.class);

    //         if (domain == null) return false;

    //         if (!domain.canEnlarge()) return false;
    //     //}
        
    //     return super.isValid(owner);
    // }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!(JJKAbilities.hasToggled(owner, JJKAbilities.SIMPLE_DOMAIN.get()) )) return false;
        if (!owner.level().isClientSide)  {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        SimpleDomainEntity domain = cap.getSummonByClass(SimpleDomainEntity.class);

         if (domain == null) return false;

         if (!domain.canEnlarge()) return false;
        }
        return super.isValid(owner);
    }
    // @Override
    // public void onEnabled(LivingEntity owner) {

    // }

    // @Override
    // public void onDisabled(LivingEntity owner) {

    // }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 4.0F;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.SIMPLE_DOMAIN.get();
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.simpleDomainEnlargementCost.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(4.0F, 4.0F);
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        CursedEnergyNature nature = cap.getNature();//&& nature != CursedEnergyNature.DIVERGENT
        return (ConfigHolder.SERVER.newShadowStyleForAll.get() || (technique == CursedTechnique.TECHNIQUELESS  )) && super.isDisplayed(owner);
    }


    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    // @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    // public static class QuickDrawForgeEvents {
    //     @SubscribeEvent
    //     public static void onLivingAttack(LivingAttackEvent event) {
    //         Entity attacker = event.getSource().getDirectEntity();

    //         LivingEntity victim = event.getEntity();

    //         if (victim.level().isClientSide || !JJKAbilities.hasToggled(victim, JJKAbilities.QUICK_DRAW.get()) ||
    //                 !JJKAbilities.hasToggled(victim, JJKAbilities.FALLING_BLOSSOM_EMOTION.get())) return;

    //         QuickDraw.attack(victim, attacker);
    //     }
    // }

    
    
}

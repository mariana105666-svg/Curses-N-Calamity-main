package radon.jujutsu_kaisen.ability.mythical_beast_amber;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Transformation;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.LightningParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.sorcerer.HeianSukunaEntity;
import radon.jujutsu_kaisen.entity.sorcerer.SukunaEntity;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.SetCursedEnergyColorC2SPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MythicalBeastAmber extends Transformation {
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("81461f5f-89d5-4cc9-8b25-17e7caac9255");
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("84341016-e56a-4b95-9fd5-42b36154c885");
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("654c65b5-dc0f-4092-8423-59cbe3d19682");
    private static final UUID ARMOR_UUID = UUID.fromString("486fd273-fdbc-4876-b0b8-af5a64bfb08a");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("0be71dde-8aeb-4c5d-955f-d37325c31a94");
    private final Set<LivingEntity> glowingEntities = new HashSet<>();  
    private int r;
     private int g;
      private int b;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (JJKAbilities.hasToggled(owner, this)) {
            return true;
        }
        if (target == null || !(target instanceof SukunaEntity sukun || target instanceof HeianSukunaEntity bigsuku) ) return false;
        //ISorcererData cap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        return true;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

@Override
public void run(LivingEntity owner) {
    //if (!owner.level().isClientSide) return; // ONLY client

    Set<LivingEntity> newGlows = new HashSet<>();
    for (Entity entity : owner.level().getEntities(null, owner.getBoundingBox().inflate(15))) {
        if (entity.isAlive() && entity instanceof LivingEntity living && living != owner) {
            if (!glowingEntities.contains(living)) {
                living.setGlowingTag(true);
            }
            newGlows.add(living);
        }
    }
    for (LivingEntity prev : glowingEntities) {
        if (!newGlows.contains(prev) && prev.isAlive()) {
            prev.setGlowingTag(false);
        }
    }

    glowingEntities.clear();
    glowingEntities.addAll(newGlows);
}

    @Override
    public float getCost(LivingEntity owner) {
        return 2.0F;
    }

     @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public boolean isReplacement() {
        return false;
    }

    @Override
    public Item getItem() {
        return JJKItems.MYTHICAL_BEAST_AMBER.get();
    }

    @Override
    public Part getBodyPart() {
        return Part.BODY;
    }

    @Override
    public void applyModifiers(LivingEntity owner) {
        EntityUtil.applyModifier(owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "Attack damage", 1.75D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        EntityUtil.applyModifier(owner, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "Movement speed", 0.4D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        EntityUtil.applyModifier(owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_HEIGHT_UUID, "Step height addition", 2.0F, AttributeModifier.Operation.ADDITION);
        //EntityUtil.applyModifier(owner, Attributes.ARMOR, ARMOR_UUID, "Armor", 20.0D, AttributeModifier.Operation.ADDITION);
        //EntityUtil.applyModifier(owner, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_UUID, "Armor toughness", 2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    

    @Override
    public void removeModifiers(LivingEntity owner) {
        EntityUtil.removeModifier(owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
        EntityUtil.removeModifier(owner, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID);
        EntityUtil.removeModifier(owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_HEIGHT_UUID);
        //EntityUtil.removeModifier(owner, Attributes.ARMOR, ARMOR_UUID);
        //EntityUtil.removeModifier(owner, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_UUID);
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        
        Vector3f oldcolor = ParticleColors.getCursedEnergyColor(owner);
        this.r = (int) (oldcolor.x * 255.0D);
      
        this.g = (int) (oldcolor.y * 255.0D);
        this.b = (int) (oldcolor.z * 255.0D);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        
        int color = FastColor.ARGB32.color(255, Math.round(156), Math.round(95), Math.round(255));
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow(); 
        cap.setCursedEnergyColor(color);
        cap.maxOutput();
       // PacketHandler.sendToServer(new SetCursedEnergyColorC2SPacket(color));
       
    }




    @Override
    public void onDisabled(LivingEntity owner) {
       // if (owner.level().isClientSide) {
        for (LivingEntity entity : glowingEntities) {
            if (entity.isAlive()) {
                entity.setGlowingTag(false);
            }
        }
        glowingEntities.clear();
    //}
        ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
      
        if (ConfigHolder.SERVER.MBAEXPReset.get()) {
            ownerCap.setExperience(0);
        }
        int color = FastColor.ARGB32.color(255, Math.round(this.r), Math.round(this.g), Math.round(this.b));
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow(); 
        cap.setCursedEnergyColor(color);
        if (owner instanceof ServerPlayer play) {
            if (ConfigHolder.SERVER.MBAReroll.get()) {
                cap.wipe(play);
            }
        }
        if (ConfigHolder.SERVER.MBADeath.get() && !owner.isDeadOrDying()) {
            owner.kill();
        }
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SoulReinforcementForgeEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingDamage(LivingDamageEvent event) {
            DamageSource source = event.getSource();

            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (!victimCap.hasToggled(JJKAbilities.MYTHICAL_BEAST_AMBER.get())) return;

            if (source.getEntity() instanceof LivingEntity attacker) {
                if (!attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
                ISorcererData attackerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

                if (HelperMethods.isMelee(source)) {
                    if (attackerCap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
                        return;
                    }
                }
            }

            if (source.is(JJKDamageSources.SOUL) || source.is(JJKDamageSources.SPLIT_SOUL_KATANA) || (source instanceof JJKDamageSources.JujutsuDamageSource jujutsu && jujutsu.getAbility() == JJKAbilities.OUTPUT_RCT.get())) return;

            for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) victim.level(), victim.blockPosition())) {
                for (DomainExpansionEntity d : domain.getClashers() ) {   
                    if (d.getOwner() == source.getEntity()) return;
                }
            }

            float cost = event.getAmount() * 10.0F * (victimCap.hasTrait(Trait.SIX_EYES) ? ConfigHolder.SERVER.sixEyesMult.get().floatValue() : 1.0F);
            if (victimCap.getEnergy() < cost) return;
            victimCap.useEnergy(cost);

            int count = 8 + (int) (victim.getBbWidth() * victim.getBbHeight()) * 16;

            for (int i = 0; i < count; i++) {
                double x = victim.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (victim.getBbWidth() * 2) - victim.getLookAngle().scale(0.35D).x;
                double y = victim.getY() + HelperMethods.RANDOM.nextDouble() * victim.getBbHeight();
                double z = victim.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (victim.getBbWidth() * 2) - victim.getLookAngle().scale(0.35D).z;
               ((ServerLevel) victim.level()).sendParticles(new LightningParticle.LightningParticleOptions(ParticleColors.getCursedEnergyColorBright(victim), 0.2F, 1),
                                    x, y, z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), JJKSounds.ELECTRICITY.get(), SoundSource.MASTER, 1.0F, 1.0F);

            if (victim instanceof ServerPlayer player) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(victimCap.serializeNBT()), player);
            }
            event.setCanceled(true);
        }
    }
}

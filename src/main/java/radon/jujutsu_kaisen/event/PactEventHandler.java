package radon.jujutsu_kaisen.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.AbilityTriggerEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.Pact;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.LimboCloneEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.world.dimension.JJKDimensions;

public class PactEventHandler {
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class PactEventHandlerForgeEvents {
        @SubscribeEvent
        public static void onAbilityTrigger(AbilityTriggerEvent.Post event) {
            Ability ability = event.getAbility();
            if (ability.isTechnique() == false) return;
            LivingEntity owner = event.getEntity();
            owner.getCapability(SorcererDataHandler.INSTANCE).ifPresent(ownerCap -> {
                Map<UUID, Set<Pact>> acceptedPacts = ownerCap.getAcceptedPacts();

                for (Map.Entry<UUID, Set<Pact>> entry : acceptedPacts.entrySet()) {
                    UUID partnerId = entry.getKey();
                    Set<Pact> pacts = entry.getValue();
                    if (pacts.contains(Pact.TECHNIQUE)) {
                        Player partner = ownerCap.getPactPartner(partnerId, Pact.TECHNIQUE);

                        if (partner != null  && partner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                            ISorcererData partnerCap = partner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

                            ownerCap.removePact(partnerId, Pact.TECHNIQUE);
                            partnerCap.removePact(owner.getUUID(), Pact.TECHNIQUE);

                            ownerCap.setTechnique(CursedTechnique.TECHNIQUELESS);
                            //ownerCap.setDisable(24000);

                            if (owner instanceof Player playerOwner) {
                                playerOwner.sendSystemMessage(Component.literal("Your pact with " + partner.getName().getString() + " has been broken!"));
                                //
                            }
                             if (owner instanceof ServerPlayer serverplayerOwner) {
                                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(ownerCap.serializeNBT()), serverplayerOwner);
                             }
                             if (partner instanceof ServerPlayer serverplayerPartner) {
                                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(partnerCap.serializeNBT()), serverplayerPartner);
                             }
                            partner.sendSystemMessage(Component.literal("Your pact with " + owner.getName().getString() + " has been broken!"));
                        }
                    }
                }
            });
        }

        
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            if (source.is(JJKDamageSources.JUJUTSU)) {
                if (attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                    ISorcererData cap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    float bindingVowMult = 1.0F;
                    if (cap.hasBindingVow(BindingVow.RECOIL)) {
                        attacker.hurt(JJKDamageSources.self(victim), event.getAmount() * 0.25F);
                        bindingVowMult += 0.15F;
                    }
                    // if (cap.hasBindingVow(BindingVow.RISK)) {
                    //     if (attacker.getHealth()/attacker.getMaxHealth() > 0.25F ) {
                    //        bindingVowMult -= 0.3F;
                    //     }
                    //     else {
                    //         bindingVowMult += 0.4F;
                    //     }
                        
                    // }
                    if (bindingVowMult != 1.0F) {
                        event.setAmount(event.getAmount() * bindingVowMult);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            while (attacker instanceof TamableAnimal tamable && tamable.isTame()) {
                attacker = tamable.getOwner();

                if (attacker == null) return;
            }

            while (victim instanceof TamableAnimal tamable && tamable.isTame()) {
                victim = tamable.getOwner();

                if (victim == null) return;
            }

            if (victim.getCapability(SorcererDataHandler.INSTANCE).isPresent() && attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                ISorcererData attackerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

                if (HelperMethods.isMelee(source) && victimCap.hasPact(attacker.getUUID(), Pact.FRIENDS) && attackerCap.hasPact(victim.getUUID(), Pact.FRIENDS)) { 
                    event.setCanceled(true);
                } else if (victimCap.hasPact(attacker.getUUID(), Pact.INVULNERABILITY) && attackerCap.hasPact(victim.getUUID(), Pact.INVULNERABILITY)) {
                    victimCap.removePact(attacker.getUUID(), Pact.INVULNERABILITY);
                    attackerCap.removePact(victim.getUUID(), Pact.INVULNERABILITY);

                        MinecraftServer server = attacker.level().getServer();

                        if (server != null) {
                            ServerLevel dimension = server.getLevel(JJKDimensions.LIMBO_KEY);

                            if (dimension != null) {
                                BlockPos pos = HelperMethods.findSafePos(dimension, attacker);
                                ResourceLocation dim = attacker.level().dimension().location();
                                attacker.teleportTo(dimension, pos.getX(), pos.getY(), pos.getZ(), Set.of(), attacker.getYRot(), attacker.getXRot());
                                attacker.level().addFreshEntity(new LimboCloneEntity(attacker, dim));
                            }
                        }
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(victimCap.serializeNBT()), (ServerPlayer) victim);
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(attackerCap.serializeNBT()), (ServerPlayer) attacker);
                    //event.setCanceled(true);
                }
            }
        }
    }
}

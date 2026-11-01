package radon.jujutsu_kaisen.event;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.LivingHitByDomainEvent;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.DomainExpansion.IOpenDomain;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.entity.ten_shadows.MahoragaEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class AdaptationEventHandler {
      private static final int DISRUPTION_DURATION = 20;
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    

    public static class AdaptationEventHandlerForgeEvents {
        public static boolean isAdaptBlacklisted(Ability ability) {
            if (ability == JJKAbilities.CURSED_ENERGY_FLOW.get() ||
                ability == JJKAbilities.SIMPLE_DOMAIN.get() ||
                ability == JJKAbilities.CURSED_ENERGY_SHIELD.get() ||
                ability == JJKAbilities.FALLING_BLOSSOM_EMOTION.get() ||
                ability == JJKAbilities.DOMAIN_AMPLIFICATION.get()) {
                return true;
            }
            return false;
        }

        @SubscribeEvent
        public static void onLivingHitByDomain(LivingHitByDomainEvent event) {
            LivingEntity victim = event.getEntity();

            if (!JJKAbilities.hasToggled(victim, JJKAbilities.WHEEL.get())) return;

            ITenShadowsData cap = victim.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

            if (!cap.isAdaptedTo(event.getAbility())) {
                cap.tryAdapt(event.getAbility());
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            DamageSource source = event.getSource();
              ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (victimCap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()) || !victimCap.hasToggled(JJKAbilities.WHEEL.get())) return;

          
           
             if (!victim.getCapability(TenShadowsDataHandler.INSTANCE).isPresent()) return;
              ITenShadowsData shadowCap = victim.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
            if (!shadowCap.isAdaptedTo(source) && !HelperMethods.isMelee(source) ) shadowCap.tryAdapt(source);

            if (victim instanceof MahoragaEntity) {
                // if (cap.isAdaptedTo(source)) {
                //     victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.MASTER, 1.0F, 1.0F);
                // }

                if (HelperMethods.isMelee(source)) {
                    event.setAmount(event.getAmount() * 0.1F);
                }
                else {
                    float process = (1.0F - shadowCap.getAdaptationProgress(source));

                    switch (shadowCap.getAdaptationType(source)) {
                        case DAMAGE -> {
                        if (shadowCap.isAdaptedTo(source)) {
                            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.MASTER, 1.0F, 1.0F);
                        }
                        event.setAmount(event.getAmount() * process);
                        }
                        case COUNTER -> {
                            event.setAmount(event.getAmount() * process);
                            if (HelperMethods.RANDOM.nextInt(Math.max(1, Math.round(20 * process))) == 0) {
                            Entity attacker = source.getEntity();

                            if (attacker != null) {
                                victim.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.position());
                                victim.swing(InteractionHand.MAIN_HAND);

                                victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.MASTER, 1.0F, 1.0F);
                                if (victim.doHurtTarget(attacker)) {
                                    victim.invulnerableTime = 0;
                                }
                                //event.setCanceled(true);
                                /* 
                                * 
                                * if (HelperMethods.RANDOM.nextInt(Math.max(1, Math.round(20 * process))) == 0) {
                                Entity attacker = source.getEntity();

                                if (attacker != null) {
                                    victim.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.position());

                                    victim.swing(InteractionHand.MAIN_HAND);

                                    if (victim.doHurtTarget(attacker)) {
                                        victim.invulnerableTime = 0;
                                    }
                                }
                            }
                                * 
                                */
                            }
                        }
                }
                }
            }   
        }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            if (!attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData attackerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            boolean melee = HelperMethods.isMelee(source);

            if (!melee && victim instanceof MahoragaEntity) {
                ITenShadowsData shadowerCap = victim.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
                  switch (shadowerCap.getAdaptationType(source)) {
                      case DAMAGE -> {
                          if (shadowerCap.isAdaptedTo(source)) {
                              victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.MASTER, 1.0F, 1.0F);
                              event.setCanceled(true);
                          }
                      }
                      case COUNTER -> {
                      }
                  }
            }

            if (attackerCap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()) || !attackerCap.hasToggled(JJKAbilities.WHEEL.get())) return;

            if (!attacker.getCapability(TenShadowsDataHandler.INSTANCE).isPresent()) return;
            ITenShadowsData shadowCap = attacker.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

            if (victimCap.hasToggled(JJKAbilities.INFINITY.get())) {
                shadowCap.tryAdapt(JJKAbilities.INFINITY.get());
            }
            if (victimCap.hasToggled(JJKAbilities.SOUL_REINFORCEMENT.get())) {
                shadowCap.tryAdapt(JJKAbilities.SOUL_REINFORCEMENT.get());
            }

            if (melee) return;

            //if (!event.isCanceled()) {
                if (attacker instanceof MahoragaEntity) {
                    Set<Ability> toggled = new HashSet<>(victimCap.getToggled());

                    for (Ability ability : toggled) {
                        if (ability instanceof IOpenDomain) continue;
                        if (!shadowCap.isAdaptedTo(ability) || isAdaptBlacklisted(ability)) continue;
                         victimCap.disrupt(ability, DISRUPTION_DURATION * shadowCap.getAdaptation(ability));
                        victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.MASTER, 1.0F, 1.0F);
                         //victimCap.toggle(ability);
                    }

                    Ability channeled = victimCap.getChanneled();

                    if (channeled != null) {
                        if (shadowCap.isAdaptedTo(channeled)) {
                            victimCap.disrupt(channeled, DISRUPTION_DURATION * shadowCap.getAdaptation(channeled));
                        }
                    }

                    if (victim instanceof ServerPlayer player) {
                        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(victimCap.serializeNBT()), player);
                    }
                }
            //}
        }
    }
}

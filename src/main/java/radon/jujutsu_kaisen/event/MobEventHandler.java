package radon.jujutsu_kaisen.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.chant.ServerChantHandler;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.*;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.misc.Barrage;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.sorcerer.HeianSukunaEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.ArrayList;
import java.util.List;

public class MobEventHandler {
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class MobEventHandlerForgeEvents {
        @SubscribeEvent
        public static void onLivingHitByDomain(LivingHitByDomainEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim instanceof ISorcerer && victim instanceof Mob mob && mob.canAttack(event.getAttacker())) mob.setTarget(event.getAttacker());
        }



        @SubscribeEvent
        public static void onMobSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
            if (!(event.getLevel() instanceof ServerLevel level)) return;
            if (event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION || event.getSpawnType() == MobSpawnType.SPAWNER ) {

            Mob mob = event.getEntity();
            if (!VeilHandler.canSpawn(level, mob, event.getX(), event.getY(), event.getZ())) {
                event.setResult(MobSpawnEvent.PositionCheck.Result.DENY);
            }
            // if (!VeilHandler.canSpawn(event.getEntity(), event.getX(), event.getY(), event.getZ())) {
            //     event.setResult(Result.DENY);
            // }
            }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (attacker instanceof TamableAnimal tamable1 && attacker instanceof ISorcerer) {
                if (tamable1.isTame() && tamable1.getOwner() == victim) {
                    event.setCanceled(true);
                } else if (victim instanceof TamableAnimal tamable2 && victim instanceof ISorcerer) {
                    if (!tamable1.is(tamable2) && tamable1.isTame() && tamable2.isTame() && tamable1.getOwner() == tamable2.getOwner()) {
                        event.setCanceled(true);
                    }
                }
            } else if (victim instanceof TamableAnimal tamable && victim instanceof ISorcerer) {
                if (tamable.isTame() && tamable.getOwner() == attacker) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            DamageSource source = event.getSource();

            if (victim instanceof ISorcerer sorcerer && sorcerer.canChant()) {
                if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                    if (!JJKAbilities.hasToggled(victim, JJKAbilities.CURSED_ENERGY_FLOW.get())) {
                        AbilityHandler.trigger(victim, JJKAbilities.CURSED_ENERGY_FLOW.get());
                    }
                    if (!source.is(DamageTypeTags.BYPASSES_SHIELD)) {
                    if (!JJKAbilities.isChanneling(victim, JJKAbilities.CURSED_ENERGY_SHIELD.get())) {
                        AbilityHandler.trigger(victim, JJKAbilities.CURSED_ENERGY_SHIELD.get());
                    }
                    }
                    // if (source instanceof JJKDamageSources.JujutsuDamageSource) {
                    //     if (!JJKAbilities.hasToggled(victim, JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
                    //         AbilityHandler.trigger(victim, JJKAbilities.DOMAIN_AMPLIFICATION.get());
                    //     }
                    // }
                }
            }
        }

        @SubscribeEvent
        public static void onAbilityTrigger(AbilityTriggerEvent.Pre event) {
            Ability ability = event.getAbility();

            LivingEntity owner = event.getEntity();

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (owner instanceof HeianSukunaEntity entity && ability == JJKAbilities.BARRAGE.get()) {
                entity.setBarrage(Barrage.DURATION * 2);
            }

            if (owner.level() instanceof ServerLevel level) {
                if (owner instanceof Mob) {
                    List<String> chants = new ArrayList<>(cap.getFirstChants(ability));

                    if (!chants.isEmpty() && HelperMethods.RANDOM.nextInt(Math.max(1, (int) (50 * cap.getMaximumOutput()))) == 0) {
                        for (int i = 0; i < HelperMethods.RANDOM.nextInt(chants.size()); i++) {
                            ServerChantHandler.onChant(owner, chants.get(i));

                            for (ServerPlayer player : level.players()) {
                                if (player.distanceTo(owner) > 64.0D) continue;

                                ResourceLocation key = owner.level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(owner.getType());

                                if (key != null) {
                                    player.sendSystemMessage(Component.literal(String.format("<%s> %s", owner.getName().getString(), chants.get(i))));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
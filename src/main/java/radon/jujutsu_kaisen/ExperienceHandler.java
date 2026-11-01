package radon.jujutsu_kaisen;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.curse.base.PackCursedSpirit;
import radon.jujutsu_kaisen.entity.ten_shadows.base.TenShadowsSummon;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.SorcererUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import org.joml.Math;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExperienceHandler {
    private static final Map<UUID, CopyOnWriteArraySet<BattleData>> battles = new HashMap<>();
    private static final Map<UUID, CopyOnWriteArraySet<BattleData>> battlesBlacklist = new HashMap<>();

    private static void addBattle(UUID ownerUUID, BattleData data) {
        if (!battles.containsKey(ownerUUID)) {
            battles.put(ownerUUID, new CopyOnWriteArraySet<>());
        }
        battles.get(ownerUUID).add(data);
    }

    private static void addBlacklist(UUID ownerUUID, BattleData data) {
        if (!battlesBlacklist.containsKey(ownerUUID)) {
            battlesBlacklist.put(ownerUUID, new CopyOnWriteArraySet<>());
        }
        battlesBlacklist.get(ownerUUID).add(data);
    }

    public static void clearBlacklist(UUID ownerUUID) {
        battlesBlacklist.remove(ownerUUID);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();

        if (victim instanceof TamableAnimal tamable && tamable.isTame()) return;
        if (victim instanceof TenShadowsSummon || !HelperMethods.expCheck(victim)) return;

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (!victim.isAlive() || victim.isRemoved() || !attacker.isAlive() || attacker.isRemoved()) return;

            if (attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                Iterator<Map.Entry<UUID, CopyOnWriteArraySet<BattleData>>> iter = battles.entrySet().iterator();

                boolean existing = false;

                while (iter.hasNext()) {
                    for (BattleData battle : iter.next().getValue()) {
                        if (battle.getTargetUUID() != victim.getUUID()) continue;
                        battle.attack(attacker.getUUID(), event.getAmount());
                        existing = true;
                    }
                }

                while (attacker instanceof TamableAnimal tamable && tamable.isTame()) {
                    attacker = tamable.getOwner();

                    if (attacker == null) return;
                }
                Iterator<Map.Entry<UUID, CopyOnWriteArraySet<BattleData>>> blIter = battlesBlacklist.entrySet().iterator();

                boolean blExisting = false;

                while (blIter.hasNext()) {
                    for (BattleData battle : blIter.next().getValue()) {
                        if (battle.getOwnerUUID().equals(attacker.getUUID()) && battle.getTargetUUID().equals(victim.getUUID())) {
                            blExisting = true;
                            break;
                        }
                    }
                }

                if (!existing && (blExisting == false) ) {
                    BattleData battle = new BattleData(attacker.getUUID(), victim.getUUID());
                    addBattle(attacker.getUUID(), battle);
                    battle.attack(attacker.getUUID(), event.getAmount());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity owner = event.getEntity();
        battles.remove(owner.getUUID());
        battlesBlacklist.remove(owner.getUUID());
    }

    public static void killExperience(LivingEntity entity, LivingEntity attacker, ServerLevel level) {
        boolean blacklistMatch = false;

        if (attacker != null) {
            Set<BattleData> blSet = battlesBlacklist.get(entity.getUUID());
            if (blSet != null) {
                for (BattleData bl : blSet) {
                    if (bl.getOwnerUUID().equals(attacker.getUUID()) &&
                        bl.getTargetUUID().equals(entity.getUUID())) 
                    {
                        blacklistMatch = true;
                        break;
                    }
                }
            }
}

        ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getExperience() > 0.0F && !blacklistMatch) {
            
            float penalty = (cap.getExperience() * ConfigHolder.SERVER.deathPenalty.get().floatValue());
            cap.setExperience(Math.max(0.0F, cap.getExperience() - penalty));

           
            int points = (int) Math.floor(penalty * 0.1F * ConfigHolder.SERVER.pointPenalty.get().floatValue());
            if (points > 0) {
                cap.setPoints(Math.max(0, cap.getPoints() - points));

                if ( ConfigHolder.SERVER.pointPenalty.get().floatValue() != 0.0 && entity instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable(String.format("chat.%s.points_penalty", JujutsuKaisen.MOD_ID), points));

                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
                }
            }

            if ( ConfigHolder.SERVER.deathPenalty.get().floatValue() != 0.0 && entity instanceof ServerPlayer player ) {
                player.sendSystemMessage(Component.translatable(String.format("chat.%s.experience_penalty", JujutsuKaisen.MOD_ID), penalty));

                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
            }
        }

        Iterator<Map.Entry<UUID, CopyOnWriteArraySet<BattleData>>> battleIter = battles.entrySet().iterator();

        while (battleIter.hasNext()) {
            Set<BattleData> current = battleIter.next().getValue();
            List<BattleData> battlesToRemove = new ArrayList<>();

            for (BattleData battle : current) {
                if (battle.getOwnerUUID().equals(entity.getUUID()) || battle.getTargetUUID().equals(entity.getUUID())) {
                    battle.end(level);
                    battlesToRemove.add(battle);
                    addBlacklist(entity.getUUID(), battle);
                }
            }

            battlesToRemove.forEach(current::remove);

            if (current.isEmpty()) {
                battleIter.remove();
            }
        }
        cap.removeLife();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel level)) return;

        if (!entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;

        LivingEntity attacker = null;

        if (event.getSource().getEntity() instanceof LivingEntity living) {
            attacker = living;
        }
        killExperience(entity, attacker, level);
    }

    private static class BattleData {
        private final UUID ownerUUID;
        private final UUID targetUUID;

        private float totalDamageDealt;
        private float damageDealtByOwner;

        public BattleData(UUID ownerUUID, UUID targetUUID) {
            this.ownerUUID = ownerUUID;
            this.targetUUID = targetUUID;
        }

        public UUID getOwnerUUID() {
            return this.ownerUUID;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        private static float calculateStrength(LivingEntity entity) {
            float strength = entity.getMaxHealth() * 0.1F;
            float armor = (float) entity.getArmorValue();
            float toughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float f = 2.0F + toughness / 4.0F;
            float f1 = Mth.clamp(armor - strength / f, armor * 0.2F, 20.0F);
            strength /= 1.0F - f1 / 25.0F;

            MobEffectInstance instance = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);

            if (instance != null) {
                int resistance = instance.getAmplifier();
                int i = (resistance + 1) * 5;
                int j = 25 - i;

                if (j == 0) {
                    return strength;
                } else {
                    float x = 25.0F / (float) j;
                    strength = strength * x;
                }
            }


            strength += (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            strength += (float) entity.getAttributeValue(Attributes.MOVEMENT_SPEED);

            if (entity instanceof PackCursedSpirit pack) {
                strength += pack.getMinCount() + ((float) (pack.getMaxCount() - pack.getMinCount()) / 2);
            }

            if (entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                strength += cap.getExperience() * 0.1F;

                if (cap.getType() == JujutsuType.CURSE || cap.isUnlocked(JJKAbilities.RCT1.get())) {
                    strength *= 1.25F;
                }
            }
            return strength;
        }

        public void end(ServerLevel level) {
            if (!(level.getEntity(this.ownerUUID) instanceof LivingEntity owner) || !(level.getEntity(this.targetUUID) instanceof LivingEntity target)) return;
            if (owner.isRemoved() || owner.isDeadOrDying() || target.isRemoved()) return;
            if (this.damageDealtByOwner == 0.0F) return;
            

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            float targetStrength = calculateStrength(target) * 1.25F;
            float ownerStrength = calculateStrength(owner) * 0.5F;

            float experience = Math.min(targetStrength* ConfigHolder.SERVER.experienceMultiplier.get().floatValue(), (targetStrength - ownerStrength) * 5.0F
                    * (this.totalDamageDealt / this.damageDealtByOwner)
                    * ConfigHolder.SERVER.experienceMultiplier.get().floatValue());
        
            float rawExperience = Math.min(targetStrength, (targetStrength - ownerStrength) * 5.0F
                    * (this.totalDamageDealt / this.damageDealtByOwner));
            if (target instanceof Player targetplayer) {
                experience *= ConfigHolder.SERVER.pvpGain.get();
                rawExperience *= ConfigHolder.SERVER.pvpGain.get();
            }
            
            if (experience < 0.1F && ConfigHolder.SERVER.minEXP.get() == 0 ) return;

                experience = Math.max( ConfigHolder.SERVER.minEXP.get().floatValue() ,experience);
                if (ConfigHolder.SERVER.maxEXP.get() != 0) {
                    if (cap.hasTrait(Trait.PRODIGY)) {
                        experience *= 2;
                    }
                    experience = Math.min(ConfigHolder.SERVER.maxEXP.get().floatValue(),experience);
                }
            if (ConfigHolder.SERVER.playerRequiredForGradeUp.get() && target instanceof Player targetplayer   ) {
                SorcererGrade previous = SorcererUtil.getGrade(cap.getExperience());
                SorcererGrade current = SorcererUtil.getGrade(experience);
                if (previous != current) {
                    experience = Math.max(previous.getNext().getRequiredExperience()-0.01f - (cap.getExperience()), 0);
                }
            }
       
           


            if (cap.addExperience(experience)) {
                if (owner instanceof Player player) {
                    player.sendSystemMessage(Component.translatable(String.format("chat.%s.experience", JujutsuKaisen.MOD_ID), experience));
                }
            }

            int points = (int) Math.floor(rawExperience * 0.2F * ConfigHolder.SERVER.pointMultiplier.get().floatValue());
           
            points = Math.max(ConfigHolder.SERVER.minPoints.get(), points);
            
            if (ConfigHolder.SERVER.maxPoints.get() != 0) {
                if (cap.hasTrait(Trait.PRODIGY)) {
                    points *= 2;
                 }
                 points = Math.max(ConfigHolder.SERVER.maxPoints.get(), points);
            }
            if (points > 0) {
                cap.addPoints(points);
                if (owner instanceof Player player) {
                    player.sendSystemMessage(Component.translatable(String.format("chat.%s.points", JujutsuKaisen.MOD_ID), points));
                }
            }

            if (owner instanceof ServerPlayer player) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
            }
        }

        public void attack(UUID attackerUUID, float damage) {
            this.totalDamageDealt += damage;

            if (attackerUUID.equals(this.ownerUUID)  ) {
                this.damageDealtByOwner += damage;
            }
        }
    }
}

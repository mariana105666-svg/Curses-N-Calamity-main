package radon.jujutsu_kaisen.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.misc.RCT1;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityUtil {
    private static final UUID CE_FLOW_ARMOR_UUID = UUID.fromString("c7b8f3f0-9c4f-4e76-b69f-dc2f3d94e7b8");
    private static final UUID CE_FLOW_ARMOR_TOUGHNESS_UUID = UUID.fromString("f3a3c0e2-bc9a-45a2-9219-0f1f6de65c17");
    private static Map<UUID, Float> lastArmorBonus = new HashMap<>();
    private static Map<UUID, Float> lastToughnessBonus = new HashMap<>();
        @Nullable
    public static LivingEntity getOwner(TamableAnimal tamable) {
        LivingEntity owner = tamable;

        while (owner instanceof TamableAnimal parent && parent.isTame()) {
            owner = parent.getOwner();

            if (owner == null) return null;
        }
        return owner;
    }

    public static <T extends Entity> List<T> getTargetableEntities(Class<T> clazz, EntityGetter getter, @Nullable LivingEntity owner, AABB bounds) {
        return getter.getEntitiesOfClass(clazz, bounds, EntitySelector.ENTITY_STILL_ALIVE
                .and(EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                .and(entity -> owner == null || (entity != owner && (!(entity instanceof TamableAnimal tamable) || getOwner(tamable) != owner))));
    }

    public static <T extends Entity> List<T> getTouchableEntities(Class<T> clazz, EntityGetter getter, @Nullable LivingEntity owner, AABB bounds) {
        List<T> entities = new ArrayList<>();

        for (T entity : getTargetableEntities(clazz, getter, owner, bounds)) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            if (cap != null) {
                if (cap.hasToggled(JJKAbilities.INFINITY.get())) continue;
            }
            entities.add(entity);
        }
        return entities;
    }
    
    public static void makePoofParticles(Entity entity) {
        for (int i = 0; i < 20; ++i) {
            double d0 = HelperMethods.RANDOM.nextGaussian() * 0.02D;
            double d1 = HelperMethods.RANDOM.nextGaussian() * 0.02D;
            double d2 = HelperMethods.RANDOM.nextGaussian() * 0.02D;
            ((ServerLevel) entity.level()).sendParticles(ParticleTypes.POOF, entity.getRandomX(1.0D), entity.getRandomY(), entity.getRandomZ(1.0D),
                    0, d0, d1, d2, 1.0D);
        }
    }

    public static void applyArmorBoost(LivingEntity owner) {
        if (!(owner instanceof Player) ) return;
        //if (owner.level().isClientSide) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        removeModifier(owner, Attributes.ARMOR, CE_FLOW_ARMOR_UUID);
        removeModifier(owner, Attributes.ARMOR_TOUGHNESS, CE_FLOW_ARMOR_TOUGHNESS_UUID);
        float currentArmor = (float) owner.getAttributeValue(Attributes.ARMOR);
        //AttributeInstance instance = owner.getAttribute(Attributes.ARMOR);
        // if (instance != null) {
        //     AttributeModifier existing = instance.getModifier(CE_FLOW_ARMOR_UUID);
        //     if (existing != null) {
        //         currentArmor -= existing.getAmount();
        //     }
        // }

        float currentToughness = (float) owner.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

        // AttributeInstance instanceToughness = owner.getAttribute(Attributes.ARMOR_TOUGHNESS);
        // if (instanceToughness != null) {
        //     AttributeModifier existing = instanceToughness.getModifier(CE_FLOW_ARMOR_TOUGHNESS_UUID);
        //     if (existing != null) {
        //         currentToughness -= existing.getAmount();
        //     }
        // }

        float ceFlowArmorBonus =  ConfigHolder.SERVER.playerCEArmorMin.get().floatValue() + (ConfigHolder.SERVER.playerCEArmor.get().floatValue() * (cap.getExperience() / (SorcererGrade.SPECIAL_GRADE.getRequiredExperience())));
        float ceFlowToughnessBonus = ConfigHolder.SERVER.playerCEArmorMin.get().floatValue() / 2 + ( (ConfigHolder.SERVER.playerCEArmor.get().floatValue() * 0.575F) * (cap.getExperience() / (SorcererGrade.SPECIAL_GRADE.getRequiredExperience() )));

        float maxArmor = ConfigHolder.SERVER.playerCEArmorMax.get().floatValue();
        float maxToughness = maxArmor * 0.6F;
        if (currentArmor + ceFlowArmorBonus > maxArmor) {
            ceFlowArmorBonus = Math.max(0, maxArmor - currentArmor);
        }
        if (currentToughness + ceFlowToughnessBonus > maxToughness) {
            ceFlowToughnessBonus = Math.max(0, maxToughness - currentToughness);
        }

          //UUID id = owner.getUUID();
           // Float prevArmor = lastArmorBonus.getOrDefault(id, -1f);
            //Float prevTough = lastToughnessBonus.getOrDefault(id, -1f);
        
            //if (prevArmor.equals(ceFlowArmorBonus) && prevTough.equals(ceFlowToughnessBonus)) {
            //    return;
            //}

            //lastArmorBonus.put(id, ceFlowArmorBonus);
            //lastToughnessBonus.put(id, ceFlowToughnessBonus);
        //EntityUtil.removeModifier(owner, Attributes.ARMOR, CE_FLOW_ARMOR_UUID);
        //EntityUtil.removeModifier(owner, Attributes.ARMOR_TOUGHNESS, CE_FLOW_ARMOR_TOUGHNESS_UUID);
        EntityUtil.applyModifier(owner, Attributes.ARMOR, CE_FLOW_ARMOR_UUID, "CE Flow Armor Bonus", ceFlowArmorBonus, AttributeModifier.Operation.ADDITION);
        EntityUtil.applyModifier(owner, Attributes.ARMOR_TOUGHNESS, CE_FLOW_ARMOR_TOUGHNESS_UUID, "CE Flow Armor Toughness Bonus", ceFlowToughnessBonus, AttributeModifier.Operation.ADDITION);
            
    }

    public static void removeArmorBoost(LivingEntity owner) {
        //if (owner.level().isClientSide) return;
        EntityUtil.removeModifier(owner, Attributes.ARMOR, CE_FLOW_ARMOR_UUID);
        EntityUtil.removeModifier(owner, Attributes.ARMOR_TOUGHNESS, CE_FLOW_ARMOR_TOUGHNESS_UUID);
    }

    public static void rotation(Entity entity, Vec3 look) {
        double d0 = look.horizontalDistance();
        entity.setYRot((float) (Mth.atan2(look.x, look.z) * (double) (180.0F / (float) Math.PI)));
        entity.setXRot((float) (Mth.atan2(look.y, d0) * (double) (180.0F / (float) Math.PI)));
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    public static void offset(Entity entity, Vec3 look, Vec3 pos) {
        rotation(entity, look);
        entity.setPos(pos.x, pos.y, pos.z);
    }

    public static void convertTo(LivingEntity src, LivingEntity dst, boolean transferInventory, boolean kill) {
        if (!src.isRemoved()) {
            dst.copyPosition(src);

            if (src.hasCustomName()) {
                dst.setCustomName(src.getCustomName());
                dst.setCustomNameVisible(src.isCustomNameVisible());
            }

            dst.setInvulnerable(src.isInvulnerable());

            if (transferInventory) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack stack = src.getItemBySlot(slot);

                    if (!stack.isEmpty()) {
                        dst.setItemSlot(slot, stack.copy());
                    }
                }
            }

            src.level().addFreshEntity(dst);

            if (src.isPassenger()) {
                Entity vehicle = src.getVehicle();
                src.stopRiding();

                if (vehicle != null) {
                    vehicle.startRiding(vehicle, true);
                }
            }

            if (kill) {
                if (src instanceof Player && (!src.isDeadOrDying()) ) {
                    src.kill();
                } else {
                    src.discard();
                }
            }
        }
    }

    @Nullable
    public static RCT1 getRCTTier(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.isUnlocked(JJKAbilities.RCT3.get())) return JJKAbilities.RCT3.get();
        if (cap.isUnlocked(JJKAbilities.RCT2.get())) return JJKAbilities.RCT2.get();
        if (cap.isUnlocked(JJKAbilities.RCT1.get())) return JJKAbilities.RCT1.get();

        return null;
    }

    public static boolean applyModifier(LivingEntity owner, Attribute attribute, UUID identifier, String name, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = owner.getAttribute(attribute);
        AttributeModifier modifier = new AttributeModifier(identifier, name, amount, operation);

        if (instance != null) {
            AttributeModifier existing = instance.getModifier(identifier);

            if (existing != null) {
                if (existing.getAmount() != amount) {
                    instance.removeModifier(identifier);
                    instance.addTransientModifier(modifier);
                    return true;
                }
            } else {
                instance.addTransientModifier(modifier);
                return true;
            }
        }
        return false;
    }

    public static void removeModifier(LivingEntity owner, Attribute attribute, UUID identifier) {
        AttributeInstance instance = owner.getAttribute(attribute);

        if (instance != null) {
            instance.removeModifier(identifier);
        }
    }
}

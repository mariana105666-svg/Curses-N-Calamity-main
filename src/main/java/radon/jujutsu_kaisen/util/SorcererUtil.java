package radon.jujutsu_kaisen.util;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;

public class SorcererUtil {
    public static final float DEATH_REVERSE_CHANCE = 0.25F;
    public static final float LIMB_REVERSE_CHANCE = 0.05F;

    public static final float VEIL_UNLOCK_CHANCE = 0.15F;

    private static final java.util.Map<java.util.UUID, Integer> PASSING = new java.util.HashMap<>();

    public static boolean tryUnlockVeil(net.minecraft.server.level.ServerPlayer player) {
        int now = player.tickCount;
        Integer last = PASSING.put(player.getUUID(), now);

        if (last != null && now - last <= 1) return false;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;
        if (cap.hasNode(ISorcererData.VEIL_NODE)) return false;
        if (!cap.hasNode(ISorcererData.CE2_NODE)) return false;
        if (player.getRandom().nextFloat() >= VEIL_UNLOCK_CHANCE) return false;

        cap.unlockNode(ISorcererData.VEIL_NODE);
        cap.unlock(JJKAbilities.VEIL.get());
        cap.unlock(JJKAbilities.QUICK_VEIL_CONFIG.get());

        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                String.format("chat.%s.veil_learned", JujutsuKaisen.MOD_ID))
                .withStyle(net.minecraft.ChatFormatting.AQUA));

        return true;
    }

    public static boolean hasTamedMahoraga(net.minecraft.world.entity.LivingEntity owner) {
        radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData shadows =
                owner.getCapability(radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler.INSTANCE).resolve().orElse(null);

        return shadows != null && shadows.hasTamed(
                owner.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE),
                radon.jujutsu_kaisen.entity.JJKEntities.MAHORAGA.get());
    }

    public static void onTamed(net.minecraft.world.entity.LivingEntity owner, net.minecraft.world.entity.EntityType<?> type) {
        if (owner.level().isClientSide) return;
        if (type != radon.jujutsu_kaisen.entity.JJKEntities.MAHORAGA.get()) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        cap.unlockNode(ISorcererData.MAHORAGA_NODE);
    }

    public static boolean canSimpleDomain(ISorcererData cap) {
        if (cap.hasTrait(radon.jujutsu_kaisen.capability.data.sorcerer.Trait.HEAVENLY_RESTRICTION)) return false;

        return !cap.hasTrait(radon.jujutsu_kaisen.capability.data.sorcerer.Trait.INCARNATED) || cap.getTechnique() == radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.TECHNIQUELESS
                || radon.jujutsu_kaisen.config.ConfigHolder.SERVER.incarnatedSimpleDomain.get();
    }

    public static boolean canEnlargeSimpleDomain(ISorcererData cap) {
        if (!canSimpleDomain(cap)) return false;

        return radon.jujutsu_kaisen.config.ConfigHolder.SERVER.newShadowStyleForAll.get()
                || cap.getTechnique() == radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.TECHNIQUELESS;
    }

    public static boolean canQuickDraw(ISorcererData cap) {
        if (!canSimpleDomain(cap)) return false;

        return radon.jujutsu_kaisen.config.ConfigHolder.SERVER.newShadowStyleForAll.get()
                || (cap.getTechnique() == radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.TECHNIQUELESS
                        && cap.getNature() != radon.jujutsu_kaisen.capability.data.sorcerer.CursedEnergyNature.DIVERGENT);
    }

    public static boolean canHollowWickerBasket(ISorcererData cap) {
        return cap.hasTrait(radon.jujutsu_kaisen.capability.data.sorcerer.Trait.INCARNATED)
                || radon.jujutsu_kaisen.config.ConfigHolder.SERVER.hwbForAll.get();
    }

    public static boolean canMaximumMeteor(ISorcererData cap) {
        return cap.getTechnique() == radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.DISASTER_FLAMES
                || cap.hasStolen(radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.DISASTER_FLAMES);
    }

    public static boolean canUzumaki(ISorcererData cap) {
        return cap.getTechnique() == radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.CURSE_MANIPULATION
                || cap.hasStolen(radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique.CURSE_MANIPULATION);
    }

    public static boolean canCursedWomb(ISorcererData cap) {
        return cap.getType() == radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType.CURSE
                && cap.hasTrait(radon.jujutsu_kaisen.capability.data.sorcerer.Trait.CURSED_WOMB)
                && cap.checkWombAwakened();
    }

    public static void checkMastery(net.minecraft.world.entity.LivingEntity owner) {
        if (owner.level().isClientSide) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;
        if (cap.getTechniqueMastery() < ISorcererData.TECHNIQUE_MASTERY_MAX) return;
        if (cap.hasNode(ISorcererData.MAHORAGA_WHEEL_NODE)) return;

        if (!hasTamedMahoraga(owner)) return;

        cap.unlockNode(ISorcererData.MAHORAGA_WHEEL_NODE);
        cap.unlock(JJKAbilities.WHEEL.get());
    }

    public static void bridgeNodes(net.minecraft.world.entity.LivingEntity owner) {
        bridgeReverse(owner);

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (cap.hasNode(ISorcererData.CE2_NODE)) {
            cap.unlockNode(ISorcererData.BARRIER_NODE);
            cap.setBlackFlashed(true);
        }

        if (hasTamedMahoraga(owner)) cap.unlockNode(ISorcererData.MAHORAGA_NODE);

        cap.setReverseEfficiency(cap.getReverseEfficiency());
    }

    public static boolean bridgeReverse(net.minecraft.world.entity.LivingEntity owner) {
        return tryUnlockReverse(owner, 0.0F, SorcererGrade.GRADE_1);
    }

    public static boolean tryUnlockReverse(net.minecraft.world.entity.LivingEntity owner, float chance, SorcererGrade minimum) {
        if (owner.level().isClientSide) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;
        if (cap.hasNode(ISorcererData.RCT_NODE)) return false;
        if (cap.getType() == radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType.CURSE) return false;
        if (!cap.hasNode(ISorcererData.CE2_NODE)) return false;
        if (getGrade(cap.getExperience()).ordinal() < minimum.ordinal()) return false;

        if (cap.isUnlocked(JJKAbilities.RCT1.get())) {
            cap.unlockNode(ISorcererData.RCT_NODE);
            return true;
        }

        if (owner.getRandom().nextFloat() >= chance) return false;

        cap.unlockNode(ISorcererData.RCT_NODE);
        cap.unlock(JJKAbilities.RCT1.get());

        owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                String.format("chat.%s.reverse_awakened", JujutsuKaisen.MOD_ID))
                .withStyle(net.minecraft.ChatFormatting.AQUA));

        return true;
    }

    public static SorcererGrade getGrade(float experience) {
        SorcererGrade result = SorcererGrade.GRADE_4;

        for (SorcererGrade grade : SorcererGrade.values()) {
            if (experience < grade.getRequiredExperience()) break;

            result = grade;
        }
        return result;
    }

    public static float getPower(float experience) {
        return 1.6F + experience / 1340.0F;
    }

    public static float getDefense(float experience) {
        return 1.3F + experience / 5000.0F;
    }

    public static float getDefenseHR(float experience) {
        return 2.6F + experience / 5000.0F;
    }
}

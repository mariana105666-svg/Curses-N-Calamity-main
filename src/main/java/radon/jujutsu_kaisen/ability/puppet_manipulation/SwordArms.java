package radon.jujutsu_kaisen.ability.puppet_manipulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.ITransformation;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.puppet.PuppetRecord;
import radon.jujutsu_kaisen.util.EntityUtil;

import java.util.UUID;

public class SwordArms extends Ability implements Ability.IToggled, ITransformation {
    public static final double BONUS = 5.0D;

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("3c7f1b64-0d2a-45e8-9b31-5f8a6c2e4d19");

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.RADIAL;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasPuppetRegister() && !cap.getPuppets().isEmpty();
    }

    @Override
    public boolean requiresPuppet() {
        return true;
    }

    @Override
    public boolean isReplacement() {
        return false;
    }

    @Override
    public Item getItem() {
        return JJKItems.SWORD_ARMS.get();
    }

    @Override
    public Part getBodyPart() {
        return Part.BODY;
    }

    @Override
    public void applyModifiers(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.isPossessing()) {
            this.removeModifiers(owner);
            return;
        }

        EntityUtil.applyModifier(owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "Sword arms", BONUS,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void removeModifiers(LivingEntity owner) {
        EntityUtil.removeModifier(owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
    }

    @Override
    public void run(LivingEntity owner) {
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        sync(owner, true);
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        sync(owner, false);
    }

    private static void sync(LivingEntity owner, boolean enabled) {
        if (!(owner instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.hasPuppetRegister()) return;

        if (cap.isPossessing()) return;

        for (PuppetRecord record : cap.getPuppets()) {
            UUID id = record.getEntity();

            if (id == null) continue;
            if (!(level.getEntity(id) instanceof PuppetEntity puppet)) continue;

            puppet.setSwordArms(enabled);
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }
}

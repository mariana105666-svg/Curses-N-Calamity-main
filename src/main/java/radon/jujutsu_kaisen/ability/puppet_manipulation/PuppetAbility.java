package radon.jujutsu_kaisen.ability.puppet_manipulation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.PossessionHandler;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.puppet.PuppetRecord;

import java.util.List;

public class PuppetAbility extends Ability implements Ability.IToggled {
    public static final int MAX_PUPPETS = 8;

    private final int slot;

    public PuppetAbility(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    @Nullable
    private PuppetRecord record(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return null;

        List<PuppetRecord> puppets = cap.getPuppets();

        return this.slot < puppets.size() ? puppets.get(this.slot) : null;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        PuppetRecord record = this.record(owner);

        if (!super.isValid(owner) || record == null) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.isPossessing()) return true;

        return record.getName().equals(cap.getPossessed());
    }

    @Override
    public Component getName(LivingEntity owner) {
        PuppetRecord record = this.record(owner);

        return record == null ? super.getName() : Component.literal(record.getName());
    }

    @Override
    public @Nullable Component getSubtitle(LivingEntity owner) {
        PuppetRecord record = this.record(owner);

        if (record == null) return null;

        if (record.isWorn()) {
            return Component.translatable(String.format("gui.%s.puppet_worn", JujutsuKaisen.MOD_ID));
        }

        int blocks = (int) Math.sqrt(record.getPos().distanceToSqr(owner.position()));

        return Component.translatable(String.format("gui.%s.puppet_distance", JujutsuKaisen.MOD_ID), blocks);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return true;
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
    public boolean usesHands() {
        return false;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (this.record(owner) == null) return Status.FAILURE;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap != null && cap.isPossessing() && !cap.hasToggled(this)) return Status.FAILURE;

        return super.isTriggerable(owner);
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        if (!(owner instanceof ServerPlayer player)) return;

        PuppetRecord record = this.record(owner);

        if (record == null) return;

        PossessionHandler.possess(player, record);
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        if (!(owner instanceof ServerPlayer player)) return;

        PossessionHandler.release(player, false);
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof ServerPlayer player)) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.isPossessing()) return;

        if (!PossessionHandler.tick(player, cap)) {
            AbilityHandler.untrigger(owner, this);
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }
}

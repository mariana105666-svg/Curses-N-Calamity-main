package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.entity.IVeil;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.IncompleteDomainEntity;
import radon.jujutsu_kaisen.entity.VeilEntity;

public class DispelVeil extends Ability {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Nullable
    private VeilEntity getOwnedVeil(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return null;

        VeilEntity result = null;

        for (IVeil current : VeilHandler.getVeils(level, owner.blockPosition())) {
            VeilEntity veil = current.getVeil();

            if (veil == null || veil.isRemoved()) continue;

            UUID creator = veil.getOwnerUUID();

            if (creator == null || !creator.equals(owner.getUUID())) continue;

            if (IncompleteDomainEntity.isClaimed(level, veil)) continue;

            if (result == null || veil.getRadius() < result.getRadius()) {
                result = veil;
            }
        }
        return result;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (!owner.level().isClientSide && this.getOwnedVeil(owner) == null) return Status.FAILURE;

        return super.isTriggerable(owner);
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        VeilEntity veil = this.getOwnedVeil(owner);

        if (veil == null) return;

        veil.discard();

        if (owner instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(String.format("chat.%s.dispel_veil", JujutsuKaisen.MOD_ID)));
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public List<Ability> getRequirements() {
        return List.of(JJKAbilities.VEIL.get());
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;

        if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return !cap.hasTrait(Trait.HEAVENLY_RESTRICTION);
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 0.0F);
    }
}

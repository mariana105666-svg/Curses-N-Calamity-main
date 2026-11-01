package radon.jujutsu_kaisen.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class VesselEntity extends Mob {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(VesselEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public VesselEntity(EntityType<? extends Mob> pType, Level pLevel) {
        super(pType, pLevel);

        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public VesselEntity(Level level, Player owner) {
        this(JJKEntities.VESSEL.get(), level);

        this.entityData.set(DATA_OWNER, Optional.of(owner.getUUID()));

        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
        this.yBodyRot = owner.yBodyRot;
        this.yHeadRot = owner.yHeadRot;

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(owner.getMaxHealth());
        this.setHealth(owner.getHealth());
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_OWNER, Optional.empty());
    }

    @Nullable
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || this.tickCount < 40 || this.tickCount % 40 != 0) return;

        Player owner = this.getOwner();

        if (owner == null) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (cap.isPossessing() && this.getUUID().equals(cap.getVessel())) return;

        this.discard();
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    @Nullable
    public Player getOwner() {
        UUID owner = this.getOwnerUUID();

        return owner == null ? null : this.level().getPlayerByUUID(owner);
    }

    @Override
    public void push(@NotNull net.minecraft.world.entity.Entity pEntity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot pSlot, @NotNull ItemStack pStack) {
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        UUID owner = this.getOwnerUUID();

        if (owner != null) pCompound.putUUID("owner", owner);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.hasUUID("owner")) {
            this.entityData.set(DATA_OWNER, Optional.of(pCompound.getUUID("owner")));
        }
    }
}

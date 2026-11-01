package radon.jujutsu_kaisen.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.entity.ai.goal.NearestAttackableCurseGoal;
import radon.jujutsu_kaisen.entity.ai.goal.NearestAttackableSorcererGoal;
import radon.jujutsu_kaisen.entity.ai.goal.SorcererGoal;
import radon.jujutsu_kaisen.entity.ai.goal.WaterWalkingFloatGoal;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LimboCloneEntity extends PathfinderMob implements ISorcerer {
    private static final int TELEPORT_RADIUS = 32;
    private static final EntityDataAccessor<Optional<CompoundTag>> DATA_PLAYER = SynchedEntityData.defineId(LimboCloneEntity.class, JJKEntityDataSerializers.OPTIONAL_COMPOUND_TAG.get());

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;

    private ResourceLocation original;

    protected LimboCloneEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        Arrays.fill(this.armorDropChances, 0.0F);
        Arrays.fill(this.handDropChances, 0.0F);
    }

    public LimboCloneEntity(LivingEntity owner, ResourceLocation original) {
        super(JJKEntities.LIMBO_CLONE.get(), owner.level());

        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());

        this.setOwner(owner);

        this.original = original;
    }

    public void setPlayer(GameProfile profile) {
        this.entityData.set(DATA_PLAYER, Optional.of(NbtUtils.writeGameProfile(new CompoundTag(), profile)));
    }

    public @Nullable GameProfile getPlayer() {
        CompoundTag a = this.entityData.get(DATA_PLAYER).orElse(null);
        if (a == null) {
            return null;
        }
        else {
            return NbtUtils.readGameProfile(a);
        }
    }

     @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_PLAYER, Optional.empty());
    }
    

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        LivingEntity owner = this.getOwner();

        if (owner != null) {
            ISorcererData ownerCap = this.getOwner().getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            ISorcererData thisCap = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            if (ownerCap != null && thisCap != null) {
                thisCap.deserializeNBT(ownerCap.serializeNBT());
            }
        }
    }

    @Override 
    public float getExperience() {
         LivingEntity owner = this.getOwner();

        if (owner != null) {
            ISorcererData ownerCap = this.getOwner().getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            return ownerCap.getExperience();
        }
        else {
            return 0.0F;
        }
    }

    @Override
    public boolean canChant() {
        return false;
    }

    @Override
    public boolean hasMeleeAttack() { return true; }
    @Override
    public boolean hasArms() { return true; }
    @Override
    public boolean canJump() { return false; }

    @Override
    public void tick() {
        LivingEntity owner = this.getOwner();

        if (!this.level().isClientSide && (owner == null || owner.isRemoved() || !owner.isAlive())) {
            this.discard();
        } else {
            super.tick();
        }
    }

    @Override
    protected void customServerAiStep() {
        LivingEntity owner = this.getOwner();

        if (owner != null) {
            if (!this.getMainHandItem().is(owner.getMainHandItem().getItem())) {
                this.setItemInHand(InteractionHand.MAIN_HAND, owner.getMainHandItem().copy());
            }
            if (!this.getOffhandItem().is(owner.getOffhandItem().getItem())) {
                this.setItemInHand(InteractionHand.OFF_HAND, owner.getOffhandItem().copy());
            }
            this.setItemSlot(EquipmentSlot.HEAD, owner.getItemBySlot(EquipmentSlot.HEAD));
            this.setItemSlot(EquipmentSlot.CHEST, owner.getItemBySlot(EquipmentSlot.CHEST));
            this.setItemSlot(EquipmentSlot.LEGS, owner.getItemBySlot(EquipmentSlot.LEGS));
            this.setItemSlot(EquipmentSlot.FEET, owner.getItemBySlot(EquipmentSlot.FEET));

            if (this.distanceTo(owner) >= this.getAttributeValue(Attributes.FOLLOW_RANGE)) {
                double d0 = owner.getX() + ((HelperMethods.RANDOM.nextDouble() - HelperMethods.RANDOM.nextDouble()) + 0.1D) * TELEPORT_RADIUS + 0.5D;
                double d1 = owner.getY() + HelperMethods.RANDOM.nextInt(3) - 1;
                double d2 = owner.getZ() + ((HelperMethods.RANDOM.nextDouble() - HelperMethods.RANDOM.nextDouble()) + 0.1D) * TELEPORT_RADIUS + 0.5D;

                if (this.level().noCollision(this.getType().getAABB(d0, d1, d2))) {
                    this.setPos(d0, d1, d2);
                }
            }
        }
    }

     @Override
    public void die(@NotNull DamageSource pDamageSource) {
        super.die(pDamageSource);

        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        MinecraftServer server = this.level().getServer();

        if (server == null) return;

        ServerLevel dimension = server.getLevel(ResourceKey.create(Registries.DIMENSION, this.original));

        if (dimension == null) return;

        if (owner.level() != this.level()) return;

        BlockPos pos = HelperMethods.findSafePos(dimension, owner);
        owner.teleportTo(dimension, pos.getX(), pos.getY(), pos.getZ(), Set.of(), owner.getYRot(), owner.getXRot());
    }


    // @Override
    // public void die(@NotNull DamageSource pDamageSource) {
    //     super.die(pDamageSource);
        
    // if (this.level().isClientSide) return;
    //     LivingEntity owner = this.getOwner();

    //     if (owner == null || !(owner instanceof ServerPlayer play)) return;

    //     MinecraftServer server = this.level().getServer();

    //     if (server == null) return;

    //     ServerLevel dimension = server.getLevel(ResourceKey.create(Registries.DIMENSION, this.original));
    //     System.out.println("pre dim");
    //     if (dimension == null) return;
    //             System.out.println("post dim");
    //     if (owner.level() != this.level()) return;

    //     BlockPos pos = HelperMethods.findSafePos(dimension, play);
    //     play.teleportTo(dimension, pos.getX(), pos.getY(), pos.getZ(), play.getYRot(), play.getXRot());
    // }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        if (this.ownerUUID != null) {
            pCompound.putUUID("owner", this.ownerUUID);
        }
        this.entityData.get(DATA_PLAYER).ifPresent(player -> pCompound.put("player", player));
        pCompound.putString("original", this.original.toString());
        
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.hasUUID("owner")) {
            this.ownerUUID = pCompound.getUUID("owner");
        }
         if (pCompound.contains("player")) {
            this.entityData.set(DATA_PLAYER, Optional.of(pCompound.getCompound("player")));
        }
        this.original = new ResourceLocation(pCompound.getString("original"));
    }


        
    @Override
    protected void registerGoals() {
        int goal = 1;
        int target = 1;

        this.goalSelector.addGoal(goal++, new WaterWalkingFloatGoal(this));
        if (this.hasMeleeAttack()) this.goalSelector.addGoal(goal++, new MeleeAttackGoal(this, 1.00D, true));
        this.goalSelector.addGoal(goal++, new SorcererGoal(this));
        this.goalSelector.addGoal(goal++, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(target++, new HurtByTargetGoal(this));

        if (this.targetsSorcerers()) this.targetSelector.addGoal(target++, new NearestAttackableSorcererGoal(this, true));
        if (this.targetsCurses()) this.targetSelector.addGoal(target++, new NearestAttackableCurseGoal(this, true));
    }

    protected boolean canFly() { return true; }
    protected boolean targetsCurses() { return true; }
    protected boolean targetsSorcerers() { return true; }

    public void setOwner(@Nullable LivingEntity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = (LivingEntity) ((ServerLevel) this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Component getName() {
        LivingEntity owner = this.getOwner();
        return owner == null ? super.getName() : owner.getName();
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public SorcererGrade getGrade() {
        return null;
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return null;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return null;
    }

    
}

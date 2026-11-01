package radon.jujutsu_kaisen.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.block.entity.IOpenDomainBarrier;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.RotationUtil;

public abstract class OpenDomainExpansionEntity extends DomainExpansionEntity implements IOpenDomainBarrier {
    private static final EntityDataAccessor<Integer> DATA_RADIUS = SynchedEntityData.defineId(OpenDomainExpansionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEIGHT = SynchedEntityData.defineId(OpenDomainExpansionEntity.class, EntityDataSerializers.INT);
    private final List<DomainExpansionEntity> clashers = new ArrayList<>();

    public OpenDomainExpansionEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public OpenDomainExpansionEntity(EntityType<?> pType, LivingEntity owner, DomainExpansion ability, int radius, int height) {
        super(pType, owner, ability);

        Vec3 pos = owner.position()
                .subtract(RotationUtil.getTargetAdjustedLookAngle(owner)
                        .multiply(this.getBbWidth() / 2.0F, 0.0D, this.getBbWidth() / 2.0F));
        this.moveTo(pos.x, pos.y, pos.z, RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));

        this.entityData.set(DATA_RADIUS, radius);
        this.entityData.set(DATA_HEIGHT, height);
        this.registerClasher(this);
        for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
            if (domain instanceof DomainBarrierEntity barrier) {
                barrier.registerClasher(this);
                // this.registerClasher(barrier);
            }
        }
        // DomainBarrierEntity existing = this.getClosestDomainBarrier(owner.blockPosition());
        // if (existing != null) {
        //     existing.registerClasher(this);
        // }
    }

    @Override
    public void push(@NotNull Entity pEntity) {

    }

    @Override
    public List<DomainExpansionEntity> getClashers() {
        return this.clashers;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public void registerClasher(DomainExpansionEntity clasher) {
        if (clasher == null) return;
        if (!this.clashers.contains(clasher)) {
            this.clashers.add(clasher);
        }
    }

    public void unregisterClasher(DomainExpansionEntity clasher) {
        if (clasher == null) return;
        this.clashers.remove(clasher);
    }
    
    @Override
    public boolean isBarrier(BlockPos pos) {
        return this.isInsideBarrier(pos);
    }

    @Override
    public boolean isBarrierOrInside(BlockPos pos) {
        return this.isBarrier(pos);
    }

        
    @Override
    public DomainExpansionEntity checkSureHitEffect() {
        if (!this.getSureHitToggled()) return null;

        if (this.isClashing()) return null;

        for (IDomainBarrier domain : VeilHandler.getDomainBarriers( (ServerLevel)this.level(), this.getBounds())) {
        //
            if (domain == this || !domain.isInsideBarrier(this.blockPosition())) continue;
           for (DomainExpansionEntity d : domain.getClashers() ) {   
                if (d != this && this.shouldCollapse(d.getStrength())) {
                    this.discard();
                }
                else if (d != this) {
                    return null;
                }
            }
            
            
           // }
        }
        return this;
    }

    @Override
    public boolean isAffected(BlockPos pos) {
        //if (this.level().getBlockEntity(pos) instanceof VeilBlockEntity veilBe && this.checkVeil(pos, this.getOwner())) return false;
        if ( VeilHandler.isProtected(this.level(), pos)) return false;

        Set<IDomainBarrier> domains = VeilHandler.getDomainBarriers((ServerLevel) this.level(), pos);
        for (IDomainBarrier domain : domains) {
            if (domain == this) continue;
            return false;
        }
        return super.isAffected(pos);
    }

    @Override
    public DomainExpansionEntity sureHitTarget(LivingEntity target) {
        DomainExpansionEntity surehit = this.checkSureHitEffect();
        if (surehit != null) {
            if (surehit.isAffected(target, false)) {
                return surehit;
            }
        }
        return null;
    }




    // @Override
    // public boolean isAffected(BlockPos pos) {
    //     if (this.level().getBlockEntity(pos) instanceof VeilBlockEntity veilBe && this.checkVeil(pos, this.getOwner())) return false;
    //     if ( VeilHandler.isProtected(this.level(), pos)) return false;

    //     Set<IDomain> domains = VeilHandler.getDomains((ServerLevel) this.level(), pos);

    //     for (IDomain domain : domains) {
    //         if (domain == this) continue;

    //         return false;
    //     }
    //     return super.isAffected(pos);
    // }

    public void reexpand() {

    }

    public int getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public int getHeight() {
       return this.entityData.get(DATA_HEIGHT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_RADIUS, 0);
        this.entityData.define(DATA_HEIGHT, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putInt("radius", this.getRadius());
        pCompound.putInt("height", this.getHeight());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        this.entityData.set(DATA_RADIUS, pCompound.getInt("radius"));
        this.entityData.set(DATA_HEIGHT, pCompound.getInt("height"));
    }

      @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!this.level().isClientSide) {
            VeilHandler.barrier(this.level().dimension(), this.getUUID());
        }
    }

    // @Override
    // public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
    //     int radius = this.getRadius() * 2;
    //     return EntityDimensions.fixed(radius, radius);
    // }

    //     @Override
    // public AABB getBounds() {
    //     return this.getBoundingBox();
    // }
    @Override
    public AABB getBounds() {
        int radius = this.getRadius();
        int height = this.getHeight() / 2;
        return new AABB(
                this.getX() - radius, this.getY() - height, this.getZ() - radius,
                this.getX() + radius, this.getY() + height, this.getZ() + radius
        );
        // return new AABB(this.getX() - width, this.getY() - ((double) height ), this.getZ() - width,
        //         this.getX() + width, this.getY() + ((double) height), this.getZ() + width);
    }

    // @Override
    // public boolean isInsideBarrier(BlockPos pos) {
    //     int width = this.getWidth();
    //     int height = this.getHeight();
    //     BlockPos center = this.blockPosition();
    //     BlockPos relative = pos.subtract(center);
    //     return relative.getY() > -height / 2 && relative.distSqr(Vec3i.ZERO) < width * width;
    // }
    @Override
    public boolean isInsideBarrier(BlockPos pos) {
        int radius = this.getRadius();
        BlockPos center = this.blockPosition();
        BlockPos relative = pos.subtract(center);

        int dx = relative.getX();
        int dz = relative.getZ();

        if (dx * dx + dz * dz >= radius * radius) return false;

        return Math.abs(relative.getY()) < radius;
    }

    @Override
    public boolean shouldCollapse(float strength) {
        return (strength / this.getStrength()) > 2.5F;
    }

    


    // @Override
    // public void onRemovedFromWorld() {
    //     if (!this.level().isClientSide) {
    //         LivingEntity owner = this.getOwner();

    //         if (owner != null) {
    //             int burnout = Math.max(15 * 20, this.getTime());

    //             if (burnout > 45 * 20) {
    //                 burnout = 45 * 20;
    //             }

    //             int realburnout = burnout;

    //             owner.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
    //                 cap.setBurnout(realburnout);
    //                 cap.resetSpeedStacks();

    //                 if (owner instanceof ServerPlayer player) {
    //                     PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
    //                 }
    //             });
    //         }
    //     }
    //     super.onRemovedFromWorld();
    // }
     @Override
    public void remove(@NotNull RemovalReason pReason) {
        super.remove(pReason);
        for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) this.level(), this.blockPosition())) {
            if (domain instanceof DomainBarrierEntity barrier) {
                barrier.unregisterClasher(this, true);
                // this.registerClasher(barrier);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        if (!this.level().isClientSide) {
            if (this.checkSureHitEffect() != null) {
                this.doSureHitEffect();
            }
        }
    }
}

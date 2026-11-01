package radon.jujutsu_kaisen.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.antlr.v4.parse.ANTLRParser.ebnfSuffix_return;
import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.optics.profunctors.Closed;

import me.lucko.spark.forge.ForgeWorldInfoProvider.Server;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.base.ITemporaryBlockEntity;
import radon.jujutsu_kaisen.block.domain.DomainBlock;
import radon.jujutsu_kaisen.block.entity.DomainBlockEntity;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.block.entity.VeilRodBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClosedDomainExpansionEntity extends DomainExpansionEntity {
   // private static final EntityDataAccessor<Integer> DATA_RADIUS = SynchedEntityData.defineId(ClosedDomainExpansionEntity.class, EntityDataSerializers.INT);

    private DomainBarrierEntity domainBarrier;
    //private int radius;

    public ClosedDomainExpansionEntity(EntityType<? > pType, Level pLevel) {
        super(pType, pLevel);
    }

    public ClosedDomainExpansionEntity(LivingEntity owner, DomainExpansion ability, int radius) {
        this(JJKEntities.CLOSED_DOMAIN_EXPANSION.get(), owner, ability, radius);
    }

   public ClosedDomainExpansionEntity(EntityType<?> pType, LivingEntity owner, DomainExpansion ability, int radius) {
        super(pType, owner, ability);
        DomainBarrierEntity existing = null;
        Vec3 rawDir = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 horizontal = new Vec3(rawDir.x, 0.0D, rawDir.z).normalize();

        Vec3 base = owner.position().add(horizontal.scale(radius - OFFSET));

        Vec3 finalPos = base.subtract(0.0D, radius, 0.0D);
    //int radius = ConfigHolder.SERVER.domainRadius.getAsInt();

//         float yaw = RotationUtil.getTargetAdjustedYRot(owner);
//         Vec3 direction = RotationUtil.calculateViewVector(0.0F, yaw);
//         Vec3 behind = owner.position().subtract(0.0D, radius, 0.0D).add(direction.scale(radius - OFFSET));
// this.moveTo(behind.x, behind.y, behind.z, RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));
    this.moveTo(
             owner.position().x, owner.position().y, owner.position().z,
            RotationUtil.getTargetAdjustedYRot(owner),
            RotationUtil.getTargetAdjustedXRot(owner)
    );
        existing = this.getClosestDomainBarrier(owner.blockPosition());
        if (existing == null) {
     
       this.domainBarrier = new DomainBarrierEntity(owner, finalPos, radius);
       owner.level().addFreshEntity(this.domainBarrier);
    }
    else {
        this.domainBarrier = existing;
    }
    this.domainBarrier.registerClasher(this);
}

    // @Override
    // public boolean shouldCollapse(float strength) {
    //     //int radius = this.getRadius();
    //     //boolean completed = this.getTime() >= radius * 2;
    //     return super.shouldCollapse(strength);
    // }


    // public AABB getBounds() {
    //     return this.getBoundingBox();
    // }

        //     @Override
        // public void onRemovedFromWorld() {
        //     super.onRemovedFromWorld();

            
        // }

    
        
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        this.refreshDimensions();
    }

    //     @Override
    // public boolean isBarrier(BlockPos pos) {
    //     int radius = this.getRadius();
    //     BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
    //     BlockPos relative = pos.subtract(center);
    //     return Math.sqrt(relative.distSqr(Vec3i.ZERO)) >= radius - 2 && Math.sqrt(relative.distSqr(Vec3i.ZERO)) <= radius;
    // }
    @Override
    public boolean isBarrier(BlockPos pos) {
        if (this.domainBarrier == null) return false;
        return this.domainBarrier.isBarrier(pos);
    }
    // @Override
    // public boolean isInsideBarrier(BlockPos pos) {
    //     int radius = this.getRadius();
    //     BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
    //     BlockPos relative = pos.subtract(center);
    //     return Math.sqrt(relative.distSqr(Vec3i.ZERO)) < radius - 2;
    // }

    @Override
    public boolean isBarrierOrInside(BlockPos pos) {
        return this.isBarrier(pos) || this.isInsideBarrier(pos);
    }

    // @Override
    // protected void defineSynchedData() {
    //     super.defineSynchedData();

    //     this.entityData.define(DATA_RADIUS, 0);
    // }



    //  @Override
    // public boolean checkSureHitEffect() {
    //     if (!this.isCompleted()) return false;

    //     Set<IDomain> domains = VeilHandler.getDomains((ServerLevel) this.level(), this.getBounds());

    //     for (IDomain domain : domains) {
    //         if (domain == this) continue;

    //         if (this.shouldCollapse(domain.getStrength())) {
    //             this.discard();
    //         }
    //         return false;
    //     }
    //     return true;
    // }

    // @Override
    // public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
    //     super.readAdditionalSaveData(pCompound);

    //     this.entityData.set(DATA_RADIUS, pCompound.getInt("radius"));
    //     //this.total = pCompound.getInt("total");

    //     for (Tag key : pCompound.getList("positions", Tag.TAG_COMPOUND)) {
    //         CompoundTag nbt = (CompoundTag) key;
    //         this.positions.put(nbt.getUUID("identifier"), new Vec3(nbt.getDouble("pos_x"),
    //                 nbt.getDouble("pos_y"), nbt.getDouble("pos_z")));
    //     }
    //     this.blockPositions.clear();
    //     ListTag blockList = pCompound.getList("block_positions", Tag.TAG_COMPOUND);
    //     for (Tag tag : blockList) {
    //         CompoundTag nbt = (CompoundTag) tag;
    //         BlockPos pos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    //         this.blockPositions.add(pos);
    //     }
    // }

    //     @Override
    // public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
    //     super.addAdditionalSaveData(pCompound);

    //     pCompound.putInt("radius", this.getRadius());
    // }

    // @Override
    // public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
    //     super.addAdditionalSaveData(pCompound);

    //     pCompound.putInt("radius", this.getRadius());
    //     //pCompound.putInt("total", this.total);
    //      ListTag positionsTag = new ListTag();

    //     for (Map.Entry<UUID, Vec3> entry : this.positions.entrySet()) {
    //         CompoundTag nbt = new CompoundTag();
    //         nbt.putUUID("identifier", entry.getKey());

    //         Vec3 position = entry.getValue();
    //         nbt.putDouble("pos_x", position.x);
    //         nbt.putDouble("pos_y", position.y);
    //         nbt.putDouble("pos_z", position.z);

    //         positionsTag.add(nbt);
    //     }
    //     pCompound.put("positions", positionsTag);
    //     ListTag blockList = new ListTag();
    //     for (BlockPos pos : this.blockPositions) {
    //         CompoundTag tag = new CompoundTag();
    //         tag.putInt("x", pos.getX());
    //         tag.putInt("y", pos.getY());
    //         tag.putInt("z", pos.getZ());
    //         blockList.add(tag);
    //     }
    //     pCompound.put("block_positions", blockList);
    // }

    // public int getRadius() {
    //     return this.entityData.get(DATA_RADIUS);
    // }

    // private boolean isCompleted() {
    //     int radius = this.getRadius();
    //     return this.getTime() >= radius * 2;
    // }

    // @Override
    // public boolean isReadyToCollapse() {
    //     return this.isCompleted();
    // }

    //     @Override
    // public boolean isBarrier(BlockPos pos) {
    //     int radius = this.getRadius();
    //     BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
    //     BlockPos relative = pos.subtract(center);
    //     return Math.sqrt(relative.distSqr(Vec3i.ZERO)) >= radius - 2 && Math.sqrt(relative.distSqr(Vec3i.ZERO)) <= radius;
    // }

    //   @Override
    // public boolean isInsideBarrier(BlockPos pos) {
    //     int radius = this.getRadius();
    //     BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
    //     BlockPos relative = pos.subtract(center);
    //     return relative.distSqr(Vec3i.ZERO) < (radius) * (radius);
    // }
    public boolean isInsideBarrier(BlockPos pos) {
        if (this.domainBarrier == null) return false;
        return this.domainBarrier.isInsideBarrier(pos);
    }

    @Override
    public DomainExpansionEntity checkSureHitEffect() {
        if (this.domainBarrier == null) return null;

        if (this.isClashing()) return null;

        return this.domainBarrier.checkSureHitEffect();
    }

    @Override
    public DomainExpansionEntity sureHitTarget(LivingEntity target) {
        if (this.domainBarrier == null) return null;
        return this.domainBarrier.sureHitTarget(target);
    }


    @Override
    public AABB getBounds() {
        if (this.domainBarrier == null) return null;
        return this.domainBarrier.getBounds();
    }

    // public boolean isWithinBarrier(BlockPos pos) {
    //     int radius = this.getRadius();
    //     BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
    //     BlockPos relative = pos.subtract(center);
    //     return relative.distSqr(Vec3i.ZERO) < (radius-1) * (radius-1);
    // }

    // @Override
    // public boolean isBarrierOrInside(BlockPos pos) {
    //     return this.isBarrier(pos) || this.isInsideBarrier(pos);
    // } 

    // @Override
    // public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
    //     int radius = this.getRadius() * 2;
    //     return EntityDimensions.fixed(radius, radius);
    // }


    
    // public void doSureHitEffect() {
    //     LivingEntity owner = this.getOwner();
    //     for (LivingEntity entity : this.getAffected()) {
    //         if (JJKAbilities.hasTrait(entity, Trait.HEAVENLY_RESTRICTION)) {
    //             this.ability.onHitBlock(this, owner, entity.blockPosition());
    //         } else {
    //             this.ability.onHitEntity(this, owner, entity, false);
    //         }
    //     }

    //     // int radius = this.getRadius();
    //     // BlockPos center = this.blockPosition().offset(0, radius / 2, 0);

    //     // for (int x = -radius; x <= radius; x++) {
    //     //     for (int y = -radius; y <= radius; y++) {
    //     //         for (int z = -radius; z <= radius; z++) {
    //     //             double distance = Math.sqrt(x * x + y * y + z * z);

    //     //             if (distance < radius - 1) {
    //     //                 BlockPos pos = center.offset(x, y, z);
    //     //                 this.ability.onHitBlock(this, owner, pos);
    //     //             }
    //     //         }
    //     //     }
    //     // }
    // }

    // @Override
    // public boolean checkSureHitEffect() {
    //     int radius = this.getRadius();
    //     boolean completed = this.getTime() >= radius * 2;

    //     if (!completed) return false;

    //     Set<DomainExpansionEntity> domains = VeilHandler.getDomains((ServerLevel) this.level(), this.getBounds());

    //     for (DomainExpansionEntity domain : domains) {
    //         if (domain == this) continue;

    //         if (this.shouldCollapse(domain.getStrength())) {
    //             this.discard();
    //         }
    //         return false;
    //     }
    //     return true;
    // }

    @Override
    public List<LivingEntity> getAffected() {
        if (this.domainBarrier == null) return List.of();
        return this.level().getEntitiesOfClass(
            LivingEntity.class,
            this.getBounds(),
            entity -> this.isAffected(entity)
        );
    }
    
    

    // private void check() {
    //     int radius = this.getRadius();

    //     Vec3 behind = this.position().add(0.0D, radius, 0.0D);
    //     BlockPos center = BlockPos.containing(behind);

    //     int count = 0;

    //     for (int x = -radius; x <= radius; x++) {
    //         for (int y = -radius; y <= radius; y++) {
    //             for (int z = -radius; z <= radius; z++) {
    //                 double distance = Math.sqrt(x * x + y * y + z * z);

    //                 if (distance < radius && distance >= radius - 1) {
    //                     BlockPos pos = center.offset(x, y, z);

    //                     if (this.level().getBlockEntity(pos) instanceof DomainBlockEntity be && be.getIdentifier() != null && be.getIdentifier().equals(this.getUUID())) count++;
    //                 }
    //             }
    //         }
    //     }

    //     if ((float) count / this.total < 0.75F) {
    //         this.discard();
    //     }
    // }

    public boolean isCompleted() {
        if (this.domainBarrier == null) return false;
        return this.domainBarrier.isCompleted();
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        super.remove(pReason);
        if (this.domainBarrier != null) {
            this.domainBarrier.unregisterClasher(this, true);
        }
    }
    //  @Override
    // public void remove(@NotNull RemovalReason pReason) {
    //     super.remove(pReason);

    //     Set<IDomain> domains = VeilHandler.getDomains((ServerLevel) this.level(), this.getBounds());

    //     for (IDomain domain : domains) {
    //         if (domain == this || !(domain instanceof ClosedDomainExpansionEntity closed)) continue;

    //         closed.createBarrier(true);
    //     }

    //     for (Map.Entry<UUID, Vec3> entry : this.positions.entrySet()) {
    //         UUID identifier = entry.getKey();

    //         Entity entity = ((ServerLevel) this.level()).getEntity(identifier);

    //         if (entity == null) continue;

    //         if (!this.isInsideBarrier(entity.blockPosition())) continue;

    //         Vec3 pos = entry.getValue();

    //         entity.teleportTo(pos.x, pos.y, pos.z);
    //     }
    //     this.blockPositions.clear();
    //     this.positions.clear();
    // }
    // @Override
    // public void remove(@NotNull RemovalReason pReason) {
    //     super.remove(pReason);
    //     System.out.println("die");
    //     ServerLevel serverLevel = (ServerLevel) this.level();
    //     //this.primary = false;
    //     Set<IDomain> domains = VeilHandler.getDomains((ServerLevel) this.level(), this.getBounds());
    //     boolean createdDomain = false;
    //     ClosedDomainExpansionEntity primary = this.getClosestBarrierAny(this.getBounds());
    //     if (primary != null && primary != this) {
    //                 System.out.println("unrege");
    //         primary.unregisterClasher(this, true);
    //     }
    //     else {
    //         for (ClosedDomainExpansionEntity clasher : this.clashers) {
    //             if (clasher != this ) {
    //                 createdDomain = true;
    //                 clasher.createBarrier(true);
    //             }
    //         }
    //     }
    //     //  if (primary == null) {
    //     //     System.out.println("found domain");
    //     // for (IDomain domain : domains) {
    //     //     if (domain == this || !(domain instanceof ClosedDomainExpansionEntity closed)) continue;

    //     //     closed.createBarrier(true);
    //     // }
    //         //  if (primary == null) {

    //      if (createdDomain == false) {
    //         for (BlockPos pos : this.blockPositions) {
    //         BlockEntity be = serverLevel.getBlockEntity(pos);
    //         if (be instanceof DomainBlockEntity domainBe) {
    //             UUID id = domainBe.getIdentifier();
    //             if (id != null && id.equals(this.getUUID())) {
    //                 domainBe.destroy();
    //             }
    //         }
    //     }
    //     }
        
    
    //     for (Map.Entry<UUID, Vec3> entry : this.positions.entrySet()) {
    //         UUID identifier = entry.getKey();
    //         boolean stillWithin = false;
    //         Entity entity = ((ServerLevel) this.level()).getEntity(identifier);

    //         if (entity == null) continue;

    //         if (!this.isInsideBarrier(entity.blockPosition())) continue;

    //         Vec3 pos = entry.getValue();
    //         for (IDomain domain : domains) {
    //             if (domain == this || !(domain instanceof ClosedDomainExpansionEntity closed)) continue;
    //                 if (closed.isInsideBarrier(entity.blockPosition())) {
    //                     stillWithin = true;
    //                     break;
    //                 }
    //             }
    //         if (!stillWithin) {
    //             entity.teleportTo(pos.x, pos.y, pos.z);
    //         }
    //     }


    //int radius = this.getRadius();
   //BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

    // for (int x = -radius; x <= radius; x++) {
    //     for (int y = -radius; y <= radius; y++) {
    //         for (int z = -radius; z <= radius; z++) {
    //             double distance = Math.sqrt(x * x + y * y + z * z);
    //             if (distance > radius) continue;

    //             BlockPos pos = center.offset(x, y, z);
    //             BlockEntity be = serverLevel.getBlockEntity(pos);
    //             if (be instanceof DomainBlockEntity domainBe) {
    //                 UUID id = domainBe.getIdentifier();
    //                 if (id != null && id.equals(this.getUUID())) {
    //                     domainBe.destroy();
    //                 }
    //             }
    //         }
    //     }
    // }


   
        // } else {
        //     //serverLevel.destroyBlock(pos, false); 
        //     //if (be instanceof DomainBlockEntity domainBe) {
        //     BlockState state = serverLevel.getBlockState(pos);
        //     if (!state.getBlock() instanceof DomainBlock) {
        //         serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        //     }

        //     //}
        //     //serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        // }
        
    //}
    


       

    
    @Override
    public void tick() {
        super.tick();

        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        if (this.level().isClientSide) return;

    //     // ParticleOptions particle = ((DomainExpansion.IClosedDomain) this.ability).getEnvironmentParticle();

    //     // if (particle != null) {
    //     //     AABB bounds = this.getBounds();

    //     //     for (BlockPos pos : BlockPos.randomBetweenClosed(this.random, 16, (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ,
    //     //             (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ)) {
    //     //         if (!this.isWithinBarrier(pos)) continue;
    //     //         Vec3 center = pos.getCenter();
    //     //         ((ServerLevel) this.level()).sendParticles(particle, center.x, center.y, center.z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
    //     //     }
    //     // }

        if (this.domainBarrier == null || (this.domainBarrier.isCompleted() && !this.isInsideBarrier(owner.blockPosition()))) {
            this.discard();
        }
    }
}
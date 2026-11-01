package radon.jujutsu_kaisen.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.VeilBlock;
import radon.jujutsu_kaisen.block.base.ITemporaryBlockEntity;
//import radon.jujutsu_kaisen.block.domain.DomainBarrierBlock;
import radon.jujutsu_kaisen.block.domain.DomainBlock;
import radon.jujutsu_kaisen.block.entity.DomainBlockEntity;
import radon.jujutsu_kaisen.block.entity.DomainSkyBlockEntity;
import radon.jujutsu_kaisen.block.entity.IBarrier;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.block.entity.IVeil;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.block.entity.VeilRodBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.entity.ten_shadows.MahoragaEntity;
import radon.jujutsu_kaisen.item.veil.modifier.ColorModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.ModifierUtils;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.RotationUtil;
import team.creative.playerrevive.PlayerReviveConfig.Sounds;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DomainBarrierEntity extends Entity implements IDomainBarrier {
    private static final EntityDataAccessor<Integer> DATA_TIME = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RADIUS = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);    
    private static final EntityDataAccessor<Float> DATA_MAX_HEALTH = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final float STRENGTH = 120.0F;

    private static final int MAX_BLOCKS_PER_TICK = 4096;
    private boolean instant;
    public static final int OFFSET = 5;
    private int lastRadius = -1;
    private final List<DomainExpansionEntity> clashers = new ArrayList<>();
    private final Map<UUID, Vec3> positions = new HashMap<>();
    private static final Map<Integer, List<Vec3i>> sphereCache = new HashMap<>();
    private List<BlockPos> toRun = new ArrayList<>();
    //private final List<BlockPos> barrierBlocks = new ArrayList<>();
    private int invulnTick;
    //private int healthMarker;
    //private final List<BlockPos> blockPositions = new ArrayList<>();  

    private static final int CHECK_INTERVAL = 20;
    //    private int total;
    //private List<Modifier> modifiers;

    @Nullable
    //private BlockPos center;
    public DomainBarrierEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
//, List<Modifier> modifiers
    public DomainBarrierEntity(LivingEntity creator, Vec3 pos, int radius) {
        super(JJKEntities.DOMAIN_BARRIER.get(), creator.level());



        this.moveTo(
                pos.x, pos.y, pos.z,
                RotationUtil.getTargetAdjustedYRot(creator),
                RotationUtil.getTargetAdjustedXRot(creator)
        );

        this.entityData.set(DATA_RADIUS, radius);
        ISorcererData cap = creator.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        Float mult = 1.0F;
        if (cap != null) {
            mult *= cap.getAbilityPower();
        }
        this.setMaxHealth(STRENGTH * mult);
        this.setHealth(this.entityData.get(DATA_MAX_HEALTH));
        //this.modifiers = modifiers;
    }

    public float getMaxHealth() {
        return this.entityData.get(DATA_MAX_HEALTH);
    }

    private void setMaxHealth(float maxHealth) {
        this.entityData.set(DATA_MAX_HEALTH, maxHealth);
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH);
    }

    private static List<Vec3i> getSphereOffsets(int radius) {
        return sphereCache.computeIfAbsent(radius, r -> {
            List<Vec3i> list = new ArrayList<>();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        if (x*x + y*y + z*z <= r*r) {
                            list.add(new Vec3i(x, y, z));
                        }
                    }
                }
            }
            return list;
        });
    }

    private void setHealth(float health) {
        this.entityData.set(DATA_HEALTH, Mth.clamp(health, 0.0F, this.getMaxHealth()));
        //float ratio = this.getHealth() / this.getMaxHealth();
        //int stage = this.getCrackStage(ratio);
        //if (this.healthMarker != stage) {
            // for (BlockPos pos : barrierBlocks) {
            //     if (this.random.nextInt(30) == 0) {
            //     BlockState state = this.level().getBlockState(pos);
            //     if (state.getBlock() instanceof DomainBarrierBlock dom) {
            //         System.out.println("setting block");
            //         System.out.println(stage);
            //         this.level().setBlock(
            //         pos,
            //         state.setValue(DomainBarrierBlock.DAMAGE, stage),
            //         Block.UPDATE_CLIENTS |  Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS
            //     );
            //     }
            //     }
            // }
            //this.healthMarker = this.getCrackStage(ratio);
        //}
    }
// List<Modifier> modifiers
    // public DomainBarrierEntity(LivingEntity owner, Vec3 pos, int radius, @Nullable BlockPos center) {
    //     //, modifiers
    //     this(owner, pos, radius);

    //     this.center = center;
    // }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TIME, 0);
        this.entityData.define(DATA_MAX_HEALTH, 0.0F);
        this.entityData.define(DATA_HEALTH, 0.0F);
        this.entityData.define(DATA_RADIUS, 0);
    }

    public int getTime() {
        return this.entityData.get(DATA_TIME);
    }

    public void setTime(int time) {
        this.entityData.set(DATA_TIME, time);
    }

    public int getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public boolean hurt(@NotNull DamageSource pSource, float pAmount, LivingEntity entity, BlockPos pos) {
        //System.out.println("hurting");
        if (this.invulnTick != 0 && this.getTime() - this.invulnTick < 5 ) return false;
       
       
        this.invulnTick = this.getTime();
                System.out.println("hurting1");
                System.out.println("max hp: " + this.getMaxHealth());
                        System.out.println("hp remaining: " + this.getHealth());

        this.setHealth(this.getHealth() - pAmount);
 
        float ratio = this.getHealth() / this.getMaxHealth();
        System.out.println("ratio: " + ratio );
        if (ratio == 0) {
             entity.level().playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GLASS_BREAK, SoundSource.MASTER, 2.0F, 1.0F   );
        }
        else {
            float pitch = 1.0F + (float) Math.pow(1.0F - ratio, 2.1) * 1.25F;

            entity.level().playSound(null, pos, JJKSounds.THUMP.get(), SoundSource.MASTER,
                    1.5F + (0.1F * (ratio / 2)),
                    pitch);
             //entity.level().playSound(null, pos.getX(), pos.getY(), pos.getZ(), JJKSounds.THUMP.get(), SoundSource.MASTER, 1.5F + (0.1F * ((ratio)/2) ), 1.0F + (1.0F - (ratio))  );
        }
                        System.out.println(this.getHealth());
        return true;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {

       // pCompound.putInt("total", this.total);
        pCompound.putInt("time", this.getTime());
        pCompound.putInt("radius", this.getRadius());
        pCompound.putFloat("max_health", this.getMaxHealth());
        pCompound.putFloat("health", this.getHealth());
        //pCompound.put("modifiers", ModifierUtils.serialize(this.modifiers));

        ListTag positionsTag = new ListTag();

        for (Map.Entry<UUID, Vec3> entry : this.positions.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("identifier", entry.getKey());

            Vec3 position = entry.getValue();
            nbt.putDouble("pos_x", position.x);
            nbt.putDouble("pos_y", position.y);
            nbt.putDouble("pos_z", position.z);

            positionsTag.add(nbt);
        }
        // if (this.center != null) {
        //     pCompound.put("center", NbtUtils.writeBlockPos(this.center));
        // }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        //this.total = pCompound.getInt("total");
        this.setTime(pCompound.getInt("time"));
        this.setRadius(pCompound.getInt("radius"));
        this.setMaxHealth(pCompound.getFloat("max_health"));
        this.setHealth(pCompound.getFloat("health"));
        //this.modifiers = ModifierUtils.deserialize(pCompound.getList("modifiers", CompoundTag.TAG_LIST));

        for (Tag key : pCompound.getList("positions", Tag.TAG_COMPOUND)) {
            CompoundTag nbt = (CompoundTag) key;
            this.positions.put(nbt.getUUID("identifier"), new Vec3(nbt.getDouble("pos_x"),
                    nbt.getDouble("pos_y"), nbt.getDouble("pos_z")));
        }

        // if (pCompound.contains("center")) {
        //     this.center = NbtUtils.readBlockPos(pCompound.getCompound("center"));
        // }
    }

    public boolean isCompleted() {
        int radius = this.getRadius();
        return this.getTime() >= radius * 2;
    }


    public boolean isBarrier(BlockPos pos) {
        int radius = this.getRadius();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
        BlockPos relative = pos.subtract(center);
        return Math.sqrt(relative.distSqr(Vec3i.ZERO)) >= radius - 1 && Math.sqrt(relative.distSqr(Vec3i.ZERO)) <= radius;
    }

    @Override
    public boolean isInsideBarrier(BlockPos pos) {
        int radius = this.getRadius();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
        BlockPos relative = pos.subtract(center);
        return Math.sqrt(relative.distSqr(Vec3i.ZERO)) < radius - 1;
    }


    public boolean isBarrierOrInside(BlockPos pos) {
        return this.isBarrier(pos) || this.isInsideBarrier(pos);
    }

    @Override
    public AABB getBounds() {
        return this.getBoundingBox();
    }

    // public List<Modifier> getModifiers() {
    //     return this.modifiers;
    // }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    
    // public BlockPos getCenter() {
    //     return this.center;
    // }

    private void check() {
        int radius = this.getRadius();

        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

        Map<Direction, Integer> counts = new EnumMap<>(Direction.class);
        Map<Direction, Integer> totals = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            counts.put(direction, 0);
            totals.put(direction, 0);
        }

        for (Vec3i off : getSphereOffsets(radius)) {
                    double distance = off.getX()*off.getX() + off.getY()*off.getY() + off.getZ()*off.getZ();

                    if (distance < (radius - 2)*(radius - 2) || distance > (radius * radius) ) continue;

                    BlockPos pos = center.offset(off);

                    if (!this.level().isInWorldBounds(pos)) continue;
                    if (this.level().getBlockState(pos).is(Blocks.BEDROCK)) continue;

                    boolean intact = this.level().getBlockEntity(pos) instanceof DomainBlockEntity;

                    if (off.getY() > 0) {
                        totals.merge(Direction.UP, 1, Integer::sum);

                        if (intact) counts.merge(Direction.UP, 1, Integer::sum);
                    }
                    if (off.getY() < 0) {
                        totals.merge(Direction.DOWN, 1, Integer::sum);

                        if (intact) counts.merge(Direction.DOWN, 1, Integer::sum);
                    }
                    if (off.getZ() > 0) {
                        totals.merge(Direction.SOUTH, 1, Integer::sum);

                        if (intact) counts.merge(Direction.SOUTH, 1, Integer::sum);
                    }
                    if (off.getZ() < 0) {
                        totals.merge(Direction.NORTH, 1, Integer::sum);

                        if (intact) counts.merge(Direction.NORTH, 1, Integer::sum);
                    }
                    if (off.getX() > 0) {
                        totals.merge(Direction.EAST, 1, Integer::sum);

                        if (intact) counts.merge(Direction.EAST, 1, Integer::sum);
                    }
                    if (off.getX() < 0) {
                        totals.merge(Direction.WEST, 1, Integer::sum);

                        if (intact) counts.merge(Direction.WEST, 1, Integer::sum);
                    }
                }
          //  }
        //}

        for (Direction direction : Direction.values()) {
            int total = totals.get(direction);

            if (total == 0) continue;

            if (counts.get(direction) / (float) total < 0.75F && !this.isRemoved() ) {
                discardClashers();
                this.discard();
                return;
            }
        }
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!this.level().isClientSide) {
            VeilHandler.barrier(this.level().dimension(), this.getUUID());
        }
    }

    @Override
    public List<DomainExpansionEntity> getClashers() {
        return this.clashers;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        int radius = this.getRadius() * 2;
        return EntityDimensions.fixed(radius, radius);
    }

    @Override
    protected void onBelowWorld() {

    }

    public boolean getShellBalance() {
        int balanceCount = 0;

        for (DomainExpansionEntity clasher : clashers) {
            if (clasher.getShellBalance()) {
                balanceCount++;
            }
        }
        return balanceCount * 2 > this.clashers.size();
    }



     

    private static class BlockPlacementTask {
        int ticks;
        Runnable action;

        BlockPlacementTask(int ticks, Runnable action) {
            this.ticks = ticks;
            this.action = action;
        }
    }

    private final Map<BlockPos, BlockPlacementTask> blockTasks = new HashMap<>();

    public void scheduleBlockPlacement(BlockPos pos, int delay, Runnable action) {
    BlockPlacementTask task = new BlockPlacementTask(delay, action);

    blockTasks.put(pos, task);
}

   public void registerClasher(DomainExpansionEntity clasher) {
        if (clasher == null) return;
        if (!this.clashers.contains(clasher)) {
            this.clashers.add(clasher);
        }

        if (!this.level().isClientSide) {
            this.createBarrier(false, clasher);
        }
        
    }

    public void unregisterClasher(DomainExpansionEntity clasher, boolean collapse) {
        if (clasher == null) return;
        this.clashers.remove(clasher);

        if (collapse == true) {
        if (!this.level().isClientSide) {
            this.createBarrier(false, clasher, true);
        }
        }
    }

    private void discardClashers() {
    for (DomainExpansionEntity clasher : new ArrayList<>(this.clashers)) {
        if (clasher != null && !clasher.isRemoved() && !(clasher instanceof OpenDomainExpansionEntity) ) {
            clasher.discard();
        }
    }
    }
     
    @Override
    public void remove(@NotNull RemovalReason pReason) {
        super.remove(pReason);
        blockTasks.clear();
        int radius = this.getRadius();
        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
        for (Vec3i off : getSphereOffsets(radius)) {
                    double distance = off.getX() * off.getX() + off.getY() * off.getY() + off.getZ() * off.getZ();
                    if (distance > (radius*radius) ) continue;
                    BlockPos pos = center.offset(off);
                    BlockEntity be = serverLevel.getBlockEntity(pos);
                    if (be instanceof DomainBlockEntity domainBe) {
                        UUID id = domainBe.getIdentifier();
                        if (id != null && id.equals(this.getUUID())) {
                            domainBe.destroy();
                        }
                    }
              //  }
            //}
        }

        Set<IBarrier> domains = VeilHandler.getBarriers((ServerLevel) this.level(), this.getBounds());
        //     for (IBarrier domain : domains) {
        //         if (domain != this && domain instanceof DomainBarrierEntity barrier) {
        //             barrier.createBarrier(true);
        //         }
        //     }
        for (Map.Entry<UUID, Vec3> entry : this.positions.entrySet()) {
            UUID identifier = entry.getKey();
            boolean stillWithin = false;
            Entity entity = ((ServerLevel) this.level()).getEntity(identifier);

            if (entity == null) continue;
            if (!this.isInsideBarrier(entity.blockPosition())) continue;

            Vec3 pos = entry.getValue();
         
            for (IBarrier domain : domains) {
                if (domain == this || !(domain instanceof DomainBarrierEntity closed)) continue;
                    if (closed.isInsideBarrier(entity.blockPosition())) {
                        stillWithin = true;
                        break;
                    }
                }
            if (!stillWithin) {
                System.out.println("positionback");
                entity.teleportTo(pos.x, pos.y, pos.z);
            }
        }
    }

    private void createBlock(int delay, BlockPos pos, int radius, double distance, BlockEntity existing, DomainExpansionEntity winner) {
        if (distance > (radius*radius) ) return;
        if (!this.level().isInWorldBounds(pos)) return;
        if (existing instanceof DomainBlockEntity dbe  && !(dbe instanceof DomainSkyBlockEntity) ) {
        UUID owner = dbe.getPidentifier();
        if (owner != null && owner.equals(winner.getUUID())) {
            return;
        }
        }

        BlockState state = this.level().getBlockState(pos);
        if (state != null && (state.is(Blocks.BEDROCK)|| (this.isCompleted() && ((state.isAir() &&  !state.is(JJKBlocks.DOMAIN_AIR.get())) || state.is(JJKBlocks.DOMAIN.get()))  )) ) return;
        if (this.isRemoved()) return;

        if (VeilHandler.getAdminVeil(this.level(), pos) != null) return;
        //if (this.isOwnedByNonDomain(pos)) return;
        CompoundTag saved = null;
        if (existing != null && !(existing instanceof ITemporaryBlockEntity)) {
            saved = existing.saveWithFullMetadata();
        }
        DomainExpansion domain = (DomainExpansion) winner.getAbility();
        List<Block> blocks = domain.getBlocks();
        List<Block> fill = domain.getFillBlocks();
        List<Block> floor = domain.getFloorBlocks();
        List<Block> bottomfloor = domain.getBottomFloorBlocks();
        List<Block> decoration = domain.getDecorationBlocks();

        Block block = JJKBlocks.DOMAIN_AIR.get();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
             
        if (distance >= (radius - 1)*(radius - 1) ) {
                block = JJKBlocks.DOMAIN.get();
        } else {
            if (pos.getY() < center.getY() - 1) {
                if (!bottomfloor.isEmpty()) {
                    block = bottomfloor.get(this.random.nextInt(bottomfloor.size()));
                } else if (!floor.isEmpty()) {
                    block = floor.get(this.random.nextInt(floor.size()));
                } else {
                    block = fill.get(this.random.nextInt(fill.size()));
                }
            } else if (pos.getY() < center.getY()) {
                block = floor.isEmpty() ? fill.get(this.random.nextInt(fill.size())) : floor.get(this.random.nextInt(floor.size()));
            } else if (pos.getY() < center.getY()) {
                block = bottomfloor.isEmpty() ? fill.get(this.random.nextInt(fill.size())) : floor.get(this.random.nextInt(floor.size()));
            } else if (distance >= (radius - 2)*(radius - 2)) {
                block = blocks.get(this.random.nextInt(blocks.size()));
            } else if (!decoration.isEmpty() && pos.getY() == center.getY()) {
                block = decoration.get(this.random.nextInt(decoration.size()));
            }
        }
        // if (existing instanceof DomainBlockEntity dbe) {
        //     if (!dbe.getIdentifier().equals(this.getUUID())) {
        //         block = JJKBlocks.DOMAIN_AIR.get();
        //         //return; // already placed by this domain
        //     }
        // }
        //
        
        
        if (JJKBlocks.DOMAIN_SKY.get() == block && this.level().getBlockEntity(pos) instanceof DomainSkyBlockEntity skybe) {
            skybe.setDomain(JJKAbilities.getKey(winner.getAbility()) );
            return;
        }
       // else if (JJKBlocks.DOMAIN.get() == block) {
       //     barrierBlocks.add(pos.immutable());
       // } 
            BlockEntity be = ((ServerLevel)this.level()).getBlockEntity(pos);
            if (be != null) {
                // if (be instanceof net.minecraft.world.Container container ) {
                //     container.clearContent();
                  
                // }
                ((ServerLevel)this.level()).removeBlockEntity(pos);
            }
             boolean success = this.level().setBlock(pos, block.defaultBlockState(),
                                              Block.UPDATE_CLIENTS |  Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            
        if (existing instanceof ITemporaryBlockEntity tmp) {
            state = tmp.getOriginal();
            saved = tmp.getSaved();
        }
        
        if (this.level().getBlockEntity(pos) instanceof DomainBlockEntity dbe) {
        // if (this.level().getBlockEntity(pos) instanceof DomainSkyBlockEntity be) {
        //     be.create(this.uuid, delay, state, saved, winner );
        // }
            dbe.create(this.uuid, delay, state, saved, winner.getUUID());
        }

    }

    /*
    
     private Map<DomainExpansionEntity, Double> getClasherAngles(BlockPos center) {
        Map<DomainExpansionEntity, Double> clasherAngles = new HashMap<>();
        int numClashers = getClasherSize();
        if (numClashers == 0) return clasherAngles;

        double anglePerClasher = 360.0 / numClashers;
        double currentAngle = 0;

        for (DomainExpansionEntity clasher : clashers) {
           // if (clasher instanceof OpenDomainExpansionEntity) continue;
            clasherAngles.put(clasher, currentAngle);
            currentAngle += anglePerClasher;
        }
        return clasherAngles;
    }

    private DomainExpansionEntity getWinnerForBlock(BlockPos blockPos, BlockPos center, Map<DomainExpansionEntity, Double> clasherAngles) {
    if (clasherAngles.isEmpty()) return null;

    Vec3 blockDir = blockPos.getCenter().subtract(center.getCenter());
    double blockAngle = Math.toDegrees(Math.atan2(blockDir.z, blockDir.x));
    if (blockAngle < 0) blockAngle += 360; // normalize to 0-360

    int numClashers = clasherAngles.size();
    double anglePerClasher = 360.0 / numClashers;

    for (Map.Entry<DomainExpansionEntity, Double> entry : clasherAngles.entrySet()) {
        double start = entry.getValue();
        double end = start + anglePerClasher;
        if (blockAngle >= start && blockAngle < end) {
            return entry.getKey();
        }
    }
    return null;
}
    */

private Map<DomainExpansionEntity, Double> getClasherAngles(BlockPos center) {
    Map<DomainExpansionEntity, Double> result = new LinkedHashMap<>();
    List<Map.Entry<DomainExpansionEntity, Double>> tmp = new ArrayList<>();

    for (DomainExpansionEntity clasher : this.clashers) {
        if (clasher == null) continue;
        Vec3 pos = clasher.position();

        Vec3 centerVec = center.getCenter();
        double dx = pos.x - centerVec.x;
        double dz = pos.z - centerVec.z;
        double angle = Math.toDegrees(Math.atan2(dz, dx)); //-180 180
        if (angle < 0) angle += 360.0;
        tmp.add(Map.entry(clasher, angle));
    }

    int n = tmp.size();
    if (n == 0) return result;

    tmp.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));

    if (n == 1) {
        result.put(tmp.get(0).getKey(), tmp.get(0).getValue());
        return result;
    }
    if (n == 2) {
    DomainExpansionEntity a = tmp.get(0).getKey();
    DomainExpansionEntity b = tmp.get(1).getKey();

    Vec3 centerVec = center.getCenter();

    Vec3 pa = a.position().subtract(centerVec).multiply(1, 0, 1);
    Vec3 pb = b.position().subtract(centerVec).multiply(1, 0, 1);

    double abAngle = Math.toDegrees(Math.atan2(
            pb.z - pa.z,
            pb.x - pa.x
    ));
    if (abAngle < 0) abAngle += 360.0;

    double aCenter = abAngle;
    double bCenter = (abAngle + 180.0);

    if (bCenter >= 360.0) bCenter -= 360.0;

    result.put(a, bCenter);
    result.put(b, aCenter);
    return result;
}
    double anglePer = 360.0 / n;

    double sumX = 0.0;
    double sumY = 0.0;
    for (int i = 0; i < n; i++) {
        double theta = tmp.get(i).getValue();
        double raw = theta - i * anglePer;
        raw = ((raw + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        double rad = Math.toRadians(raw);
        sumX += Math.cos(rad);
        sumY += Math.sin(rad);
    }

    double avgRad = Math.atan2(sumY, sumX);
    double avgDeg = Math.toDegrees(avgRad); //-180 180

    if (avgDeg < 0) avgDeg += 360.0;

    for (int i = 0; i < n; i++) {
        DomainExpansionEntity clasher = tmp.get(i).getKey();
        double centerAngle = (i * anglePer + avgDeg) % 360.0;
        if (centerAngle < 0) centerAngle += 360.0;
        result.put(clasher, centerAngle);
    }

    return result;
}

private static double circularDistanceDeg(double a, double b) {
    double diff = Math.abs(a - b) % 360.0;
    return Math.min(diff, 360.0 - diff);
}

private DomainExpansionEntity getWinnerForBlock(BlockPos blockPos, BlockPos center, Map<DomainExpansionEntity, Double> clasherCenters) {
    if (clasherCenters.isEmpty()) return null;

    if (clasherCenters.size() == 1) {
        return clasherCenters.keySet().iterator().next();
    }

    Vec3 blockDir = blockPos.getCenter().subtract(center.getCenter()).multiply(1, 0, 1);
    double blockAngle = Math.toDegrees(Math.atan2(blockDir.z, blockDir.x));
    if (blockAngle < 0) blockAngle += 360.0;

    DomainExpansionEntity best = null;
    double bestDist = Double.MAX_VALUE;

    for (Map.Entry<DomainExpansionEntity, Double> entry : clasherCenters.entrySet()) {
        double centerAngle = entry.getValue() % 360.0;
        if (centerAngle < 0) centerAngle += 360.0;

        double dist = circularDistanceDeg(blockAngle, centerAngle);
        if (dist < bestDist) {
            bestDist = dist;
            best = entry.getKey();
        }
    }

    return best;
}

    private void createBarrier(boolean instant) {
        createBarrier(instant, null);
    }

    private void createBarrier(boolean instant, @Nullable DomainExpansionEntity leadClasher) {
        createBarrier(instant, leadClasher, false);
    }

    private void createBarrier(boolean instant, @Nullable DomainExpansionEntity leadClasher, boolean lose) {
        int radius = this.getRadius();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));
        Map<DomainExpansionEntity, Double> clasherAngles = getClasherAngles(center);
        List<DomainExpansionEntity> currentClashers = new ArrayList<>(clashers);
        if (clasherAngles.isEmpty()) {
            if (!this.isRemoved()) {
                discardClashers();
                this.discard();
            }
            return;
        }
        Vec3 behind;
        if (leadClasher != null) {
            if (lose) {
                Vec3 clasherPos = leadClasher.position();
                Vec3 centerpos = this.position().add(0.0D, radius, 0.0D);
                Vec3 dirToClasher = centerpos.subtract(clasherPos).normalize();
                behind = centerpos.add(dirToClasher.scale(radius - OFFSET));
            }
            else {
                Vec3 clasherPos = leadClasher.position();
                Vec3 centerPos = this.position().add(0.0D, radius, 0.0D);
                Vec3 dirToClasher = clasherPos.subtract(centerPos).normalize();
                behind = centerPos.add(dirToClasher.scale(radius - OFFSET));
            }
        } else {
            Vec3 direction = this.getLookAngle();
            behind = this.position().subtract(direction.scale(radius - OFFSET)).add(0.0D, radius, 0.0D);
        }
        //Vec3 behind = this.position().subtract(direction.scale(radius - OFFSET)).add(0.0D, radius, 0.0D);
        //  for (int x = -radius; x <= radius; x++) {
        //     for (int y = -radius; y <= radius; y++) {
        //         for (int z = -radius; z <= radius; z++) {
        //             double distance = Math.sqrt(x * x + y * y + z * z);
        //             if (distance > radius) continue;
        for (Vec3i off : getSphereOffsets(radius)) {
                    BlockPos pos = center.offset(off);
                    double distance = off.getX()*off.getX() + off.getY()*off.getY() + off.getZ()*off.getZ();

                    if (!this.level().isInWorldBounds(pos)) continue;
                    int delay = (int) Math.round(pos.getCenter().distanceTo(behind)) / 2 + 1;
                

                        
                        //Vec3 blockDir = pos.getCenter().subtract(center.getCenter()).multiply(1,0,1);
                        //double blockAngle = Math.atan2(blockDir.z, blockDir.x);
                        


                    //DomainExpansionEntity winner = null;
                    // double minDiff = Double.MAX_VALUE;
                    // for (Map.Entry<DomainExpansionEntity, Double> entry : clasherAngles.entrySet()) {
                    //     double diff = Math.abs(wrapDegrees(Math.toDegrees(blockAngle) - Math.toDegrees(entry.getValue())));
                    //     if (diff < minDiff) {
                    //         minDiff = diff;
                    //         winner = entry.getKey();
                    //     }
                    // }

           

                    // // if (existing instanceof DomainSkyBlockEntity sky) {
                    // //     if (sky.getPidentifier() != null && !sky.getPidentifier().equals(winner.getUUID())) {
                    // //         sky.setPidentifier(winner.getUUID());
                    // //         sky.setDomain(JJKAbilities.getKey(winner.getAbility()));
                    // //     }
                    // // }
                    if (instant) {
                        
                            BlockEntity existing = this.level().getBlockEntity(pos);
                        DomainExpansionEntity winner = getWinnerForBlock(pos, center, clasherAngles);
                                 if (winner == null) continue;
                                            if (existing instanceof DomainBlockEntity dbe) {
                        // if (dbe.getPidentifier() != null && dbe.getPidentifier().equals(winner.getUUID())) {
                        //     continue; // already placed by this winner
                        // }
                    }
                        this.createBlock(radius - delay, pos, radius, distance, existing, winner);
                    } else {
                        //final DomainExpansionEntity winnerFinal = winner;
                            this.scheduleBlockPlacement(pos, delay, () -> {
                                boolean same = clashers.size() == currentClashers.size() && clashers.containsAll(currentClashers);
                                DomainExpansionEntity winner = null;
                                if (!same) {
                                        winner = getWinnerForBlock(pos, center, getClasherAngles(center));
                                }
                                else {
                                        winner = getWinnerForBlock(pos, center, clasherAngles);
                                }
                                    BlockEntity existing = this.level().getBlockEntity(pos);
                          
                                         if (winner == null) return;
                    //                                 if (existing instanceof DomainBlockEntity dbe) {
                    //     if (dbe.getPidentifier() != null && dbe.getPidentifier().equals(winner.getUUID())) {
                    //         return; // already placed by this winner
                    //     }
                    //     else if (dbe.getPidentifier() == null) {
                    //         dbe.setPidentifier(winner.getUUID());
                    //         return;
                    //     }
                        
                    // }
                                this.createBlock(radius - delay, pos, radius, distance, existing, winner);
                        });
                    }
                 }
        //     }
        // }


    }

    private void applyPreStun() {
        if (this.level().isClientSide) return;

        int radius = this.getRadius();
        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
            LivingEntity.class, 
            new AABB(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius
            ),
            entity -> this.isInsideBarrier(entity.blockPosition())
        );



      

        for (LivingEntity entity : entities ) {
            JJKEffectUtil.addEffect(entity, new MobEffectInstance(
                JJKEffects.DOMAINSTUN.get(),
                30,
                1,
                false,
                false,
                false
            ));
            JJKEffectUtil.addEffect(entity,new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30,
                6, false, false, false));
        }
    }


    // public boolean isAffected(LivingEntity victim, DomainExpansionEntity winnerDom) {
    //     LivingEntity owner = winnerDom.getOwner();

    //     if (owner == null || victim == owner) {
    //         return false;
    //     }

    //     if (victim instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwner() == owner) return false;

    //     if (!owner.canAttack(victim)) return false;

    //     if (victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
    //         ITenShadowsData victimTenShadowsCap = victim.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElse(null);
    //         ISorcererData victimSorcererCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //         if ((victimTenShadowsCap != null && victim instanceof MahoragaEntity && victimTenShadowsCap.isAdaptedTo(winnerDom.ability))) return false;

    //         if (victimSorcererCap.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get())) {
    //             return false;       
    //         }

    //         if (victimSorcererCap.hasToggled(JJKAbilities.SIMPLE_DOMAIN.get())) {
    //             SimpleDomainEntity simple = victimSorcererCap.getSummonByClass(SimpleDomainEntity.class);

    //             if (simple != null) {
    //                 ISorcererData ownerSorcererCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
    //                 simple.hurt(JJKDamageSources.indirectJujutsuAttack(this, owner, winnerDom.ability), ownerSorcererCap.getAbilityPower() * 10.0F);
    //                 return false;
    //             }
    //         }

    //         for (SimpleDomainEntity simple : this.level().getEntitiesOfClass(SimpleDomainEntity.class, AABB.ofSize(victim.position(), SimpleDomainEntity.MAX_RADIUS * 3, SimpleDomainEntity.MAX_RADIUS * 3, SimpleDomainEntity.MAX_RADIUS * 3))) {
    //             //if (victim.distanceTo(simple) < simple.getRadius()) return false;
    //             if (simple.isInsideBarrier(victim.blockPosition())) return false;
    //         }
    //     }
    //     return this.isAffected(victim.blockPosition());
    // }



    public int getClasherSize() {
        int i = 0;
        for (DomainExpansionEntity clasher : this.clashers) {
            if ( !(clasher instanceof OpenDomainExpansionEntity)) {
                i++;
            }
        }
        return i;
    }

    public DomainExpansionEntity checkSureHitEffect() {
        if (!this.isCompleted()) return null;
        DomainExpansionEntity remainingEntity = null;
        int remainingDomains = 0;
        for (DomainExpansionEntity domain : clashers) {
            if (domain == null || domain.isRemoved() || !domain.isAlive()) continue;

            if (domain.getDomain().shouldCollapse(domain.getStrength())) {
                domain.getDomain().discard();
                continue;
            }
            if (!domain.getSureHitToggled()) {
                continue;
            }

            if (domain.isClashing()) {
                continue;
            }
            remainingDomains++;
            if (remainingDomains > 1) {
                return null;
            }
            remainingEntity = domain;
        }
        return remainingEntity;
    }

    // private int getCrackStage(float ratio) {
    //     if (ratio > 0.0F && ratio <= 0.1F) {
    //         return 5;
    //     } else if (ratio > 0.1 && ratio <= 0.5F) {
    //         return 4;
    //     }
    //     else if (ratio > 0.5 && ratio <= 0.9) {
    //         return 3;
    //     }
    //     else if (ratio > 0.9 && ratio <= 1.0F) {
    //         return 2;
    //     }
    //     return 1;
    // }

    @Override
    public void tick() {
        super.tick();

        this.setTime(this.getTime() + 1);

        if (this.level().isClientSide) return;




        int radius = this.getRadius();
         if (!this.isCompleted() && !this.isRemoved()) {
            BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBounds(),
                    entity -> this.isInsideBarrier(entity.blockPosition()))) {
                if (!this.positions.containsKey(entity.getUUID())) {
                    this.positions.put(entity.getUUID(), entity.position());
                }
                if (entity.getY() < center.getY()) {
                    entity.teleportTo(entity.getX(), center.getY(), entity.getZ());
                    Vec3 motion = entity.getDeltaMovement();
                    if (motion.y < 0) {
                        entity.setDeltaMovement(motion.x, 0.0D, motion.z);
                    }

                    entity.fallDistance = 0;
                }
            }
        }
       
        DomainExpansionEntity sureHit = this.checkSureHitEffect();
        if (sureHit != null) {
            sureHit.doSureHitEffect();
        }
        //  ParticleOptions particle = ((DomainExpansion.IClosedDomain) this.ability).getEnvironmentParticle();

        // if (particle != null) {
        //     AABB bounds = this.getBounds();

        //     for (BlockPos pos : BlockPos.randomBetweenClosed(this.random, 16, (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ,
        //             (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ)) {
        //         if (!this.isWithinBarrier(pos)) continue;
        //         Vec3 center = pos.getCenter();
        //         ((ServerLevel) this.level()).sendParticles(particle, center.x, center.y, center.z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        //     }
        // }
        if (this.isCompleted()) {
              if (!this.level().isClientSide) {
        AABB bounds = this.getBounds();
        Map<DomainExpansionEntity, Double> clasherAngles = getClasherAngles(BlockPos.containing(this.position().add(0.0D, getRadius(), 0.0D)));
        
        for (DomainExpansionEntity clasher : clashers) {
            if (clasher == null) continue;

            ParticleOptions particle = ((DomainExpansion) clasher.getAbility()).getEnvironmentParticle();
            if (particle == null) continue;

            for (BlockPos pos : BlockPos.randomBetweenClosed(this.random, 16,
                    (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ,
                    (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ)) {

                DomainExpansionEntity winner = getWinnerForBlock(pos, BlockPos.containing(this.position().add(0.0D, getRadius(), 0.0D)), clasherAngles);
                if (winner != clasher) continue;

                Vec3 center = pos.getCenter();
                ((ServerLevel) this.level()).sendParticles(particle, center.x, center.y, center.z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
            if (this.getTime() % CHECK_INTERVAL == 0) {
                this.check();
            }
        } else {
            if (this.getTime() == 1) {
                this.applyPreStun();
                this.refreshDimensions();
            }
            //this.createBarrier(false);
        }
        if (!blockTasks.isEmpty()) {
    int placedThisTick = 0;

   

    for (Map.Entry<BlockPos, BlockPlacementTask> entry : blockTasks.entrySet()) {
        BlockPlacementTask task = entry.getValue();
        task.ticks--;

        if (task.ticks <= 0) {
            toRun.add(entry.getKey());
        }
    }

    for (BlockPos pos : toRun) {
        if (placedThisTick >= MAX_BLOCKS_PER_TICK) {
            break;
        }

        BlockPlacementTask task = blockTasks.remove(pos);
        //if (task != null) {
            task.action.run();

            placedThisTick++;
        //}
    }
    toRun.clear();
}
        if (getClasherSize() == 0 && !this.isRemoved() ) {
            this.discard();
        }
    }
    
}

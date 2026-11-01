package radon.jujutsu_kaisen.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.VeilBlock;
import radon.jujutsu_kaisen.block.base.ITemporaryBlockEntity;
import radon.jujutsu_kaisen.block.entity.IVeil;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.block.entity.VeilRodBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.item.veil.modifier.ColorModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.ModifierUtils;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VeilEntity extends Entity implements IVeil {
    private static final EntityDataAccessor<Integer> DATA_TIME = SynchedEntityData.defineId(VeilEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RADIUS = SynchedEntityData.defineId(VeilEntity.class, EntityDataSerializers.INT);

    private static final Map<Integer, List<Vec3i>> SHELL_CACHE = new HashMap<>();

    private static final int BLOCKS_PER_TICK = 4096;

    private static final float COST = 0.0001F;
    private static final int CHECK_INTERVAL = 20;
    private static final int HEAL_INTERVAL = 5 * 20;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;

    private List<Modifier> modifiers;

    @Nullable
    private BlockPos center;

    private int total;

    private int cursor;
    private int lapTotal;

    private int checkCursor;
    private int checkCount;

    private boolean claimed;

    private int knownRadius = -1;

    public VeilEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public VeilEntity(LivingEntity owner, Vec3 pos, int radius, List<Modifier> modifiers) {
        super(JJKEntities.VEIL.get(), owner.level());

        this.setOwner(owner);

        this.setPos(pos.subtract(0.0D, radius, 0.0D));

        this.setRadius(radius);

        this.modifiers = modifiers;
    }

    public VeilEntity(LivingEntity owner, Vec3 pos, int radius, List<Modifier> modifiers, @Nullable BlockPos center) {
        this(owner, pos, radius, modifiers);

        this.center = center;
    }

    @Override
    public VeilEntity getVeil() {
        return this;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TIME, 0);
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

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("owner", this.ownerUUID);
        }
        pCompound.putInt("total", this.total);
        pCompound.putInt("time", this.getTime());
        pCompound.putInt("radius", this.getRadius());
        pCompound.put("modifiers", ModifierUtils.serialize(this.modifiers));

        if (this.center != null) {
            pCompound.put("center", NbtUtils.writeBlockPos(this.center));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (pCompound.hasUUID("owner")) {
            this.ownerUUID = pCompound.getUUID("owner");
        }
        this.total = pCompound.getInt("total");
        this.setTime(pCompound.getInt("time"));
        this.setRadius(pCompound.getInt("radius"));
        this.modifiers = ModifierUtils.deserialize(pCompound.getList("modifiers", CompoundTag.TAG_LIST));

        if (pCompound.contains("center")) {
            this.center = NbtUtils.readBlockPos(pCompound.getCompound("center"));
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

    public void setOwner(@Nullable LivingEntity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public BlockPos getBarrierCenter() {
        return BlockPos.containing(this.position().add(0.0D, this.getRadius(), 0.0D));
    }

    @Override
    public boolean isBarrier(BlockPos pos) {
        int radius = this.getRadius();
        BlockPos relative = pos.subtract(this.getBarrierCenter());
        return Math.sqrt(relative.distSqr(Vec3i.ZERO)) >= radius - 1 && Math.sqrt(relative.distSqr(Vec3i.ZERO)) <= radius;
    }

    @Override
    public boolean isInsideBarrier(BlockPos pos) {
        int radius = this.getRadius();
        BlockPos relative = pos.subtract(this.getBarrierCenter());
        return Math.sqrt(relative.distSqr(Vec3i.ZERO)) < radius - 1;
    }

    @Override
    public boolean isBarrierOrInside(BlockPos pos) {
        return this.isBarrier(pos) || this.isInsideBarrier(pos);
    }

    @Override
    public AABB getBounds() {
        return this.getBoundingBox();
    }

    @Override
    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    
    private void removeBarrier() {
        int radius = this.getRadius();

        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

        for (Vec3i off : getShellOffsets(radius)) {
            BlockPos pos = center.offset(off);

            if (!(this.level().getBlockEntity(pos) instanceof VeilBlockEntity veil)) continue;

            UUID parent = veil.getParentUUID();

            if (parent != null && parent.equals(this.getUUID())) {
                veil.destroy();
            }
        }
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        if (!this.level().isClientSide && (pReason == RemovalReason.DISCARDED || pReason == RemovalReason.KILLED)) {
            this.removeBarrier();
        }

        super.remove(pReason);
    }

    private boolean charge(LivingEntity owner) {
        if (!(owner instanceof Player player) || !player.getAbilities().instabuild) {
            ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            float cost = COST * (ownerCap.hasTrait(Trait.SIX_EYES) ? 0.5F : 1.0F);

            if (ownerCap.getEnergy() < cost) return false;

            ownerCap.useEnergy(cost);

            if (owner instanceof ServerPlayer player) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(ownerCap.serializeNBT()), player);
            }
        }
        return true;
    }

    private void createBarrier() {
        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        BlockState replacement = JJKBlocks.VEIL.get().defaultBlockState();

        for (Modifier modifier : this.modifiers) {
            if (modifier.getType() == Modifier.Type.COLOR) {
                replacement = replacement.setValue(VeilBlock.COLOR, ((ColorModifier) modifier).getColor());
            } else if (modifier.getType() == Modifier.Type.TRANSPARENT) {
                replacement = replacement.setValue(VeilBlock.TRANSPARENT, true);
            }
        }

        int radius = this.getRadius();

        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

        if (this.getTime() - 1 == 0) {
            int diameter = radius * 2;

            // for (CursedSpirit curse : this.level().getEntitiesOfClass(CursedSpirit.class,
            //         AABB.ofSize(this.blockPosition().getCenter(), diameter, diameter, diameter))) {
            //     curse.setHiding(false);
            // }
        }

        List<Vec3i> shell = getShellOffsets(radius);

        int minY = radius - this.getTime();
        int budget = BLOCKS_PER_TICK;

        while (budget-- > 0 && this.cursor < shell.size()) {
            Vec3i off = shell.get(this.cursor);

            if (off.getY() < minY) break;

            this.cursor++;

            int delay = radius - off.getY();

            BlockPos pos = center.offset(off);

            if (!this.level().isInWorldBounds(pos)) continue;

            BlockState state = this.level().getBlockState(pos);

            if (state.is(Blocks.BEDROCK)) continue;

            BlockEntity existing = this.level().getBlockEntity(pos);

            CompoundTag saved = null;

            if (existing != null && !(existing instanceof ITemporaryBlockEntity)) {
                saved = existing.saveWithFullMetadata();
            }

            if (existing instanceof VeilBlockEntity be) {
                this.lapTotal++;

                if (be.getParentUUID() != null && be.getParentUUID().equals(this.getUUID())) continue;
            } else {
                if (!this.charge(owner)) continue;
                if (existing != null) {
                    ((ServerLevel) this.level()).removeBlockEntity(pos);
                }
                if (this.level().setBlock(pos, replacement,
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS)) this.lapTotal++;
            }

            if (existing instanceof ITemporaryBlockEntity tmp) {
                state = tmp.getOriginal();
                saved = tmp.getSaved();
            }

            if (this.level().getBlockEntity(pos) instanceof VeilBlockEntity be) {
                be.create(this.getUUID(), this.ownerUUID, (radius * 2) - delay, radius, this.modifiers, state, saved);
            }
        }

        if (this.cursor >= shell.size()) {
            this.total = this.lapTotal;
            this.lapTotal = 0;
        }
    }

    private static List<Vec3i> getShellOffsets(int radius) {
        return SHELL_CACHE.computeIfAbsent(radius, r -> {
            List<Vec3i> offsets = new ArrayList<>();

            int outerSqr = r * r;
            int innerSqr = (r - 1) * (r - 1);

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    int planar = x * x + y * y;

                    if (planar >= outerSqr) continue;

                    int max = (int) Math.sqrt(outerSqr - planar) + 1;
                    int min = planar >= innerSqr ? 0 : Math.max(0, (int) Math.sqrt(innerSqr - planar) - 1);

                    for (int z = min; z <= max; z++) {
                        double distance = Math.sqrt(planar + z * z);

                        if (distance >= r || distance < r - 1) continue;

                        offsets.add(new Vec3i(x, y, z));

                        if (z != 0) offsets.add(new Vec3i(x, y, -z));
                    }
                }
            }
            offsets.sort((a, b) -> Integer.compare(b.getY(), a.getY()));

            return offsets;
        });
    }
    
    public BlockPos getCenter() {
        return this.center;
    }

    private void check() {
        // if (this.center != null) {
        //     if (!this.level().getBlockState(this.center).is(JJKBlocks.VEIL_ROD.get())) {
        //         this.discard();
        //         return;
        //     }
        // }

        int radius = this.getRadius();

        BlockPos center = BlockPos.containing(this.position().add(0.0D, radius, 0.0D));

        List<Vec3i> shell = getShellOffsets(radius);

        int budget = BLOCKS_PER_TICK;

        while (budget-- > 0 && this.checkCursor < shell.size()) {
            if (this.level().getBlockEntity(center.offset(shell.get(this.checkCursor))) instanceof VeilBlockEntity) {
                this.checkCount++;
            }
            this.checkCursor++;
        }

        if (this.checkCursor < shell.size()) return;

        int count = this.checkCount;

        this.checkCursor = 0;
        this.checkCount = 0;

        if (this.total <= 0) return;

        if ((float) count / this.total < 0.75F) {
            this.discard();
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
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        int radius = this.getRadius() * 2;
        return EntityDimensions.fixed(radius, radius);
    }

    @Override
    protected void onBelowWorld() {

    }

    @Override
    public void tick() {
        super.tick();

        this.setTime(this.getTime() + 1);

        int radius = this.getRadius();

        if (this.knownRadius != radius) {
            this.knownRadius = radius;
            this.refreshDimensions();
        }

        if (this.level().isClientSide) return;

        boolean completed = this.getTime() >= radius * 2;

        if (this.getTime() % CHECK_INTERVAL == 0) {
            this.claimed = IncompleteDomainEntity.isClaimed((ServerLevel) this.level(), this);

            if (this.claimed) {
                this.checkCursor = 0;
                this.checkCount = 0;
            }
        }

        if (this.claimed) return;

        if (completed) {
            if (this.checkCursor > 0 || this.getTime() % CHECK_INTERVAL == 0) {
                this.check();
            }

            if (this.getTime() % HEAL_INTERVAL == 0) {
                this.cursor = 0;
                this.lapTotal = 0;
            }
        }

        this.createBarrier();
    }
}

package radon.jujutsu_kaisen.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.ability.cursed_speech.CursedSpeech;
import radon.jujutsu_kaisen.ability.misc.IncompleteDomain;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.util.RotationUtil;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.network.packet.s2c.EquipAbilityS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.base.ITemporaryBlockEntity;
import radon.jujutsu_kaisen.block.entity.DomainBlockEntity;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.block.entity.IVeil;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.VeilEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IncompleteDomainEntity extends DomainExpansionEntity implements IDomainBarrier {
    private static final EntityDataAccessor<BlockPos> DATA_MIN =
            SynchedEntityData.defineId(IncompleteDomainEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> DATA_MAX =
            SynchedEntityData.defineId(IncompleteDomainEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final int BLOCKS_PER_TICK = 256;

    private static final float CLASH_HEALTH = 250.0F;
    private static final float CLASH_DAMAGE = 10.0F;

    private static final int BREACH_REACH = 2;

    private static final int PULL_REACH = 2;

    private static final int SHELTER_HEIGHT = 3;

    private static final int DISABLE_DURATION = 60;

    private static final int DISABLE_REFRESH = 20;

    private static final int STEAL_INTERVAL = 10 * 20;

    private static final int MARKER_COUNT = 5;

    private static final int MARKER_INTERVAL = 5;

    private static final double MARKER_REACH = 2.5D;

    private static final double SWAP_RANGE = 50.0D;

    private static final Vector3f MARKER_COLOR = new Vector3f(0.45F, 0.80F, 1.0F);

    private static final int SPEAK_INTERVAL = 15 * 20;

    private final List<DomainExpansionEntity> clashers = new ArrayList<>();

    private final List<BlockPos> replacedPositions = new ArrayList<>();

    private final List<BlockPos> breachPositions = new ArrayList<>();

    private boolean incomplete = true;

    private List<BlockPos> pending;

    private Set<BlockPos> floors;

    private Set<BlockPos> decorations;

    private int placed;
    private boolean formed;

    @Nullable
    private UUID claimedVeil;

    private final List<BlockPos> markers = new ArrayList<>();

    @Nullable
    private UUID carried;

    private int spoken = -1;
    private int command;

    private int stole = -1;

    public IncompleteDomainEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public IncompleteDomainEntity(LivingEntity owner, DomainExpansion ability, IncompleteDomain.Room room) {
        this(owner, ability, room, true, true);
    }

    public IncompleteDomainEntity(LivingEntity owner, DomainExpansion ability, IncompleteDomain.Room room, boolean incomplete) {
        this(owner, ability, room, incomplete, true);
    }

    public IncompleteDomainEntity(LivingEntity owner, DomainExpansion ability, IncompleteDomain.Room room,
                                  boolean incomplete, boolean seals) {
        super(JJKEntities.INCOMPLETE_DOMAIN.get(), owner, ability);

        this.incomplete = incomplete;
        this.setClashHealth(this.getMaxClashHealth());

        this.formed = !seals;

        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());

        this.entityData.set(DATA_MIN, room.min());
        this.entityData.set(DATA_MAX, room.max());

        this.clashers.add(this);

        VeilEntity veil = VeilHandler.getVeilAt(owner.level(), owner.blockPosition());

        if (veil != null) {
            this.claimedVeil = veil.getUUID();
        }
    }

    @Nullable
    private VeilEntity claimed() {
        if (this.claimedVeil == null || !(this.level() instanceof ServerLevel level)) return null;

        return level.getEntity(this.claimedVeil) instanceof VeilEntity veil && !veil.isRemoved() ? veil : null;
    }

    public static boolean isClaimed(ServerLevel level, VeilEntity veil) {
        for (IncompleteDomainEntity domain : level.getEntitiesOfClass(IncompleteDomainEntity.class,
                veil.getBoundingBox().inflate(veil.getRadius()))) {
            if (domain.isRemoved()) continue;
            if (veil.getUUID().equals(domain.claimedVeil)) return true;
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_MIN, BlockPos.ZERO);
        this.entityData.define(DATA_MAX, BlockPos.ZERO);
    }

    public BlockPos getMin() {
        return this.entityData.get(DATA_MIN);
    }

    public BlockPos getMax() {
        return this.entityData.get(DATA_MAX);
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
    public int getRadius() {
        BlockPos min = this.getMin();
        BlockPos max = this.getMax();

        int longest = Math.max(max.getX() - min.getX(), Math.max(max.getY() - min.getY(), max.getZ() - min.getZ()));

        return Math.max(1, longest / 2);
    }

    @Override
    public AABB getBounds() {
        return new AABB(this.getMin(), this.getMax().offset(1, 1, 1));
    }

    @Override
    public boolean isBarrier(BlockPos pos) {
        BlockPos min = this.getMin();
        BlockPos max = this.getMax();

        if (pos.getX() < min.getX() || pos.getX() > max.getX()) return false;
        if (pos.getY() < min.getY() || pos.getY() > max.getY()) return false;
        if (pos.getZ() < min.getZ() || pos.getZ() > max.getZ()) return false;

        return pos.getX() == min.getX() || pos.getX() == max.getX()
                || pos.getY() == min.getY() || pos.getY() == max.getY()
                || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
    }

    @Override
    public boolean isInsideBarrier(BlockPos pos) {
        VeilEntity veil = this.claimed();

        if (veil != null) return veil.isInsideBarrier(pos);

        BlockPos min = this.getMin();
        BlockPos max = this.getMax();

        return pos.getX() > min.getX() && pos.getX() < max.getX()
                && pos.getY() > min.getY() && pos.getY() < max.getY()
                && pos.getZ() > min.getZ() && pos.getZ() < max.getZ();
    }

    @Override
    public boolean isBarrierOrInside(BlockPos pos) {
        return this.isBarrier(pos) || this.isInsideBarrier(pos);
    }

    public boolean isCompleted() {
        return this.formed;
    }

    public boolean isIncomplete() {
        return this.incomplete;
    }

    @Override
    public float getMaxClashHealth() {
        return CLASH_HEALTH;
    }

    @Override
    public float getClashDamage() {
        return CLASH_DAMAGE;
    }

    @Override
    public DomainExpansionEntity checkSureHitEffect() {
        if (!this.getSureHitToggled()) return null;

        if (this.isClashing()) return null;

        if (!this.isCompleted()) return null;

        return this;
    }

    @Override
    public DomainExpansionEntity sureHitTarget(LivingEntity target) {
        DomainExpansionEntity surehit = this.checkSureHitEffect();

        if (surehit != null && surehit.isAffected(target, false)) return surehit;

        return null;
    }

    private boolean isProtectedByForeignVeil(BlockPos pos) {
        if (!(this.level() instanceof ServerLevel level)) return false;

        VeilEntity claimed = this.claimed();

        for (IVeil veil : VeilHandler.getVeils(level, pos)) {
            if (veil == claimed) continue;

            int radius = veil.getRadius();

            if (pos.distSqr(veil.getVeil().getCenter()) < (double) radius * radius) return true;
        }
        return false;
    }

    @Override
    public boolean isAffected(BlockPos pos) {
        if (this.isProtectedByForeignVeil(pos)) return false;

        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers((ServerLevel) this.level(), pos)) {
            if (barrier != this) return false;
        }
        return super.isAffected(pos);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        this.tickBreach();

        if (!this.formed) {
            this.sealSurfaces();
            return;
        }

        if (this.checkSureHitEffect() != null) {
            this.doSureHitEffect();
        }
    }

    private void tickBreach() {
        if (!this.incomplete) return;

        DomainExpansionEntity opponent = this.getCompleteOpponent();

        if (opponent == null) {
            this.closeBreach();
            return;
        }

        this.openBreach();
        this.pullOutsidersIn(opponent);
    }

    @Nullable
    private DomainExpansionEntity getCompleteOpponent() {
        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers((ServerLevel) this.level(), this.getBounds())) {
            for (DomainExpansionEntity domain : barrier.getClashers()) {
                if (domain == this || domain.isRemoved()) continue;

                if (domain instanceof IncompleteDomainEntity other && other.isIncomplete()) continue;

                if (domain instanceof OpenDomainExpansionEntity) continue;

                return domain;
            }
        }
        return null;
    }

    private void openBreach() {
        if (!this.breachPositions.isEmpty()) return;

        ServerLevel level = (ServerLevel) this.level();
        BlockPos centre = this.blockPosition().below();

        for (int dx = -BREACH_REACH; dx <= BREACH_REACH; dx++) {
            for (int dz = -BREACH_REACH; dz <= BREACH_REACH; dz++) {
                BlockPos pos = centre.offset(dx, 0, dz);

                if (this.replace(pos, level, JJKBlocks.INCOMPLETE_BARRIER.get(), true)) {
                    this.breachPositions.add(pos.immutable());
                }
            }
        }
    }

    private void closeBreach() {
        if (this.breachPositions.isEmpty()) return;

        if (!this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();

            for (BlockPos pos : this.breachPositions) {
                if (!(level.getBlockEntity(pos) instanceof DomainBlockEntity dbe)) continue;

                UUID identifier = dbe.getIdentifier();

                if (identifier != null && identifier.equals(this.getUUID())) {
                    dbe.destroy();
                    this.clearOrphanedBlock(level, pos);
                }
            }
        }
        this.breachPositions.clear();
    }

    private void clearOrphanedBlock(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof DomainBlockEntity restored)) return;

        UUID identifier = restored.getIdentifier();

        if (identifier != null && level.getEntity(identifier) instanceof DomainExpansionEntity domain
                && !domain.isRemoved() && domain.isAlive()) {
            return;
        }
        restored.destroy();
    }

    @Override
    public Ability getToggledAbility() {
        return JJKAbilities.INCOMPLETE_DOMAIN.get();
    }

    @Override
    public void doSureHitEffect() {
        if (this.ability != null) {
            super.doSureHitEffect();
            return;
        }

        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || cap.getTechnique() == null) return;

        switch (cap.getTechnique()) {
            case ANGEL -> this.disableTechniques();
            case MIMICRY -> this.stealAbility(owner, cap);
            case BOOGIE_WOOGIE -> this.markFloor();
            case CURSED_SPEECH -> this.speak(owner);
            default -> { }
        }
    }

    private void markFloor() {
        if (this.markers.isEmpty()) this.scatterMarkers();

        if (this.getTime() % MARKER_INTERVAL == 0) this.showMarkers();
    }

    private void scatterMarkers() {
        List<BlockPos> candidates = this.markerCandidates();

        this.markers.clear();

        for (int i = 0; i < MARKER_COUNT * 4 && this.markers.size() < MARKER_COUNT; i++) {
            BlockPos pos = this.pickMarker(candidates);

            if (pos != null && !this.markers.contains(pos)) this.markers.add(pos);
        }
    }

    private List<BlockPos> markerCandidates() {
        List<BlockPos> candidates = new ArrayList<>();

        if (this.floors != null) {
            for (BlockPos floor : this.floors) {
                candidates.add(floor.above());
            }
        }

        if (candidates.isEmpty() && this.decorations != null) candidates.addAll(this.decorations);

        return candidates;
    }

    @Nullable
    private BlockPos pickMarker(List<BlockPos> candidates) {
        if (!candidates.isEmpty()) {
            return candidates.get(this.random.nextInt(candidates.size()));
        }

        BlockPos min = this.getMin();
        BlockPos max = this.getMax();

        int width = max.getX() - min.getX();
        int depth = max.getZ() - min.getZ();

        if (width <= 1 || depth <= 1) return null;

        return new BlockPos(min.getX() + 1 + this.random.nextInt(width - 1),
                min.getY() + 1,
                min.getZ() + 1 + this.random.nextInt(depth - 1));
    }

    private void showMarkers() {
        if (!(this.level() instanceof ServerLevel level)) return;

        for (BlockPos pos : this.markers) {
            level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(MARKER_COLOR, 0.6F, 0.8F, 12),
                    pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D,
                    6, 0.3D, 0.4D, 0.3D, 0.0D);
        }
    }

    @Nullable
    public static IncompleteDomainEntity boogieWoogie(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return null;

        for (IncompleteDomainEntity domain : level.getEntitiesOfClass(IncompleteDomainEntity.class,
                owner.getBoundingBox().inflate(IncompleteDomain.MAX_SIZE))) {
            if (domain.isRemoved() || !domain.isCompleted()) continue;
            if (domain.markers.isEmpty() || domain.getOwner() != owner) continue;

            return domain;
        }
        return null;
    }

    public boolean swap(LivingEntity owner) {
        if (!(this.level() instanceof ServerLevel level)) return false;

        LivingEntity looked = RotationUtil.getExpandedLookAt(owner, SWAP_RANGE);

        if (looked != null && looked != owner && this.isInsideBarrier(looked.blockPosition())) {
            this.carried = looked.getUUID();

            level.playSound(null, looked.getX(), looked.getY(), looked.getZ(),
                    JJKSounds.CLAP.get(), SoundSource.MASTER, 1.0F, 1.4F);

            return true;
        }

        BlockPos mark = this.lookedAtMarker(owner);

        if (mark == null) return false;

        if (this.carried == null) return true;

        if (!(level.getEntity(this.carried) instanceof LivingEntity held) || held.isRemoved()) {
            this.carried = null;
            return true;
        }

        level.playSound(null, held.getX(), held.getY(), held.getZ(),
                JJKSounds.CLAP.get(), SoundSource.MASTER, 2.0F, 1.0F);

        held.teleportTo(mark.getX() + 0.5D, mark.getY(), mark.getZ() + 0.5D);

        level.playSound(null, mark.getX(), mark.getY(), mark.getZ(),
                JJKSounds.CLAP.get(), SoundSource.MASTER, 2.0F, 1.0F);

        this.markers.remove(mark);

        BlockPos replacement = this.pickMarker(this.markerCandidates());

        if (replacement != null) this.markers.add(replacement);

        return true;
    }

    @Nullable
    private BlockPos lookedAtMarker(LivingEntity owner) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        BlockPos best = null;
        double closest = MARKER_REACH * MARKER_REACH;

        for (BlockPos pos : this.markers) {
            Vec3 offset = pos.getCenter().subtract(eye);

            double along = offset.dot(look);

            if (along <= 0.0D || along > SWAP_RANGE) continue;

            double across = offset.subtract(look.scale(along)).lengthSqr();

            if (across > closest) continue;

            best = pos;
            closest = across;
        }
        return best;
    }

    private void speak(LivingEntity owner) {
        if (this.spoken >= 0 && this.getTime() - this.spoken < SPEAK_INTERVAL) return;

        this.spoken = this.getTime();

        List<CursedSpeech> vocabulary = new ArrayList<>();

        for (Ability ability : CursedTechnique.CURSED_SPEECH.getAbilities()) {
            if (ability instanceof CursedSpeech speech) vocabulary.add(speech);
        }

        if (vocabulary.isEmpty()) return;

        List<Entity> audience = new ArrayList<>(this.getAffected());

        if (audience.isEmpty()) return;

        CursedSpeech next = vocabulary.get(this.command % vocabulary.size());

        this.command++;

        CursedSpeech.speak(owner, next, audience);

        if (owner instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(
                    String.format("chat.%s.domain_speech", JujutsuKaisen.MOD_ID), next.getName()));
        }
    }

    private void returnBorrowed() {
        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || cap.getBorrowed().isEmpty()) return;

        cap.clearBorrowed();

        if (owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new EquipAbilityS2CPacket(EquipAbilityS2CPacket.NOTHING), player);
        }
    }

    private void stealAbility(LivingEntity owner, ISorcererData cap) {
        if (this.stole >= 0 && this.getTime() - this.stole < STEAL_INTERVAL) return;

        this.stole = this.getTime();

        List<LivingEntity> victims = new ArrayList<>();

        for (LivingEntity entity : this.getAffected()) {
            ISorcererData data = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (data == null || data.getTechnique() == null) continue;

            victims.add(entity);
        }

        if (victims.isEmpty()) return;

        LivingEntity victim = victims.get(this.random.nextInt(victims.size()));

        ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (victimCap == null || victimCap.getTechnique() == null) return;

        List<Ability> stealable = new ArrayList<>();

        for (Ability ability : victimCap.getTechnique().getAbilities()) {
            if (ability.getMenuType() != MenuType.MELEE && ability.getMenuType() != MenuType.J2TSU) continue;
            if (cap.getBorrowed().contains(ability)) continue;

            stealable.add(ability);
        }

        if (stealable.isEmpty()) return;

        Ability stolen = stealable.get(this.random.nextInt(stealable.size()));

        cap.borrow(stolen);

        if (!(owner instanceof ServerPlayer player)) return;

        ResourceLocation key = JJKAbilities.getKey(stolen);

        if (key != null) PacketHandler.sendToClient(new EquipAbilityS2CPacket(key), player);

        player.sendSystemMessage(Component.translatable(
                String.format("chat.%s.mimicry_ability", JujutsuKaisen.MOD_ID), stolen.getName()));
    }

    private void disableTechniques() {
        if (this.getTime() % DISABLE_REFRESH != 0) return;

        for (LivingEntity victim : this.getAffected()) {
            victim.getCapability(SorcererDataHandler.INSTANCE).ifPresent(victimCap -> {
                victimCap.setDisable(DISABLE_DURATION);

                if (victim instanceof ServerPlayer player) {
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(victimCap.serializeNBT()), player);
                }
            });
        }
    }

    public boolean isSheltered(BlockPos pos) {
        if (this.breachPositions.isEmpty()) return false;

        BlockPos centre = this.blockPosition().below();

        if (Math.abs(pos.getX() - centre.getX()) > BREACH_REACH) return false;
        if (Math.abs(pos.getZ() - centre.getZ()) > BREACH_REACH) return false;

        int above = pos.getY() - centre.getY();

        return above >= 0 && above <= SHELTER_HEIGHT;
    }

    public static boolean isShelteredFromSureHit(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers(serverLevel)) {
            for (DomainExpansionEntity domain : barrier.getClashers()) {
                if (!(domain instanceof IncompleteDomainEntity incomplete)) continue;
                if (incomplete.isRemoved() || !incomplete.isIncomplete()) continue;

                if (incomplete.isSheltered(pos)) return true;
            }
        }
        return false;
    }

    private void pullOutsidersIn(DomainExpansionEntity complete) {
        AABB bounds = complete.getBounds();

        if (bounds == null) return;

        for (Player player : this.level().getEntitiesOfClass(Player.class, bounds.inflate(PULL_REACH))) {
            if (complete.isInsideBarrier(player.blockPosition())) continue;
            if (!isTouchingBarrier(complete, player.blockPosition())) continue;

            BlockPos centre = this.blockPosition();

            player.teleportTo(centre.getX() + 0.5D, centre.getY(), centre.getZ() + 0.5D);
        }
    }

    private static boolean isTouchingBarrier(DomainExpansionEntity complete, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(
                pos.offset(-PULL_REACH, -PULL_REACH, -PULL_REACH),
                pos.offset(PULL_REACH, PULL_REACH, PULL_REACH))) {
            if (complete.isBarrier(nearby)) return true;
        }
        return false;
    }

    private void sealSurfaces() {
        ServerLevel level = (ServerLevel) this.level();

        if (this.pending == null) {
            this.pending = this.collectSurfaces();
        }

        int budget = BLOCKS_PER_TICK;

        while (this.placed < this.pending.size() && budget-- > 0) {
            this.seal(this.pending.get(this.placed++), level);
        }

        if (this.placed >= this.pending.size()) {
            this.formed = true;
            this.pending = null;
        }
    }

    private List<BlockPos> collectSurfaces() {
        BlockPos min = this.getMin();
        BlockPos max = this.getMax();

        BlockPos centre = new BlockPos((min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2, (min.getZ() + max.getZ()) / 2);

        int radius = Math.max(1, (max.getX() - min.getX()) / 2);

        Set<BlockPos> interior = this.floodInterior(centre, radius);

        this.floors = new HashSet<>();
        this.decorations = new HashSet<>();

        Set<BlockPos> shell = new HashSet<>();

        for (BlockPos pos : interior) {
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);

                if (interior.contains(next)) continue;

                shell.add(next);

                if (direction == Direction.DOWN) this.floors.add(next);
            }
        }

        if (this.ability != null && !this.ability.getDecorationBlocks().isEmpty()) {
            for (BlockPos floor : this.floors) {
                BlockPos above = floor.above();

                if (interior.contains(above)) this.decorations.add(above);
            }
        }

        List<BlockPos> result = new ArrayList<>(shell);
        result.addAll(this.decorations);
        result.sort(Comparator.comparingDouble(pos -> pos.distSqr(centre)));

        return result;
    }

    private static final int WIDEST = 16;

    private static final int NECK = 2;

    private static boolean wall(ServerLevel level, BlockPos pos, Set<BlockPos> own) {
        return IncompleteDomain.isSolid(level, pos) && !own.contains(pos);
    }

    private static int width(ServerLevel level, BlockPos pos, Set<BlockPos> own,
                             BlockPos.MutableBlockPos cursor) {
        int narrowest = WIDEST;

        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            int span = 1;

            for (Direction direction : new Direction[]{
                    Direction.get(Direction.AxisDirection.NEGATIVE, axis),
                    Direction.get(Direction.AxisDirection.POSITIVE, axis)}) {
                cursor.set(pos);

                for (int step = 0; step < WIDEST; step++) {
                    cursor.move(direction);

                    if (wall(level, cursor, own)) break;

                    span++;
                }
            }
            narrowest = Math.min(narrowest, span);
        }
        return narrowest;
    }

    private Set<BlockPos> floodInterior(BlockPos centre, int radius) {
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, Integer> widths = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        ServerLevel level = (ServerLevel) this.level();

        Set<BlockPos> own = new HashSet<>(this.replacedPositions);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        visited.add(centre);
        widths.put(centre, width(level, centre, own, cursor));
        queue.add(centre);

        int limit = radius * radius;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            int here = widths.get(current);

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);

                if (visited.contains(next)) continue;
                if (next.distSqr(centre) > limit) continue;

                if (IncompleteDomain.isSolid(level, next) && !own.contains(next)) continue;

                int beyond = width(level, next, own, cursor);

                if (beyond * NECK <= here) continue;

                visited.add(next);
                widths.put(next, Math.max(beyond, here));
                queue.add(next);
            }
        }
        return visited;
    }

    private void seal(BlockPos pos, ServerLevel level) {
        Block block = this.blockFor(pos);

        if (block == null) return;

        if (this.replace(pos, level, block, false)) {
            this.replacedPositions.add(pos.immutable());
        }
    }

    @Nullable
    private Block blockFor(BlockPos pos) {
        if (this.ability == null) return JJKBlocks.DOMAIN.get();

        if (this.floors != null && this.floors.contains(pos)) {
            List<Block> bottom = this.ability.getBottomFloorBlocks();

            if (!bottom.isEmpty()) return this.pick(bottom);

            List<Block> floor = this.ability.getFloorBlocks();

            if (!floor.isEmpty()) return this.pick(floor);

            List<Block> fill = this.ability.getFillBlocks();

            if (!fill.isEmpty()) return this.pick(fill);

            return JJKBlocks.DOMAIN.get();
        }

        if (this.decorations != null && this.decorations.contains(pos)) {
            List<Block> decoration = this.ability.getDecorationBlocks();

            return decoration.isEmpty() ? null : this.pick(decoration);
        }

        List<Block> shell = this.ability.getBlocks();

        return shell.isEmpty() ? JJKBlocks.DOMAIN.get() : this.pick(shell);
    }

    private Block pick(List<Block> blocks) {
        return blocks.get(this.random.nextInt(blocks.size()));
    }

    private boolean replace(BlockPos pos, ServerLevel level, Block block, boolean snapshot) {
        if (this.isRemoved()) return false;
        if (!level.isInWorldBounds(pos)) return false;

        BlockState current = level.getBlockState(pos);

        if (current.is(Blocks.BEDROCK)) return false;

        if (VeilHandler.isProtected(level, pos)) return false;

        BlockEntity existing = level.getBlockEntity(pos);

        if (existing instanceof DomainBlockEntity dbe) {
            UUID identifier = dbe.getIdentifier();

            if (identifier != null && identifier.equals(this.getUUID()) && current.is(block)) return false;
        }

        CompoundTag saved = null;

        if (!snapshot && existing instanceof ITemporaryBlockEntity tmp) {
            current = tmp.getOriginal();
            saved = tmp.getSaved();
        } else if (existing != null) {
            saved = existing.saveWithFullMetadata();
        }

        if (current == null) {
            current = Blocks.AIR.defaultBlockState();
        }

        if (existing != null) {
            level.removeBlockEntity(pos);
        }

        level.setBlock(pos, block.defaultBlockState(),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);

        if (!(level.getBlockEntity(pos) instanceof DomainBlockEntity dbe)) return false;

        dbe.create(this.getUUID(), 0, current, saved, this.getUUID());

        return true;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        if (this.claimedVeil != null) {
            pCompound.putUUID("claimed_veil", this.claimedVeil);
        }
        pCompound.put("min", NbtUtils.writeBlockPos(this.getMin()));
        pCompound.put("max", NbtUtils.writeBlockPos(this.getMax()));
        pCompound.putBoolean("formed", this.formed);
        pCompound.putBoolean("incomplete", this.incomplete);
        pCompound.putInt("placed", this.placed);

        ListTag positionsList = new ListTag();

        for (BlockPos pos : this.replacedPositions) {
            positionsList.add(NbtUtils.writeBlockPos(pos));
        }
        pCompound.put("replaced_positions", positionsList);

        ListTag breachList = new ListTag();

        for (BlockPos pos : this.breachPositions) {
            breachList.add(NbtUtils.writeBlockPos(pos));
        }
        pCompound.put("breach_positions", breachList);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.hasUUID("claimed_veil")) {
            this.claimedVeil = pCompound.getUUID("claimed_veil");
        }
        this.entityData.set(DATA_MIN, NbtUtils.readBlockPos(pCompound.getCompound("min")));
        this.entityData.set(DATA_MAX, NbtUtils.readBlockPos(pCompound.getCompound("max")));
        this.formed = pCompound.getBoolean("formed");
        this.incomplete = !pCompound.contains("incomplete") || pCompound.getBoolean("incomplete");
        this.placed = pCompound.getInt("placed");

        this.replacedPositions.clear();

        for (Tag tag : pCompound.getList("replaced_positions", Tag.TAG_COMPOUND)) {
            this.replacedPositions.add(NbtUtils.readBlockPos((CompoundTag) tag));
        }

        this.breachPositions.clear();

        for (Tag tag : pCompound.getList("breach_positions", Tag.TAG_COMPOUND)) {
            this.breachPositions.add(NbtUtils.readBlockPos((CompoundTag) tag));
        }

        if (this.clashers.isEmpty()) {
            this.clashers.add(this);
        }
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        if (!this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();

            this.closeBreach();

            if (this.claimedVeil != null && level.getEntity(this.claimedVeil) instanceof VeilEntity veil
                    && !veil.isRemoved()) {
                veil.discard();
            }

            this.returnBorrowed();

            for (BlockPos pos : this.replacedPositions) {
                if (!(level.getBlockEntity(pos) instanceof DomainBlockEntity dbe)) continue;

                UUID identifier = dbe.getIdentifier();

                if (identifier != null && identifier.equals(this.getUUID())) {
                    dbe.destroy();
                }
            }
            this.replacedPositions.clear();
        }
        this.pending = null;

        super.remove(pReason);
    }

    @Override
    public void push(@NotNull Entity pEntity) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}

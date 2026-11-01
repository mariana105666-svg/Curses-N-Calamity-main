package radon.jujutsu_kaisen.ability.misc;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.ability.AbilityDisplayInfo;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.entity.VeilEntity;
import java.util.UUID;
import radon.jujutsu_kaisen.entity.IncompleteDomainEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class IncompleteDomain extends Ability implements Ability.IToggled {
    private static final int POINTS_COST = 95;

    public static final int MAX_SIZE = 50;

    public static final int MAX_RADIUS = MAX_SIZE / 2;

    private static final float ENCLOSURE_THRESHOLD = 0.75F;

    private static final int FLOOR_REACH = 2;

    private static final float FOREIGN_VEIL_COST = 200.0F;

    private static final int TOKEN_RADIUS = 4;

    public record Room(BlockPos min, BlockPos max) {
        public int width() {
            return this.max.getX() - this.min.getX() + 1;
        }

        public int height() {
            return this.max.getY() - this.min.getY() + 1;
        }

        public int depth() {
            return this.max.getZ() - this.min.getZ() + 1;
        }
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isDomain() {
        return true;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.DOMAIN;
    }

    @Override
    public boolean canDisable() {
        return false;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return Status.FAILURE;

        if (cap.hasSummonOfClass(DomainExpansionEntity.class)) return Status.FAILURE;

        Status status = super.isTriggerable(owner);

        if (status != Status.SUCCESS) return status;

        if (owner.level().isClientSide) return status;

        AbilityHandler.untrigger(owner, JJKAbilities.FOREST_PLATFORM.get());

        String[] reason = new String[1];

        if (scan(owner, reason) == null && findCompleteDomain(owner) == null) {
            this.explain(owner, reason[0]);

            return Status.FAILURE;
        }

        return status;
    }

    @Nullable
    public static DomainExpansionEntity findCompleteDomain(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return null;

        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers(level, owner.blockPosition())) {
            for (DomainExpansionEntity domain : barrier.getClashers()) {
                if (domain.isRemoved() || domain.getOwner() == owner) continue;

                if (domain instanceof IncompleteDomainEntity incomplete && incomplete.isIncomplete()) continue;

                return domain;
            }
        }
        return null;
    }

    private static Room around(LivingEntity owner) {
        BlockPos origin = owner.blockPosition();

        return new Room(origin.offset(-TOKEN_RADIUS, -TOKEN_RADIUS, -TOKEN_RADIUS),
                origin.offset(TOKEN_RADIUS, TOKEN_RADIUS, TOKEN_RADIUS));
    }

    @Override
    public Status isStillUsable(LivingEntity owner) {
        if (!owner.level().isClientSide) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.hasSummonOfClass(IncompleteDomainEntity.class)) return Status.FAILURE;
        }
        return super.isStillUsable(owner);
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        Room room = scan(owner);

        boolean seals = room != null;

        if (room == null) {
            if (findCompleteDomain(owner) == null) {
                if (owner instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable(
                            String.format("chat.%s.incomplete_domain.no_room", JujutsuKaisen.MOD_ID)));
                }
                return;
            }
            room = around(owner);
        }

        DomainExpansion ability = getDomainAbility(owner);

        IncompleteDomainEntity domain = new IncompleteDomainEntity(owner, ability, room, true, seals);
        owner.level().addFreshEntity(domain);

        cap.addSummon(domain);

        chargeForVeil(owner, cap);
    }

    private static void chargeForVeil(LivingEntity owner, ISorcererData cap) {
        if (owner instanceof Player player && player.getAbilities().instabuild) return;

        VeilEntity veil = VeilHandler.getVeilAt(owner.level(), owner.blockPosition());

        if (veil == null) return;

        UUID creator = veil.getOwnerUUID();

        if (creator != null && creator.equals(owner.getUUID())) return;

        cap.useEnergy(FOREIGN_VEIL_COST);

        if (owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        }
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        cap.unsummonByClass(IncompleteDomainEntity.class);

        if (owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        }
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Nullable
    public static DomainExpansion getDomainAbility(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return null;

        CursedTechnique technique = cap.getTechnique();

        if (technique != null && technique.getDomain() instanceof DomainExpansion domain) return domain;

        if (cap.hasTechnique(CursedTechnique.BRAIN_TRANSPLANT) && cap.getLastStolen() != null
                && cap.getLastStolen().getDomain() instanceof DomainExpansion stolen) {
            return stolen;
        }
        return null;
    }

    @Nullable
    private static final Map<UUID, Long> EXPLAINED = new HashMap<>();

    private static final int EXPLAIN_INTERVAL = 3 * 20;

    private void explain(LivingEntity owner, @Nullable String reason) {
        if (reason == null || !(owner instanceof ServerPlayer player)) return;

        long now = owner.level().getGameTime();
        Long last = EXPLAINED.get(owner.getUUID());

        if (last != null && now - last < EXPLAIN_INTERVAL) return;

        EXPLAINED.put(owner.getUUID(), now);

        player.sendSystemMessage(Component.translatable(
                String.format("chat.%s.incomplete_domain.refused", JujutsuKaisen.MOD_ID), reason));
    }

    public static Room scan(LivingEntity owner) {
        return scan(owner, null);
    }

    public static Room scan(LivingEntity owner, @Nullable String[] reason) {
        Level level = owner.level();
        BlockPos origin = owner.blockPosition();

        if (isSolid(level, origin)) return fail(reason, "solid");
        if (!hasFloor(level, origin)) return fail(reason, "floor");
        if (!hasRoof(level, origin)) return fail(reason, "roof");

        int[] reach = new int[1];

        float enclosed = enclosure(level, origin, reach);

        if (enclosed < ENCLOSURE_THRESHOLD) {
            if (reason != null) {
                reason[0] = String.format("open (%d%% of the view is walled in, %d%% needed)",
                        Math.round(enclosed * 100.0F), Math.round(ENCLOSURE_THRESHOLD * 100.0F));
            }
            return null;
        }

        int radius = Math.max(1, Math.min(MAX_RADIUS, reach[0]));

        return new Room(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius));
    }

    @Nullable
    private static Room fail(@Nullable String[] reason, String because) {
        if (reason != null) reason[0] = because;

        return null;
    }


    private static boolean hasFloor(Level level, BlockPos origin) {
        for (int depth = 1; depth <= FLOOR_REACH; depth++) {
            if (isSolid(level, origin.below(depth))) return true;
        }
        return false;
    }

    private static boolean hasRoof(Level level, BlockPos origin) {
        for (int height = 1; height <= MAX_SIZE; height++) {
            if (isSolid(level, origin.above(height))) return true;
        }
        return false;
    }

    private static Map<BlockPos, Integer> flood(Level level, BlockPos origin) {
        Map<BlockPos, Integer> visited = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        visited.put(origin, 0);
        queue.add(origin);

        int limit = MAX_RADIUS * MAX_RADIUS;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);

                if (visited.containsKey(next)) continue;

                int distance = (int) next.distSqr(origin);

                if (distance > limit) continue;
                if (isSolid(level, next)) continue;

                visited.put(next, distance);
                queue.add(next);
            }
        }
        return visited;
    }

    private static final int SIGHTLINES = 512;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private static float enclosure(Level level, BlockPos origin, int[] reach) {
        Vec3 eye = origin.getCenter();

        int blocked = 0;
        int furthest = 1;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int i = 0; i < SIGHTLINES; i++) {
            double y = 1.0D - (2.0D * i + 1.0D) / SIGHTLINES;
            double ring = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double turn = i * GOLDEN_ANGLE;

            double dx = Math.cos(turn) * ring;
            double dz = Math.sin(turn) * ring;

            int hit = look(level, eye, dx, y, dz, cursor);

            if (hit < 0) continue;

            blocked++;
            furthest = Math.max(furthest, hit);
        }

        reach[0] = furthest;

        return (float) blocked / SIGHTLINES;
    }

    private static int look(Level level, Vec3 eye, double dx, double dy, double dz,
                            BlockPos.MutableBlockPos cursor) {
        for (double distance = 0.5D; distance <= MAX_RADIUS; distance += 0.5D) {
            cursor.set(Mth.floor(eye.x + dx * distance),
                    Mth.floor(eye.y + dy * distance),
                    Mth.floor(eye.z + dz * distance));

            if (isSolid(level, cursor)) return (int) Math.ceil(distance);
        }
        return -1;
    }

    private static int radiusFor(int squared) {
        return Math.max(1, (int) Math.ceil(Math.sqrt(squared)));
    }

    public static boolean isSolid(Level level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) return false;

        BlockState state = level.getBlockState(pos);

        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 2.5F;
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

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.CURSED_ENERGY_FLOW.get();
    }

    @Override
    public int getPointsCost() {
        return POINTS_COST;
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(1.0F, 0.0F);
    }

    @Override
    public AbilityDisplayInfo getDisplay(LivingEntity owner) {
        Vec2 coordinates = this.getDisplayCoordinates();
        return new AbilityDisplayInfo("domain_expansion", coordinates.x, coordinates.y);
    }
}

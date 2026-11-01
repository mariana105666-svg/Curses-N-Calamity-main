package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.TunnelStateS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TunnelHandler {
    private static final double REACH = 4.5D;

    private static final double RANGE = 128.0D;

    private static final double SPEED = 1.0D;

    private static final int SWING_INTERVAL = 4;

    private static final float MINE_SPEED = 1.5F;

    private static final ItemStack PICK = Items.STONE_PICKAXE.getDefaultInstance();

    private static final Map<UUID, TunnelJob> JOBS = new HashMap<>();

    private TunnelHandler() {
    }

    public static void start(ServerPlayer player, BlockPos origin, Direction facing, TunnelPattern pattern) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.hasPuppetRegister()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        List<UUID> puppets = new ArrayList<>();

        for (PuppetRecord record : cap.getPuppets()) {
            UUID id = record.getEntity();

            if (id == null) continue;
            if (!(level.getEntity(id) instanceof PuppetEntity puppet)) continue;
            if (puppet.distanceToSqr(player) > RANGE * RANGE) continue;

            puppets.add(id);
        }

        if (puppets.isEmpty()) return;

        cancel(player);

        if (JJKAbilities.hasToggled(player, JJKAbilities.FOLLOW.get())) {
            AbilityHandler.untrigger(player, JJKAbilities.FOLLOW.get());
        }

        for (UUID id : puppets) {
            if (level.getEntity(id) instanceof PuppetEntity puppet) {
                puppet.setWorking(true);
                puppet.setTarget(null);
            }
        }

        JOBS.put(player.getUUID(), new TunnelJob(origin, facing, pattern, puppets));

        PacketHandler.sendToClient(new TunnelStateS2CPacket(true), player);
    }

    public static void cancel(ServerPlayer player) {
        TunnelJob job = JOBS.remove(player.getUUID());

        if (job != null) PacketHandler.sendToClient(new TunnelStateS2CPacket(false), player);

        if (job == null || !(player.level() instanceof ServerLevel level)) return;

        for (UUID id : job.getPuppets()) {
            if (!(level.getEntity(id) instanceof PuppetEntity puppet)) continue;

            puppet.setWorking(false);
            puppet.getNavigation().stop();

            for (BlockPos pos : job.current()) level.destroyBlockProgress(puppet.getId(), pos, -1);
        }
    }

    public static boolean isWorking(ServerPlayer player) {
        return JOBS.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        TunnelJob job = JOBS.get(player.getUUID());

        if (job == null) return;

        if (job.isDone()) {
            cancel(player);
            return;
        }

        boolean working = false;

        for (UUID id : job.getPuppets()) {
            if (!(level.getEntity(id) instanceof PuppetEntity puppet)) continue;
            if (puppet.isRemoved() || !puppet.isAlive()) continue;

            working = true;

            if (puppet.getTarget() != null && puppet.getTarget().isAlive()) continue;

            advance(level, player, puppet, job);
        }

        if (!working) cancel(player);
    }

    private static void advance(ServerLevel level, ServerPlayer player, PuppetEntity puppet, TunnelJob job) {
        UUID id = puppet.getUUID();

        BlockPos target = job.getClaim(id);

        if (target == null || !canMine(level, player, target)) {
            target = claim(level, player, job, id);

            if (target == null) {
                job.step();
                return;
            }

            job.claim(id, target);

            puppet.getNavigation().stop();
        }

        Vec3 center = Vec3.atCenterOf(target);

        if (puppet.distanceToSqr(center) > REACH * REACH) {
            if (puppet.getNavigation().isDone()) {
                BlockPos spot = job.approach(target);

                if (!puppet.getNavigation().moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, SPEED)) {
                    puppet.getMoveControl().setWantedPosition(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, SPEED);
                }
            }
            return;
        }

        puppet.getNavigation().stop();
        puppet.getLookControl().setLookAt(center.x, center.y, center.z);

        if (puppet.tickCount % SWING_INTERVAL == 0) puppet.swing(InteractionHand.MAIN_HAND);

        BlockState state = level.getBlockState(target);

        float delta = speed(state, level, target);

        if (delta <= 0.0F) return;

        float total = job.addProgress(target, delta * MINE_SPEED);

        level.destroyBlockProgress(puppet.getId(), target, (int) (total * 10.0F));

        if (total < 1.0F) return;

        level.destroyBlockProgress(puppet.getId(), target, -1);

        level.destroyBlock(target, true, puppet);
    }

    @Nullable
    private static BlockPos claim(ServerLevel level, ServerPlayer player, TunnelJob job, UUID id) {
        BlockPos fallback = null;

        for (BlockPos pos : job.current()) {
            if (!canMine(level, player, pos)) continue;

            if (fallback == null) fallback = pos;

            if (!job.isClaimed(pos, id)) return pos;
        }
        return fallback;
    }

    private static float speed(BlockState state, ServerLevel level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);

        if (hardness < 0.0F) return 0.0F;
        if (hardness == 0.0F) return 1.0F;

        boolean correct = !state.requiresCorrectToolForDrops() || PICK.isCorrectToolForDrops(state);

        return PICK.getDestroySpeed(state) / hardness / (correct ? 30.0F : 100.0F);
    }

    private static boolean canMine(ServerLevel level, ServerPlayer player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        if (!HelperMethods.isDestroyable(level, player, pos)) return false;

        return !state.requiresCorrectToolForDrops() || PICK.isCorrectToolForDrops(state);
    }
}

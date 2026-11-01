package radon.jujutsu_kaisen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.IncompleteDomainEntity;
import radon.jujutsu_kaisen.entity.OpenDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.DomainClashS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DomainClashHandler {
    private static final int CLASH_INTERVAL = 10;

    private static final float REGEN = 0.5F;

    private static final float TAKEOVER_SHARE = 0.5F;

    private static final int SYNC_INTERVAL = 2;

    private static final float MIN_STRENGTH = 0.01F;

    private static final float SHELL_ADVANTAGE = 1.5F;

    private static final float SHELL_PENALTY = 1.0F / SHELL_ADVANTAGE;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START || event.side == LogicalSide.CLIENT) return;

        if (!(event.level instanceof ServerLevel level)) return;

        List<DomainExpansionEntity> domains = collect(level);

        if (domains.isEmpty()) return;

        Set<UUID> live = new HashSet<>();

        for (DomainExpansionEntity domain : domains) {
            live.add(domain.getUUID());
        }
        reported.retainAll(live);

        Set<DomainExpansionEntity> drained = new HashSet<>();
        Set<DomainExpansionEntity> clashing = new HashSet<>();

        for (List<DomainExpansionEntity> group : group(domains)) {
            if (group.size() < 2) continue;

            markClashing(group, clashing);

            tickClash(level, group, drained);
        }

        for (DomainExpansionEntity domain : domains) {
            domain.setClashing(clashing.contains(domain));

            if (drained.contains(domain)) continue;

            domain.setClashHealth(domain.getClashHealth() + REGEN);
        }
    }

    private static List<DomainExpansionEntity> collect(ServerLevel level) {
        Set<DomainExpansionEntity> result = new LinkedHashSet<>();

        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers(level)) {
            for (DomainExpansionEntity domain : barrier.getClashers()) {
                if (domain == null || domain.isRemoved() || !domain.isAlive()) continue;

                if (domain.isInstant()) continue;

                if (domain.getBounds() == null) continue;

                result.add(domain);
            }
        }
        return new ArrayList<>(result);
    }

    private static List<List<DomainExpansionEntity>> group(List<DomainExpansionEntity> domains) {
        List<List<DomainExpansionEntity>> groups = new ArrayList<>();
        Set<DomainExpansionEntity> assigned = new HashSet<>();

        for (DomainExpansionEntity domain : domains) {
            if (!assigned.add(domain)) continue;

            List<DomainExpansionEntity> group = new ArrayList<>();
            group.add(domain);

            for (int i = 0; i < group.size(); i++) {
                AABB bounds = group.get(i).getBounds();

                for (DomainExpansionEntity other : domains) {
                    if (assigned.contains(other)) continue;
                    if (!bounds.intersects(other.getBounds())) continue;

                    assigned.add(other);
                    group.add(other);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    private static final Set<UUID> reported = new HashSet<>();

    private static void report(ServerLevel level, List<DomainExpansionEntity> group) {
        List<String> out = new ArrayList<>();

        out.add("[JJK-CLASH] tick " + level.getGameTime() + ", " + group.size() + " domains in contact");

        for (DomainExpansionEntity domain : group) {
            LivingEntity owner = domain.getOwner();

            String who = owner == null ? "<no owner>" : owner.getName().getString();
            String what = domain.getAbility() == null
                    ? "<no technique>"
                    : JJKAbilities.getKey(domain.getAbility()).getPath();

            String kind = domain instanceof OpenDomainExpansionEntity ? "open"
                    : isIncomplete(domain) ? "incomplete" : "closed";

            out.add("  " + who + "  -  " + what + "  (" + kind + ")");
            out.add(String.format("      health       %.1f / %.1f",
                    domain.getClashHealth(), domain.getMaxClashHealth()));
            out.add(String.format("      raw power    %.3f   <- the caster's cursed energy experience",
                    domain.getClashStrength()));
            out.add(String.format("      shell        %s, factor %.2f",
                    domain.getShellBalance() ? "outer" : "inner", shellFactor(domain)));
            out.add(String.format("      contest str  %.3f   <- raw power x shell factor",
                    clashStrength(domain)));

            float clashing = 0.0F;
            float siege = 0.0F;

            for (DomainExpansionEntity other : group) {
                if (other == domain || !drains(other, domain)) continue;

                LivingEntity attacker = other.getOwner();
                String name = attacker == null ? "<no owner>" : attacker.getName().getString();

                if (isSiege(other, domain)) {
                    float amount = other.getClashDamage() * shellFactor(domain);
                    siege += amount;

                    out.add(String.format("      besieged by %s for %.2f", name, amount));
                } else {
                    clashing += other.getClashDamage();

                    out.add(String.format("      contested by %s for %.2f", name, other.getClashDamage()));
                }
            }

            float fromClash = clashing;
            float total = siege + fromClash;

            if (total > 0.0F) {
                out.add(String.format("      losing       %.2f per %d ticks  (siege %.2f + contest %.2f), empty in %ds",
                        total, CLASH_INTERVAL, siege, fromClash,
                        Math.round(domain.getClashHealth() / total * CLASH_INTERVAL / 20.0F)));
            } else {
                out.add("      losing       nothing, regenerating");
            }
        }
        System.out.println(String.join(System.lineSeparator(), out));
    }
    private static void tickClash(ServerLevel level, List<DomainExpansionEntity> group, Set<DomainExpansionEntity> drained) {
        boolean fresh = false;

        for (DomainExpansionEntity domain : group) {
            if (reported.add(domain.getUUID())) fresh = true;
        }

        if (fresh) report(level, group);

        List<DomainExpansionEntity> losers = new ArrayList<>();

        boolean fighting = false;

        for (DomainExpansionEntity domain : group) {
            float clashing = 0.0F;
            float siege = 0.0F;

            for (DomainExpansionEntity other : group) {
                if (other == domain) continue;
                if (!drains(other, domain)) continue;

                if (isSiege(other, domain)) {
                    siege += other.getClashDamage() * shellFactor(domain);
                } else {
                    clashing += other.getClashDamage();
                }
            }

            float drain = siege + clashing;

            if (drain <= 0.0F) continue;

            if (level.getGameTime() % CLASH_INTERVAL != 0) {
                fighting = true;
                drained.add(domain);
                continue;
            }

            fighting = true;
            drained.add(domain);

            domain.setClashHealth(domain.getClashHealth() - drain);

            if (domain.getClashHealth() <= 0.0F) {
                losers.add(domain);
            }
        }

        if (!fighting) return;

        if (!losers.isEmpty()) {
            resolve(group, losers);
            return;
        }

        if (level.getGameTime() % SYNC_INTERVAL == 0) {
            broadcast(level, group);
        }
    }

    private static boolean drains(DomainExpansionEntity attacker, DomainExpansionEntity target) {
        boolean attackerOpen = attacker instanceof OpenDomainExpansionEntity;
        boolean targetOpen = target instanceof OpenDomainExpansionEntity;

        if (attackerOpen == targetOpen) return true;

        DomainExpansionEntity open = attackerOpen ? attacker : target;
        DomainExpansionEntity closed = attackerOpen ? target : attacker;

        if (isCaught(open, closed)) return true;

        return attackerOpen;
    }

    private static boolean isSiege(DomainExpansionEntity attacker, DomainExpansionEntity target) {
        if (!(attacker instanceof OpenDomainExpansionEntity)) return false;
        if (target instanceof OpenDomainExpansionEntity) return false;

        return !isCaught(attacker, target);
    }

    private static boolean isCaught(DomainExpansionEntity open, DomainExpansionEntity closed) {
        LivingEntity owner = open.getOwner();

        return owner != null && closed.isInsideBarrier(owner.blockPosition());
    }

    private static void markClashing(List<DomainExpansionEntity> group, Set<DomainExpansionEntity> clashing) {
        for (DomainExpansionEntity domain : group) {
            for (DomainExpansionEntity other : group) {
                if (other == domain) continue;
                if (!drains(other, domain) || !drains(domain, other)) continue;

                if (isIncomplete(other) && !isIncomplete(domain)) continue;

                clashing.add(domain);
                break;
            }
        }
    }

    private static boolean isIncomplete(DomainExpansionEntity domain) {
        return domain instanceof IncompleteDomainEntity incomplete && incomplete.isIncomplete();
    }

    private static float strength(DomainExpansionEntity domain) {
        return Math.max(MIN_STRENGTH, domain.getClashStrength());
    }

    private static float clashStrength(DomainExpansionEntity domain) {
        return Math.max(MIN_STRENGTH, strength(domain) * shellFactor(domain));
    }

    private static float shellFactor(DomainExpansionEntity domain) {
        if (domain instanceof OpenDomainExpansionEntity) return 1.0F;

        return domain.getShellBalance() ? SHELL_PENALTY : SHELL_ADVANTAGE;
    }

    private static void resolve(List<DomainExpansionEntity> group, List<DomainExpansionEntity> losers) {
        List<DomainExpansionEntity> survivors = new ArrayList<>(group);
        survivors.removeAll(losers);

        for (DomainExpansionEntity loser : losers) {
            loser.discard();
        }

        for (DomainExpansionEntity survivor : survivors) {
            if (!payTakeover(survivor)) {
                survivor.discard();
                continue;
            }

            if (survivor instanceof OpenDomainExpansionEntity open) {
                open.reexpand();
            }
        }
    }

    private static boolean payTakeover(DomainExpansionEntity domain) {
        LivingEntity owner = domain.getOwner();

        if (owner == null) return false;

        if (owner instanceof Player player && player.getAbilities().instabuild) return true;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;

        float cost = (domain.getAbility() == null
                ? DomainExpansionEntity.INITIAL_COST
                : domain.getAbility().getRealCost(owner, DomainExpansionEntity.INITIAL_COST)) * TAKEOVER_SHARE;

        if (cap.getEnergy() < cost) return false;

        cap.useEnergy(cost);

        if (owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        }
        return true;
    }

    private static void broadcast(ServerLevel level, List<DomainExpansionEntity> group) {
        List<DomainExpansionEntity> ordered = new ArrayList<>(group);
        ordered.sort(Comparator.comparing(Entity::getUUID));

        Set<ServerPlayer> viewers = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            for (DomainExpansionEntity domain : group) {
                if (!domain.isInsideBarrier(player.blockPosition())) continue;

                viewers.add(player);
                break;
            }
        }

        if (viewers.isEmpty()) return;

        for (ServerPlayer viewer : viewers) {
            List<DomainClashS2CPacket.Entry> entries = new ArrayList<>();

            for (DomainExpansionEntity domain : ordered) {
                LivingEntity owner = domain.getOwner();

                entries.add(new DomainClashS2CPacket.Entry(
                        owner == null ? "?" : owner.getName().getString(),
                        domain.getClashFraction(),
                        owner == viewer));
            }

            entries.sort((a, b) -> Boolean.compare(b.self(), a.self()));

            PacketHandler.sendToClient(new DomainClashS2CPacket(entries), viewer);
        }
    }
}

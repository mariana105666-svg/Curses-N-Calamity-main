package radon.jujutsu_kaisen;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.entity.IBarrier;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.block.entity.IOpenDomainBarrier;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.block.entity.VeilRodBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.DomainBarrierEntity;
import radon.jujutsu_kaisen.block.entity.IVeil;
import radon.jujutsu_kaisen.entity.OpenDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.VeilEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.PlayerModifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VeilHandler {

    // private static final Map<ResourceKey<Level>, Set<VeilRodBlockEntity>> veilsByDimension = new HashMap<>();
    // private static final Map<ResourceKey<Level>, Set<UUID>> domains = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<UUID>> barriers = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<UUID, IBarrier>> cache = new ConcurrentHashMap<>();
    //private final Map<BlockPos, CompoundTag> placedBlocks = new HashMap<>();

    public static void barrier(ResourceKey<Level> dimension, UUID identifier) {
        barriers.computeIfAbsent(dimension, ignored -> new HashSet<>()).add(identifier);
    }

    @Nullable
    private static IBarrier get(ServerLevel level, UUID identifier) {
        ResourceKey<Level> dimension = level.dimension();

        Map<UUID, IBarrier> slot = cache.computeIfAbsent(dimension, k -> new HashMap<>());

        IBarrier cached = slot.get(identifier);

        if (cached instanceof Entity entity && entity.isAlive()) {
            return cached;
        }

        Entity entity = level.getEntity(identifier);

        if (entity instanceof IBarrier barrier) {
            slot.put(identifier, barrier);
            return barrier;
        }

        slot.remove(identifier);
        return null;
    }

     public static Set<IBarrier> getBarriers(ServerLevel level, BlockPos target) {
        Set<IBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;
                  //System.out.println("domain looped -2");
        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            //System.out.println(barrier == null);
           // System.out.println(!barrier.isInsideBarrier(target));
            //System.out.println(target);
            //  System.out.println("domain looped -1");
            if (barrier == null || !barrier.isInsideBarrier(target)) continue;
             // System.out.println("domain looped -0.5");
            result.add(barrier);
        }
        return result;
    }

    public static Set<IDomainBarrier> getDomainBarriers(ServerLevel level, AABB bounds) {
        Set<IDomainBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;

        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            if (barrier == null || !(barrier instanceof IDomainBarrier barrierEntity) || !bounds.intersects(barrierEntity.getBounds())) continue;
            result.add(barrierEntity);
        }
        return result;
    }


    public static Set<IDomainBarrier> getDomainBarriers(ServerLevel level) {
        Set<IDomainBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;

        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);

            if (!(barrier instanceof IDomainBarrier barrierEntity)) continue;

            result.add(barrierEntity);
        }
        return result;
    }

     public static Set<IDomainBarrier> getDomainBarriers(ServerLevel level, BlockPos target) {
        Set<IDomainBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;
                  //System.out.println("domain looped -2a");
        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            //System.out.println(barrier == null);
              //System.out.println("domain looped -1");
            if (barrier == null ||  !(barrier instanceof IDomainBarrier barrierEntity) || !barrierEntity.isInsideBarrier(target)) continue;
            //  System.out.println("domain looped -0.5");
            result.add(barrierEntity);
        }
        return result;
    }
    

    public static Set<IDomainBarrier> getDomainShellBarriers(ServerLevel level, AABB bounds) {
        Set<IDomainBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;

        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            if (barrier == null || barrier instanceof IOpenDomainBarrier || !(barrier instanceof IDomainBarrier barrierEntity) || !bounds.intersects(barrierEntity.getBounds())) continue;
            result.add(barrierEntity);
        }
        return result;
    }


     public static Set<IDomainBarrier> getDomainShellBarriers(ServerLevel level, BlockPos target) {
        Set<IDomainBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;
        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            if (barrier == null || barrier instanceof IOpenDomainBarrier || !(barrier instanceof IDomainBarrier barrierEntity) || !barrierEntity.isInsideBarrier(target)) continue;
            result.add(barrierEntity);
        }
        return result;
    }
    

    public static Set<IBarrier> getBarriers(ServerLevel level, AABB bounds) {
        Set<IBarrier> result = new HashSet<>();

        if (!barriers.containsKey(level.dimension())) return result;

        for (UUID identifier : barriers.get(level.dimension())) {
            IBarrier barrier = get(level, identifier);
            if (barrier == null || !bounds.intersects(barrier.getBounds())) continue;
            result.add(barrier);
        }
        return result;
    }

      public static Set<IDomain> getDomains(ServerLevel level, BlockPos target) {
        Set<IDomain> result = new HashSet<>();
        for (IBarrier barrier : getBarriers(level, target)) {
            if (!(barrier instanceof IDomain domain)) continue;
            result.add(domain);
        }
        return result;
    }

    

    public static Set<IDomain> getDomains(ServerLevel level, AABB bounds) {
        Set<IDomain> result = new HashSet<>();

        for (IBarrier barrier : getBarriers(level, bounds)) {
            if (!(barrier instanceof IDomain domain)) continue;

            result.add(domain);
        }
        return result;
    }


      public static Set<IVeil> getVeils(ServerLevel level, BlockPos target) {
        Set<IVeil> result = new HashSet<>();

        for (IBarrier barrier : getBarriers(level, target)) {
            if (!(barrier instanceof IVeil veil)) continue;

            result.add(veil);
        }
        return result;
    }

    public static Set<IVeil> getVeils(ServerLevel level, AABB bounds) {
        Set<IVeil> result = new HashSet<>();

        for (IBarrier barrier : getBarriers(level, bounds)) {
            if (!(barrier instanceof IVeil veil)) continue;

            result.add(veil);
        }
        return result;
    }

    
    @Nullable
    public static IBarrier getSurroundingBarrier(ServerLevel level, BlockPos target) {
        Set<IBarrier> all = getBarriers(level, target);

        if (all.isEmpty()) return null;

        IBarrier innermost = null;
        double bestDepth = -Double.MAX_VALUE;

        for (IBarrier barrier : all) {
            AABB bounds = barrier.getBounds();
            double dx = Math.min(target.getX() - bounds.minX, bounds.maxX - target.getX());
            double dy = Math.min(target.getY() - bounds.minY, bounds.maxY - target.getY());
            double dz = Math.min(target.getZ() - bounds.minZ, bounds.maxZ - target.getZ());

            double depth = Math.min(dx, Math.min(dy, dz));

            if (depth > bestDepth) {
                bestDepth = depth;
                innermost = barrier;
            }
        }

        return innermost;
    }




    public static boolean wouldOverlapVeil(ServerLevel level, Vec3 center, int radius) {
        AABB bounds = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);

        for (IVeil veil : getVeils(level, bounds)) {
            VeilEntity existing = veil.getVeil();

            if (existing == null || existing.isRemoved()) continue;

            double reach = radius + existing.getRadius();

            if (existing.getBarrierCenter().getCenter().distanceToSqr(center) < reach * reach) return true;
        }
        return false;
    }

    public static boolean checkIntersect(Level level, BlockPos center, int radius) {
        if ( !(level instanceof ServerLevel servLevel)) return false;
    Set<IVeil> veils = getVeils(servLevel, center);
    if (veils == null) return false;

    for (IVeil veil : veils) {
        BlockPos otherPos = veil.getVeil().getCenter();

        if (otherPos.equals(center)) continue;

            int otherRadius = veil.getRadius();
            double dist = Math.sqrt(center.distSqr(otherPos));
            if (dist <= radius + otherRadius && dist >= Math.abs(radius - otherRadius)) {
                return true;
            }

    }

    return false;
}

  public static boolean isWhitelisted(@Nullable BlockPos parent, Entity entity) {
        if (entity == null || parent == null || !(entity.level().getBlockEntity(parent) instanceof VeilBlockEntity be)) return false;

        UUID owner = be.getOwnerUUID();

        if (owner != null && owner.equals(entity.getUUID())) return true;

        List<Modifier> modifiers = be.getModifiers();

        if (modifiers == null) return false;
        if (entity instanceof Player player) {
            for (Modifier modifier : modifiers) {
                if (modifier.getAction() == Modifier.Action.ALLOW &&
                    modifier.getType() == Modifier.Type.OWNER_BYPASS && (entity.getUUID().equals(be.getOwnerUUID())) ) {
                        return true;
                }
                if (modifier.getAction() != Modifier.Action.ALLOW && modifier.getType() != Modifier.Type.PLAYER)
                    continue;
                if (modifier.getType() == Modifier.Type.PLAYER && (player.getUUID().equals(((PlayerModifier) modifier).getUUID()) || ((PlayerModifier) modifier).getName().equals(player.getDisplayName().getString()))) {
                    return modifier.getAction() == Modifier.Action.ALLOW;
                }
            }
        }

        if (!entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;

        ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        Modifier.Type wanted = cap.getType() == JujutsuType.CURSE
                ? Modifier.Type.CURSE
                : Modifier.Type.SORCERER;

        for (Modifier modifier : modifiers) {
            if (modifier.getType() == wanted && modifier.getAction() == Modifier.Action.ALLOW) return true;
        }
        return false;
    }

@Nullable
    public static VeilEntity getVeil(Level pLevel, VeilBlockEntity be) {
        if (((((ServerLevel) pLevel).getEntity(be.getParentUUID()) instanceof VeilEntity veil) && !veil.isRemoved() && veil.isAlive())) {
            return veil;
        }
        return null;
    }
    public static boolean canDestroy(LivingEntity entity, Level level, double x, double y, double z) {
        return canDestroy(entity, level, x, y, z, false);
    }
    @Nullable
    public static VeilEntity getVeilAt(Level level, BlockPos target) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        if (!barriers.containsKey(serverLevel.dimension())) return null;

        for (UUID identifier : barriers.get(serverLevel.dimension())) {
            IBarrier barrier = get(serverLevel, identifier);

            if (!(barrier instanceof IVeil veil)) continue;
            if (!barrier.isBarrierOrInside(target)) continue;

            return veil.getVeil();
        }
        return null;
    }

    @Nullable
    public static VeilEntity getAdminVeil(Level level, BlockPos target) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        if (!barriers.containsKey(serverLevel.dimension())) return null;

        for (UUID identifier : barriers.get(serverLevel.dimension())) {
            IBarrier barrier = get(serverLevel, identifier);

            if (!(barrier instanceof IVeil veil)) continue;
            if (!barrier.isBarrierOrInside(target)) continue;
            if (!isAdminVeil(veil)) continue;

            return veil.getVeil();
        }
        return null;
    }

    private static boolean isAdminVeil(IVeil veil) {
        List<Modifier> modifiers = veil.getModifiers();

        if (modifiers == null) return false;

        for (Modifier modifier : modifiers) {
            if (modifier.getType() == Modifier.Type.ADMIN) return true;
        }
        return false;
    }

    public static boolean isAdminBlocked(Level level, BlockPos target, @Nullable Entity actor) {
        VeilEntity veil = getAdminVeil(level, target);

        if (veil == null) return false;

        if (actor == null) return true;

        UUID owner = veil.getOwnerUUID();

        return owner == null || !owner.equals(actor.getUUID());
    }

    public static boolean isAbilityBlocked(LivingEntity owner, Ability ability) {
        VeilEntity veil = getAdminVeil(owner.level(), owner.blockPosition());

        if (veil == null) return false;

        UUID creator = veil.getOwnerUUID();

        if (creator != null && creator.equals(owner.getUUID())) return false;

        List<Modifier> modifiers = veil.getModifiers();

        if (modifiers == null) return false;

        for (Modifier modifier : modifiers) {
            if (modifier.getAction() != Modifier.Action.DENY) continue;

            if (modifier.getType() == Modifier.Type.ABILITY) return true;

            if (modifier.getType() == Modifier.Type.DOMAIN
                    && (ability.isDomain() || ability == JJKAbilities.ZERO_POINT_TWO_SECOND_DOMAIN_EXPANSION.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDestroy(LivingEntity entity, Level level, double x, double y, double z, boolean manualBreak) {
        if ( !(level instanceof ServerLevel servLevel)) return false;
        BlockPos target = BlockPos.containing(x, y, z);

        if (isAdminBlocked(level, target, entity)) return false;
        Set<IVeil> veils = VeilHandler.getVeils(servLevel, target);
        if (veils == null) return true;
    

        for (IVeil veil : veils) {
            if (manualBreak && entity.level().getBlockState(target).is(JJKBlocks.VEIL_ROD.get())) return true;
            int radius = veil.getRadius();
            if (target.distSqr(veil.getVeil().getCenter() ) >= radius * radius) continue;
            boolean ownerFlag = false;
            boolean destroyFlag = true;
            for (Modifier modifier : veil.getModifiers()) {
                if (modifier.getAction() == Modifier.Action.ALLOW &&
                    modifier.getType() == Modifier.Type.OWNER_BYPASS) {
                    ownerFlag = true;
                }else if (modifier.getAction() == Modifier.Action.DENY &&
                    modifier.getType() == Modifier.Type.GRIEFING) {
                    destroyFlag = false;
                }
            }
            VeilEntity veilObj = veil.getVeil();
         if (ownerFlag == true && entity != null && veilObj.getUUID() != null && veilObj.getUUID().equals(entity.getUUID()) ) continue;
                return destroyFlag;
        }
        return true;
    }

    public static boolean canSpawn(ServerLevel level, Mob mob, double x, double y, double z) {
        BlockPos target = BlockPos.containing(x, y, z);

        if (getAdminVeil(level, target) != null) return false;
        IBarrier owner = getSurroundingBarrier(level, target);

        if (owner instanceof IVeil veil)  {
            boolean curseSpawnFlag = false;
            boolean sorcererSpawnFlag = false;
            for (Modifier modifier : veil.getModifiers()) {
            if (modifier.getAction() == Modifier.Action.ALLOW &&
                modifier.getType() == Modifier.Type.CURSE_SPAWN) {
                curseSpawnFlag = true;
            } else if (modifier.getAction() == Modifier.Action.ALLOW &&
                modifier.getType() == Modifier.Type.SORCERER_SPAWN) {
                sorcererSpawnFlag = true;
            }
            }
            if ((sorcererSpawnFlag && mob instanceof SorcererEntity) || (curseSpawnFlag && mob instanceof CursedSpirit) )  {
            }
            else {
                return false;
            }
            
        }
        else if (owner instanceof IDomainBarrier) {
            return false;
        }

        
        return true;
    }

  
 
     @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();

        if (barriers.containsKey(level.dimension())) {
            Set<UUID> identifiers = barriers.get(level.dimension());

            if (!identifiers.remove(entity.getUUID())) return;

            if (identifiers.isEmpty()) {
                barriers.remove(level.dimension());
                cache.remove(level.dimension());
            } else {
                Map<UUID, IBarrier> levelCache = cache.get(level.dimension());
                if (levelCache != null) {
                    levelCache.remove(entity.getUUID());
                    if (levelCache.isEmpty()) cache.remove(level.dimension());
                }
            }
        }
    }

    


    public static boolean isProtected(Level level, BlockPos target) {
        if ( !(level instanceof ServerLevel servLevel)) return false;
        Set<IVeil> veils = getVeils(servLevel, target);
        if (veils.isEmpty()) return false;

        for (IVeil veil : veils) {
            //if (!rod.isValid()) continue;
            int radius = veil.getRadius();
            if (target.distSqr(veil.getVeil().getCenter() ) < radius * radius) return true;
        }
        return false;
    }

    public static boolean isTeleportValid(Level level, BlockPos target) {
        
        if ( !(level instanceof ServerLevel servLevel)) return true;
        Set<IVeil> veils = getVeils(servLevel, target);
        if (veils.isEmpty()) return true;

        for (IVeil veil : veils) {
            boolean ownerFlag = false;
             boolean teleportFlag = false;
             for (Modifier modifier : veil.getModifiers()) {
                if (modifier.getAction() == Modifier.Action.ALLOW &&
                    modifier.getType() == Modifier.Type.OWNER_BYPASS) {
                    ownerFlag = true;
                }else if (modifier.getAction() == Modifier.Action.DENY &&
                    modifier.getType() == Modifier.Type.TELEPORT) {
                    teleportFlag = true;
                }
            }
            int radius = veil.getRadius();
            if (( teleportFlag && target.distSqr(veil.getVeil().getCenter()) <= (radius+2) * (radius+2) && !ownerFlag ) ) return false;
        }
        return true;
    }
}

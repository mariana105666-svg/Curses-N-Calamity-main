package radon.jujutsu_kaisen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.entity.VesselEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncEnergyS2CPacket;
import radon.jujutsu_kaisen.puppet.PuppetRecord;
import radon.jujutsu_kaisen.util.EntityUtil;

import java.util.UUID;

public class PossessionHandler {
    private static final UUID POSSESSION_HEALTH = UUID.fromString("6f1d5a24-9e4b-4c07-8f3a-2b6c1e7d0a95");
    private static final UUID POSSESSION_ATTACK = UUID.fromString("9a4e7c81-3f26-4b05-8d7e-1c0b9f2a6e34");

    private static final double REACH = 480.0D;

    private static final float DRAIN_NEAR = 0.2F;
    private static final float DRAIN_FAR = 10.0F;

    private static final float INSIDE_DRAIN = 1.15F;

    private static final int SURVEY_INTERVAL = 20;

    private PossessionHandler() {
    }

    public static boolean possess(ServerPlayer player, PuppetRecord record) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || cap.isPossessing()) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        if (!record.getDimension().isEmpty()
                && !record.getDimension().equals(level.dimension().location().toString())) {
            player.sendSystemMessage(Component.translatable(
                    String.format("chat.%s.puppet_elsewhere", JujutsuKaisen.MOD_ID), record.getName()));
            return false;
        }

        Vec3 destination = record.getPos();

        level.getChunkAt(BlockPos.containing(destination));

        UUID id = record.getEntity();
        float yRot = player.getYRot();

        if (id != null && level.getEntity(id) instanceof PuppetEntity puppet) {
            record.setHealth(puppet.getHealth());
            record.setMaxHealth(puppet.getMaxHealth());
            record.setPos(puppet.position());

            destination = puppet.position();
            yRot = puppet.getYRot();

            puppet.discard();
        }

        record.setEntity(null);

        cap.setBodyHealth(player.getHealth());
        cap.setBodyMaxHealth(player.getMaxHealth());
        cap.setBodyPos(player.position());

        if (!cap.isInside()) {
            VesselEntity vessel = new VesselEntity(level, player);

            level.addFreshEntity(vessel);

            cap.setVessel(vessel.getUUID());
        }

        cap.setPossessed(record.getName());

        if (!cap.isInside()) stash(player, cap);

        wear(player, record);

        player.teleportTo(destination.x, destination.y, destination.z);
        player.setYRot(yRot);
        player.setXRot(0.0F);

        return true;
    }

    private static void wear(ServerPlayer player, PuppetRecord record) {
        EntityUtil.applyModifier(player, Attributes.MAX_HEALTH, POSSESSION_HEALTH,
                "Puppet health", record.getMaxHealth() - player.getMaxHealth(),
                AttributeModifier.Operation.ADDITION);

        EntityUtil.applyModifier(player, Attributes.ATTACK_DAMAGE, POSSESSION_ATTACK,
                "Puppet strength", PuppetEntity.ATTACK - player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE),
                AttributeModifier.Operation.ADDITION);

        player.setHealth(Mth.clamp(record.getHealth(), 1.0F, player.getMaxHealth()));
    }

    private static void stash(ServerPlayer player, ISorcererData cap) {
        net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();

        player.getInventory().save(items);

        cap.setStoredItems(items);

        player.getInventory().clearContent();
        player.inventoryMenu.broadcastFullState();
    }

    private static void restore(ServerPlayer player, ISorcererData cap) {
        net.minecraft.nbt.ListTag items = cap.getStoredItems();

        if (items.isEmpty()) return;

        player.getInventory().load(items);

        cap.setStoredItems(new net.minecraft.nbt.ListTag());

        player.inventoryMenu.broadcastFullState();
    }

    private static void shed(ServerPlayer player, ISorcererData cap, @Nullable VesselEntity vessel) {
        float returning = vessel == null ? cap.getBodyHealth() : vessel.getHealth();

        EntityUtil.removeModifier(player, Attributes.MAX_HEALTH, POSSESSION_HEALTH);
        EntityUtil.removeModifier(player, Attributes.ATTACK_DAMAGE, POSSESSION_ATTACK);

        player.setHealth(Mth.clamp(returning, 1.0F, player.getMaxHealth()));
    }

    public static void release(ServerPlayer player, boolean destroyed) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.isPossessing()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        boolean inside = cap.isInside();

        String name = cap.getPossessed();
        PuppetRecord record = name == null ? null : cap.getPuppetByName(name);

        if (record != null) {
            if (destroyed) {
                cap.removePuppet(record);
            } else {
                record.setHealth(player.getHealth());
                record.setPos(player.position());

                PuppetEntity puppet = build(level, player, record);

                record.setEntity(puppet == null ? null : puppet.getUUID());
            }
        }

        if (!inside && cap.getBodyPos() != null) {
            level.getChunkAt(BlockPos.containing(cap.getBodyPos()));
        }

        VesselEntity vessel = vessel(level, cap);

        shed(player, cap, vessel);

        restore(player, cap);

        if (inside) {
            if (vessel != null) vessel.discard();
        } else if (vessel != null) {
            player.teleportTo(vessel.getX(), vessel.getY(), vessel.getZ());
            player.setYRot(vessel.getYRot());
            player.setXRot(vessel.getXRot());

            vessel.discard();
        } else if (cap.getBodyPos() != null) {
            Vec3 pos = cap.getBodyPos();

            player.teleportTo(pos.x, pos.y, pos.z);
        }

        cap.setVessel(null);
        cap.setPossessed(null);
        cap.setInside(false);
    }

    @Nullable
    public static PuppetEntity build(ServerLevel level, ServerPlayer owner, PuppetRecord record) {
        PuppetEntity puppet = JJKEntities.PUPPET.get().create(level);

        if (puppet == null) return null;

        Vec3 pos = record.getPos();

        float yaw = owner.getYRot();

        puppet.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);

        puppet.setYBodyRot(yaw);
        puppet.setYHeadRot(yaw);

        AttributeInstance max = puppet.getAttribute(Attributes.MAX_HEALTH);

        if (max != null) max.setBaseValue(record.getMaxHealth());

        puppet.setHealth(Mth.clamp(record.getHealth(), 1.0F, puppet.getMaxHealth()));
        puppet.link(owner, record.getName());

        record.setDimension(level.dimension().location().toString());

        level.addFreshEntity(puppet);

        return puppet;
    }

    public static boolean tick(ServerPlayer player, ISorcererData cap) {
        if (!(player.level() instanceof ServerLevel level)) return true;

        return drain(player, cap, level);
    }

    private static boolean drain(ServerPlayer player, ISorcererData cap, ServerLevel level) {
        float cost;

        if (cap.isInside()) {
            cost = ConfigHolder.SERVER.cursedEnergyRegenerationAmount.get().floatValue() * INSIDE_DRAIN;
        } else {
            VesselEntity vessel = vessel(level, cap);

            double ratio = vessel == null ? 1.0D
                    : Math.min(1.0D, Math.sqrt(player.distanceToSqr(vessel)) / REACH);

            cost = (float) (DRAIN_NEAR + (DRAIN_FAR - DRAIN_NEAR) * ratio * ratio);
        }

        if (cap.getEnergy() < cost) return false;

        cap.useEnergy(cost);

        PacketHandler.sendToClient(new SyncEnergyS2CPacket(cap.getEnergy()), player);

        return true;
    }

    @Nullable
    public static VesselEntity vessel(ServerLevel level, ISorcererData cap) {
        UUID id = cap.getVessel();

        return id != null && level.getEntity(id) instanceof VesselEntity vessel && !vessel.isRemoved()
                ? vessel : null;
    }

    public static boolean isVessel(Entity entity) {
        return entity instanceof VesselEntity;
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class PossessionForgeEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.isPossessing()) return;
            if (event.getAmount() < player.getHealth()) return;

            event.setCanceled(true);

            release(player, true);
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof VesselEntity vessel)) return;
            if (!(vessel.getOwner() instanceof ServerPlayer player)) return;

            ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.isPossessing()) return;

            release(player, false);

            player.setHealth(1.0F);
            player.hurt(event.getSource(), Float.MAX_VALUE);
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (!(event.player instanceof ServerPlayer player)) return;
            if (player.tickCount % SURVEY_INTERVAL != 0) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.hasPuppetRegister()) return;

            for (PuppetRecord record : cap.getPuppets()) {
                UUID id = record.getEntity();

                if (id != null && level.getEntity(id) instanceof PuppetEntity puppet) {
                    record.setPos(puppet.position());
                    record.setHealth(puppet.getHealth());
                    record.setMaxHealth(puppet.getMaxHealth());
                }

                record.setDistance((float) Math.sqrt(record.getPos().distanceToSqr(player.position())));
            }
        }
    }
}

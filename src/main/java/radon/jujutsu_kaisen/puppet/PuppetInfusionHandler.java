package radon.jujutsu_kaisen.puppet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.puppet_manipulation.PuppetAbility;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncEnergyS2CPacket;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PuppetInfusionHandler {
    public static final int DURATION = 600;

    public static final double REACH = 4.0D;

    public static final float COST = 750.0F;

    private static final float COST_PER_TICK = COST / DURATION;

    private static final Set<UUID> INFUSING = new HashSet<>();

    private PuppetInfusionHandler() {
    }

    public static void setInfusing(ServerPlayer player, boolean infusing) {
        if (infusing) {
            INFUSING.add(player.getUUID());
        } else {
            INFUSING.remove(player.getUUID());
        }
    }

    public static PuppetEntity candidate(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.hasPuppetRegister()) return null;

        if (cap.getPuppets().size() >= PuppetAbility.MAX_PUPPETS) return null;

        if (!owner.isShiftKeyDown()) return null;

        LivingEntity looked = RotationUtil.getExpandedLookAt(owner, REACH);

        if (!(looked instanceof PuppetEntity puppet)) return null;

        return puppet.isLinked() ? null : puppet;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        UUID id = player.getUUID();

        if (!INFUSING.contains(id)) return;

        PuppetEntity puppet = candidate(player);

        if (puppet == null) {
            INFUSING.remove(id);
            return;
        }

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (cap.getEnergy() < COST_PER_TICK) {
            INFUSING.remove(id);
            return;
        }

        if (!id.equals(puppet.getInfuser())) {
            puppet.setInfuser(id);
            puppet.setInfusion(0);
        }

        cap.useEnergy(COST_PER_TICK);

        PacketHandler.sendToClient(new SyncEnergyS2CPacket(cap.getEnergy()), player);

        int ticks = puppet.getInfusion() + 1;

        puppet.setInfusion(ticks);

        if (ticks < DURATION) return;

        link(player, puppet);
        INFUSING.remove(id);
    }

    private static void link(ServerPlayer player, PuppetEntity puppet) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.hasPuppetRegister()) return;

        String name = cap.nextPuppetName();

        PuppetRecord record = new PuppetRecord(name, puppet.getUUID(),
                puppet.getHealth(), puppet.getMaxHealth(), puppet.position());

        record.setDimension(player.level().dimension().location().toString());

        cap.addPuppet(record);

        puppet.link(player, name);
        puppet.clearInfusion();

        cap.syncFull();

        player.sendSystemMessage(Component.translatable("chat.jujutsu_kaisen.puppet_infused", name)
                .withStyle(ChatFormatting.GRAY));
    }
}

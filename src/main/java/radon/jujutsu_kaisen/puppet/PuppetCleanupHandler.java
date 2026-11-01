package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.PuppetEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PuppetCleanupHandler {
    private static final int INTERVAL = 100;

    private PuppetCleanupHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount < INTERVAL || player.tickCount % INTERVAL != 0) return;

        MinecraftServer server = player.getServer();

        if (server == null) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || !cap.hasPuppetRegister()) return;

        List<PuppetRecord> stale = new ArrayList<>();

        for (PuppetRecord record : cap.getPuppets()) {
            UUID id = record.getEntity();

            if (id == null) continue;

            ServerLevel level = level(server, record);

            if (level == null) continue;

            BlockPos pos = BlockPos.containing(record.getPos());

            if (!level.isLoaded(pos)) continue;

            if (level.getEntity(id) instanceof PuppetEntity) continue;

            stale.add(record);
        }

        if (stale.isEmpty()) return;

        for (PuppetRecord record : stale) cap.removePuppet(record);

        cap.syncFull();
    }

    @Nullable
    private static ServerLevel level(MinecraftServer server, PuppetRecord record) {
        String dimension = record.getDimension();

        if (dimension.isEmpty()) return server.overworld();

        ResourceLocation key = ResourceLocation.tryParse(dimension);

        if (key == null) return null;

        return server.getLevel(ResourceKey.create(Registries.DIMENSION, key));
    }
}

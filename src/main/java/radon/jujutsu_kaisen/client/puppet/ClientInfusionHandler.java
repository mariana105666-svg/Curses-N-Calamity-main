package radon.jujutsu_kaisen.client.puppet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.InfusePuppetC2SPacket;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.puppet.PuppetInfusionHandler;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, value = Dist.CLIENT)
public class ClientInfusionHandler {
    private static PuppetEntity target;

    private static boolean sent;

    private ClientInfusionHandler() {
    }

    public static float getProgress() {
        return target == null ? 0.0F : (float) target.getInfusion() / PuppetInfusionHandler.DURATION;
    }

    public static boolean isInfusing() {
        return target != null && sent;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            reset();
            return;
        }

        target = mc.options.keyUse.isDown() ? PuppetInfusionHandler.candidate(player) : null;

        boolean infusing = target != null;

        if (infusing != sent) {
            PacketHandler.sendToServer(new InfusePuppetC2SPacket(infusing));
            sent = infusing;
        }
    }

    private static void reset() {
        target = null;
        sent = false;
    }
}

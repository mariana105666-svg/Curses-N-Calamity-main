package radon.jujutsu_kaisen.client.domain;

import net.minecraft.client.Minecraft;
import radon.jujutsu_kaisen.network.packet.s2c.DomainClashS2CPacket;

import java.util.List;

public class ClientDomainClashHandler {
    private static final int TIMEOUT = 10;

    private static List<DomainClashS2CPacket.Entry> entries = List.of();
    private static long updated = Long.MIN_VALUE;

    public static void accept(List<DomainClashS2CPacket.Entry> incoming) {
        entries = incoming;

        Minecraft mc = Minecraft.getInstance();
        updated = mc.level == null ? Long.MIN_VALUE : mc.level.getGameTime();
    }

    public static List<DomainClashS2CPacket.Entry> getEntries() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.level.getGameTime() - updated > TIMEOUT) return List.of();

        return entries;
    }
}

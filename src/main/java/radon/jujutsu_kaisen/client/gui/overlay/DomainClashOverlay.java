package radon.jujutsu_kaisen.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import radon.jujutsu_kaisen.client.domain.ClientDomainClashHandler;
import radon.jujutsu_kaisen.network.packet.s2c.DomainClashS2CPacket;

import java.util.List;

public class DomainClashOverlay {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;
    private static final int TOP = 8;
    private static final int GAP = 2;

    private static final int BACKGROUND = 0xA0000000;
    private static final int BORDER = 0xFF000000;
    private static final int SELF = 0xFF55FFFF;
    private static final int ENEMY = 0xFFFF5555;

    public static IGuiOverlay OVERLAY = (gui, graphics, partialTicks, width, height) -> {
        Minecraft mc = gui.getMinecraft();

        if (mc.player == null || mc.options.hideGui) return;

        List<DomainClashS2CPacket.Entry> entries = ClientDomainClashHandler.getEntries();

        if (entries.size() < 2) return;

        int count = entries.size();
        int left = (width - WIDTH) / 2;
        int slot = WIDTH / count;
        int centre = left + WIDTH / 2;

        graphics.fill(left - 1, TOP - 1, left + WIDTH + 1, TOP + HEIGHT + 1, BORDER);
        graphics.fill(left, TOP, left + WIDTH, TOP + HEIGHT, BACKGROUND);

        for (int i = 0; i < count; i++) {
            DomainClashS2CPacket.Entry entry = entries.get(i);

            int slotLeft = left + (i * slot) + (i > 0 ? GAP : 0);
            int slotRight = left + ((i + 1) * slot) - (i < count - 1 ? GAP : 0);

            int filled = Math.round((slotRight - slotLeft) * Math.max(0.0F, Math.min(1.0F, entry.fraction())));
            int colour = entry.self() ? SELF : ENEMY;

            if (slotRight <= centre) {
                graphics.fill(slotRight - filled, TOP, slotRight, TOP + HEIGHT, colour);
            } else if (slotLeft >= centre) {
                graphics.fill(slotLeft, TOP, slotLeft + filled, TOP + HEIGHT, colour);
            } else {
                int mid = (slotLeft + slotRight) / 2;
                graphics.fill(mid - filled / 2, TOP, mid + filled / 2, TOP + HEIGHT, colour);
            }
        }
        drawNames(graphics, mc, entries, left, slot, count);
    };

    private static void drawNames(GuiGraphics graphics, Minecraft mc, List<DomainClashS2CPacket.Entry> entries,
                                  int left, int slot, int count) {
        int y = TOP + HEIGHT + 3;

        for (int i = 0; i < count; i++) {
            DomainClashS2CPacket.Entry entry = entries.get(i);

            Component name = Component.literal(entry.name());
            int x = left + (i * slot) + (slot / 2);

            graphics.drawCenteredString(mc.font, name, x, y, entry.self() ? SELF : ENEMY);
        }
    }
}

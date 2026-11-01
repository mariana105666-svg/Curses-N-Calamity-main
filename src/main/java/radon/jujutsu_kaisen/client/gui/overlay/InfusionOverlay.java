package radon.jujutsu_kaisen.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Vector3f;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.puppet.ClientInfusionHandler;

public class InfusionOverlay {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 6;

    private static final int BOTTOM_GAP = 68;

    private static final int LABEL_GAP = 4;

    private static final int BACKGROUND = 0xC0000000;
    private static final int BORDER = 0xFF000000;

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        if (!ClientInfusionHandler.isInfusing()) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) return;

        int x = (screenWidth - WIDTH) / 2;
        int y = screenHeight - BOTTOM_GAP;

        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BACKGROUND);

        Vector3f color = ParticleColors.getCursedEnergyColor(mc.player);

        int rgb = 0xFF000000
                | ((int) (color.x() * 0xFF) << 16)
                | ((int) (color.y() * 0xFF) << 8)
                | (int) (color.z() * 0xFF);

        int filled = (int) (WIDTH * ClientInfusionHandler.getProgress());

        if (filled > 0) graphics.fill(x, y, x + filled, y + HEIGHT, rgb);

        Component label = Component.translatable("gui.jujutsu_kaisen.infusing_ce");

        graphics.drawCenteredString(mc.font, label, screenWidth / 2,
                y - LABEL_GAP - mc.font.lineHeight, 0xFFFFFFFF);
    };
}

package radon.jujutsu_kaisen.client.gui.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import radon.jujutsu_kaisen.client.particle.ParticleColors;

import java.util.List;
import java.util.Locale;

public class ClientNodeTooltip implements ClientTooltipComponent {
    private static final int WRAP = 170;

    private static final int LINE = 9;
    private static final int GAP = 3;

    private static final int BAR_WIDTH = 72;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_GAP = 5;

    private static final int DESCRIPTION = 0xFFAAAAAA;
    private static final int READOUT = 0xFFFFFFFF;

    private static final int BACKGROUND = 0xFF0C0C12;
    private static final int BORDER = 0xFF2A2A3A;

    private final List<FormattedCharSequence> lines;
    private final boolean bar;
    private final float value;
    private final float max;
    private final String readout;
    private final String label;

    public ClientNodeTooltip(NodeTooltip tooltip) {
        this.lines = Minecraft.getInstance().font.split(tooltip.description(), WRAP);
        this.bar = tooltip.bar();
        this.value = tooltip.value();
        this.max = tooltip.max();
        this.label = tooltip.label().getString();

        this.readout = this.bar
                ? (this.value == Math.floor(this.value) ? String.valueOf((int) this.value) : String.format(Locale.ROOT, "%.1f", this.value))
                : String.format(Locale.ROOT, "%.2f", this.value);
    }

    private int labelWidth(Font font) {
        return this.label.isEmpty() ? 0 : font.width(this.label) + BAR_GAP;
    }

    private int readoutWidth(Font font) {
        return this.bar ? this.labelWidth(font) + BAR_WIDTH + 2 + BAR_GAP + font.width(this.readout) : font.width(this.readout);
    }

    @Override
    public int getHeight() {
        return this.lines.size() * LINE + GAP + (this.bar ? BAR_HEIGHT + 2 : LINE);
    }

    @Override
    public int getWidth(@NotNull Font font) {
        int width = this.readoutWidth(font);

        for (FormattedCharSequence line : this.lines) width = Math.max(width, font.width(line));

        return width;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        for (int i = 0; i < this.lines.size(); i++) {
            graphics.drawString(font, this.lines.get(i), x, y + i * LINE, DESCRIPTION, false);
        }

        int top = y + this.lines.size() * LINE + GAP;

        if (!this.bar) {
            int span = this.getWidth(font);

            graphics.drawString(font, this.readout, x + (span - font.width(this.readout)) / 2, top, READOUT, false);
            return;
        }

        int middle = top + (BAR_HEIGHT + 2 - font.lineHeight) / 2 + 1;

        if (!this.label.isEmpty()) {
            graphics.drawString(font, this.label, x, middle, READOUT, false);
        }

        int left = x + this.labelWidth(font);

        graphics.fill(left, top, left + BAR_WIDTH + 2, top + BAR_HEIGHT + 2, BORDER);
        graphics.fill(left + 1, top + 1, left + BAR_WIDTH + 1, top + BAR_HEIGHT + 1, BACKGROUND);

        int filled = this.max <= 0.0F ? 0
                : Mth.floor(BAR_WIDTH * Mth.clamp(this.value / this.max, 0.0F, 1.0F));

        if (filled > 0) {
            graphics.fill(left + 1, top + 1, left + 1 + filled, top + BAR_HEIGHT + 1, colour());
        }

        graphics.drawString(font, this.readout, left + BAR_WIDTH + 2 + BAR_GAP, middle, READOUT, false);
    }

    private static int colour() {
        Player player = Minecraft.getInstance().player;

        if (player == null) return 0xFFFFFFFF;

        Vector3f tint = ParticleColors.getCursedEnergyColor(player);

        return 0xFF000000
                | ((int) (tint.x() * 0xFF) << 16)
                | ((int) (tint.y() * 0xFF) << 8)
                | (int) (tint.z() * 0xFF);
    }
}

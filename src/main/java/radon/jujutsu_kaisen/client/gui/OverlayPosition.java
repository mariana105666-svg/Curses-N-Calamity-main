package radon.jujutsu_kaisen.client.gui;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public enum OverlayPosition {
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP_LEFT,
    TOP_RIGHT;

    public boolean isTop() {
        return this == TOP_LEFT || this == TOP_RIGHT;
    }

    public boolean isRight() {
        return this == TOP_RIGHT || this == BOTTOM_RIGHT;
    }

    public OverlayPosition next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public Component getName() {
        return Component.translatable(String.format("gui.%s.overlay_position.%s", JujutsuKaisen.MOD_ID,
                this.name().toLowerCase(java.util.Locale.ROOT)));
    }
}

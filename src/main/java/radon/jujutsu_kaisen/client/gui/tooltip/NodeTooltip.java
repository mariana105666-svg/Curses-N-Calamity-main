package radon.jujutsu_kaisen.client.gui.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record NodeTooltip(Component description, boolean bar, float value, float max, Component label) implements TooltipComponent {
}

package radon.jujutsu_kaisen.item.veil.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public class TeleportModifier extends Modifier {
    public TeleportModifier(Action action) {
        super(Type.TELEPORT, action);
    }

    public TeleportModifier(CompoundTag nbt) {
        super(nbt);
    }

    @Override
    public Component getComponent() {
        return Component.translatable(String.format("item.%s.veil_rod.teleport.%s", JujutsuKaisen.MOD_ID, this.getAction().name().toLowerCase()))
                .withStyle(this.getAction() == Action.DENY ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN);
    }
}

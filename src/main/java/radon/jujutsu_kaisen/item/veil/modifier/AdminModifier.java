package radon.jujutsu_kaisen.item.veil.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public class AdminModifier extends Modifier {
    public AdminModifier(Action action) {
        super(Type.ADMIN, action);
    }

    public AdminModifier(CompoundTag nbt) {
        super(nbt);
    }

    @Override
    public Component getComponent() {
        return Component.translatable(String.format("item.%s.veil_rod.admin", JujutsuKaisen.MOD_ID))
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}

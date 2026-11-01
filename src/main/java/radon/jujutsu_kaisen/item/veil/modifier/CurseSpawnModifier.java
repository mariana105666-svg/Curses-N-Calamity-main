package radon.jujutsu_kaisen.item.veil.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public class CurseSpawnModifier extends Modifier {
    public CurseSpawnModifier(Action action) {
        super(Type.CURSE_SPAWN, action);
    }

    public CurseSpawnModifier(CompoundTag nbt) {
        super(nbt);
    }

    @Override
    public Component getComponent() {
        return Component.translatable(String.format("item.%s.veil_rod.curse_spawn.%s", JujutsuKaisen.MOD_ID, this.getAction().name().toLowerCase()))
                .withStyle(this.getAction() == Action.DENY ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN);
    }
}

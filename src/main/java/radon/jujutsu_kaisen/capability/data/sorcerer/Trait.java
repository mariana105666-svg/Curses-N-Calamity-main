package radon.jujutsu_kaisen.capability.data.sorcerer;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;

public enum Trait {
    SIX_EYES,
    HEAVENLY_RESTRICTION,
    VESSEL,
    RCT_OUTPUT,
    INCARNATED,
    PERFECT_BODY,
    CURSED_WOMB,
    DEATH_PAINTING,
    PRODIGY,
    SIMURIAN,
    RAW_OUTPUT;

    public Component getName() {
        return Component.translatable(String.format("trait.%s.%s", JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }

    public String getRawName() {
        return this.name().toLowerCase();
    }
}

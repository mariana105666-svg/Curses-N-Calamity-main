package radon.jujutsu_kaisen.capability.data.sorcerer;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public enum Mastery {
    CURSED_ENERGY,
    TECHNIQUE,
    RCT,
    BARRIER,
    DEFENSE;

    public static final Mastery[] BRANCHES = {TECHNIQUE, RCT, BARRIER, DEFENSE};

    public Component getName() {
        return Component.translatable(String.format("mastery.%s.%s", JujutsuKaisen.MOD_ID, this.getRawName()));
    }

    public String getRawName() {
        return this.name().toLowerCase();
    }
}

package radon.jujutsu_kaisen.config;

import net.minecraftforge.common.ForgeConfigSpec;
import radon.jujutsu_kaisen.client.gui.MeleeMenuType;
import radon.jujutsu_kaisen.client.gui.OverlayPosition;

public class ClientConfig {
    public final ForgeConfigSpec.EnumValue<MeleeMenuType> meleeMenuType;
    public final ForgeConfigSpec.BooleanValue visibleCursedEnergy;
    public final ForgeConfigSpec.EnumValue<OverlayPosition> overlayPosition;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Settings").push("settings");
        this.meleeMenuType = builder.comment("The melee menu type")
                .defineEnum("meleeMenuType", MeleeMenuType.TOGGLE);
        this.visibleCursedEnergy = builder.comment("Whether or not cursed energy particles are visible")
                        .define("visibleCursedEnergy", true);
        this.overlayPosition = builder.comment("Where the cursed energy bar, output and experience are drawn")
                .defineEnum("overlayPosition", OverlayPosition.BOTTOM_LEFT);
        builder.pop();
    }
}

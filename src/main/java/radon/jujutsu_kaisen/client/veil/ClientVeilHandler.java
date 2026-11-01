package radon.jujutsu_kaisen.client.veil;

import net.minecraft.client.Minecraft;
import radon.jujutsu_kaisen.client.gui.screen.VeilConfigurationScreen;

public class ClientVeilHandler {
    public static void openConfiguration() {
        Minecraft.getInstance().setScreen(new VeilConfigurationScreen());
    }
}

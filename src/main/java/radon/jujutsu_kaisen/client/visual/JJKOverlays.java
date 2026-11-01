package radon.jujutsu_kaisen.client.visual;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.visual.base.IOverlay;
import radon.jujutsu_kaisen.client.visual.base.IVisual;
import radon.jujutsu_kaisen.client.visual.overlay.BrainTransplantOverlay;
import radon.jujutsu_kaisen.client.visual.overlay.CursedSpeechOverlay;
import radon.jujutsu_kaisen.client.visual.overlay.PerfectBodyOverlay;
import radon.jujutsu_kaisen.client.visual.overlay.SimurianOverlay;
import radon.jujutsu_kaisen.client.visual.overlay.SixEyesOverlay;
import radon.jujutsu_kaisen.client.visual.visual.BlueFistsVisual;
import radon.jujutsu_kaisen.client.visual.visual.CursedEnergyVisual;
import radon.jujutsu_kaisen.client.visual.visual.IdleTransfigurationVisual;
import radon.jujutsu_kaisen.client.visual.visual.TransfiguredSoulVisual;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JJKOverlays {
    public static List<IOverlay> OVERLAYS = new ArrayList<>();

    static {
        OVERLAYS.add(new SixEyesOverlay());
        OVERLAYS.add(new CursedSpeechOverlay());
        OVERLAYS.add(new BrainTransplantOverlay());
        OVERLAYS.add(new PerfectBodyOverlay());
        OVERLAYS.add(new SimurianOverlay());
    }
}

package radon.jujutsu_kaisen.client.gui.overlay;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import radon.jujutsu_kaisen.JujutsuKaisen;

import java.io.InputStream;

public class CursedEnergyBarTexture {
    private static final ResourceLocation SOURCE =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/gui/overlay/energy_bar.png");

    private static final ResourceLocation TARGET =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/gui/overlay/energy_bar_tinted");

    private static final int[] START = {107, 22, 175};
    private static final int[] END = {0, 191, 255};

    private static NativeImage source;

    private static DynamicTexture texture;

    private static int builtFor;

    private static boolean failed;

    private CursedEnergyBarTexture() {
    }

    public static ResourceLocation get(int color) {
        if (failed) return SOURCE;

        if (texture != null && builtFor == color) return TARGET;

        if (!load()) return SOURCE;

        repaint(color);

        builtFor = color;

        return TARGET;
    }

    private static boolean load() {
        if (source != null) return true;

        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(SOURCE)) {
            source = NativeImage.read(stream);
        } catch (Exception e) {
            failed = true;
            return false;
        }

        texture = new DynamicTexture(new NativeImage(source.getWidth(), source.getHeight(), false));

        Minecraft.getInstance().getTextureManager().register(TARGET, texture);

        return true;
    }

    private static void repaint(int color) {
        NativeImage pixels = texture.getPixels();

        if (pixels == null) {
            failed = true;
            return;
        }

        float[] wanted = {
                ((color >> 16) & 0xFF),
                ((color >> 8) & 0xFF),
                (color & 0xFF)
        };

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                pixels.setPixelRGBA(x, y, convert(source.getPixelRGBA(x, y), wanted));
            }
        }

        texture.upload();
    }

    private static int convert(int abgr, float[] wanted) {
        int a = (abgr >>> 24) & 0xFF;

        if (a == 0) return abgr;

        float b = (abgr >> 16) & 0xFF;
        float g = (abgr >> 8) & 0xFF;
        float r = abgr & 0xFF;

        float along = clamp((g - START[1]) / (float) (END[1] - START[1]));

        // What the ramp should be at that point once the far end is the caster's colour
        float[] target = {
                START[0] + (wanted[0] - START[0]) * along,
                START[1] + (wanted[1] - START[1]) * along,
                START[2] + (wanted[2] - START[2]) * along
        };

        float reference = luminance(
                START[0] + (END[0] - START[0]) * along,
                START[1] + (END[1] - START[1]) * along,
                START[2] + (END[2] - START[2]) * along);

        float shade = reference <= 0.0F ? 1.0F : luminance(r, g, b) / reference;

        return (a << 24)
                | (channel(target[2] * shade) << 16)
                | (channel(target[1] * shade) << 8)
                | channel(target[0] * shade);
    }

    private static float luminance(float r, float g, float b) {
        return 0.2126F * r + 0.7152F * g + 0.0722F * b;
    }

    private static float clamp(float value) {
        return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }
}

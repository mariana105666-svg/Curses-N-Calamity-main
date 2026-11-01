package radon.jujutsu_kaisen.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BlinkOverlay {
    private static FlashEvent current;
    private static ChatVisiblity previousChatVisibility;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (current == null) return;

        current.duration--;

        if (current.duration <= 0) {
            current = null;
            if (previousChatVisibility != null) {
                Minecraft mc = Minecraft.getInstance();
                mc.options.chatVisibility().set(previousChatVisibility);
                previousChatVisibility = null;
            }
        }
    }

    public static void flash(int setDuration) {
        Minecraft mc = Minecraft.getInstance();

        if (current == null) {
            previousChatVisibility = mc.options.chatVisibility().get();
            mc.options.chatVisibility().set(ChatVisiblity.HIDDEN);
        }
        current = new FlashEvent(setDuration);
    }

    public static IGuiOverlay OVERLAY = (gui, graphics, partialTicks, width, height) -> {
        if (current == null) return;
        

        float alpha = (float) current.duration / current.maxDuration;
        if (alpha >= 0.5) {
            alpha = 1;
        }
        else {
            alpha = alpha * 2;
        }
        graphics.fill(0, 0, width, height, HelperMethods.toRGB24(0,0,0, (int) (255 * alpha)));
        
    };

    public static class FlashEvent {
        public int duration;
        public int maxDuration;

        public FlashEvent(int duration) {
            this.duration = duration;
            this.maxDuration = duration;
        }
    }
}
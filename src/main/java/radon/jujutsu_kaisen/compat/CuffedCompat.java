package radon.jujutsu_kaisen.compat;

import net.minecraft.world.entity.player.Player;
import com.lazrproductions.cuffed.cap.base.IRestrainableCapability;
import com.lazrproductions.cuffed.cap.provider.RestrainableCapabilityProvider;



public class CuffedCompat {
    public static void load() {}

     public static boolean headRestrained(Player player) {
        return player.getCapability(RestrainableCapabilityProvider.CAP)
                     .map(cap -> ((IRestrainableCapability) cap).headRestrained())
                     .orElse(false);
    }

    public static boolean legsRestrained(Player player) {
        return player.getCapability(RestrainableCapabilityProvider.CAP)
                     .map(cap -> ((IRestrainableCapability) cap).legsRestrained())
                     .orElse(false);
    }

    public static boolean armsRestrained(Player player) {
        return player.getCapability(RestrainableCapabilityProvider.CAP)
                     .map(cap -> ((IRestrainableCapability) cap).armsRestrained())
                     .orElse(false);
    }
}
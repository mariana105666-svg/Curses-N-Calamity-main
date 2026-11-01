package radon.jujutsu_kaisen.compat;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ExperienceHandler;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.AbilityTriggerEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import team.creative.playerrevive.api.event.PlayerBleedOutEvent;
import team.creative.playerrevive.server.PlayerReviveServer;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.PLAYER_REVIVE, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerReviveCompat {
    public static void load() {
    }

    public static void Kill(Player player) {
        PlayerReviveServer.getBleeding(player).forceBledOut();
    }

    
    public static void Revive(Player player) {
        PlayerReviveServer.revive(player);
    }
    

    public static boolean IsBleedingOut(Player player) {
        return PlayerReviveServer.isBleeding(player);
    }

    @SubscribeEvent
    public static void PlayerBleedOutEvent(PlayerBleedOutEvent event) {
              LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel level)) return;

        if (!entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;

        LivingEntity attacker = null;
        RegistryAccess registry = level.registryAccess();
        if (event.getBleeding().getSource(registry).getEntity() instanceof LivingEntity living) {
            attacker = living;
        }
        //System.out.println("correct attacker");
        ExperienceHandler.killExperience(entity, attacker, level);
    }
}

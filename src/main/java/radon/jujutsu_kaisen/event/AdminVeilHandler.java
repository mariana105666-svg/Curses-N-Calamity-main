package radon.jujutsu_kaisen.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdminVeilHandler {
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;

        if (VeilHandler.isAdminBlocked(level, event.getPos(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();

        LivingEntity instigator = event.getExplosion().getIndirectSourceEntity();

        event.getAffectedEntities().removeIf(entity ->
                VeilHandler.isAdminBlocked(level, entity.blockPosition(), instigator));
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();

        Entity attacker = event.getSource().getEntity();

        if (attacker == null) return;

        Level level = victim.level();
        BlockPos pos = victim.blockPosition();

        if (VeilHandler.isAdminBlocked(level, pos, attacker)
                || VeilHandler.isAdminBlocked(level, attacker.blockPosition(), attacker)) {
            event.setCanceled(true);
        }
    }
}

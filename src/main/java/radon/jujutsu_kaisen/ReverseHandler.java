package radon.jujutsu_kaisen;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReverseHandler {
    public static final float PER_ROW = 0.1F;
    public static final float ROW_HEALTH = 20.0F;
    public static final float PER_LIMB = 0.4F;

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity owner = event.getEntity();

        if (owner.level().isClientSide) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        Ability channeled = cap.getChanneled();

        if (channeled == null || !channeled.isReverseCursedTechnique()) return;

        float room = owner.getMaxHealth() - owner.getHealth();

        if (room <= 0.0F) return;

        float healed = Math.min(event.getAmount(), room);

        if (healed <= 0.0F) return;

        cap.addReverseEfficiency(healed / ROW_HEALTH * PER_ROW);
    }
}

package radon.jujutsu_kaisen.client.visual.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.base.IVisual;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.CuriosUtil;
import radon.jujutsu_kaisen.util.HelperMethods;

public class CursedEnergyVisual implements IVisual {
    public static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createThreadSafe);

    @Override
    public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.player == null) return false;
        ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        if (cap == null) return false;
        if (!ConfigHolder.CLIENT.visibleCursedEnergy.get()) return false;
        if (!data.toggled.contains(JJKAbilities.CURSED_ENERGY_FLOW.get())) return false;

        if (data.channeled == JJKAbilities.CURSED_ENERGY_SHIELD.get()) return true;

        if (entity == mc.player) return false;

        if (!cap.hasTrait(Trait.SIX_EYES)) return false;

        return !mc.player.getItemBySlot(EquipmentSlot.HEAD).is(JJKItems.BLINDFOLD.get())
                && !CuriosUtil.findSlot(mc.player, "head").is(JJKItems.BLINDFOLD.get());
    }

    @Override
    public void tick(LivingEntity entity, ClientVisualHandler.ClientData data) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;

        float scale = data.channeled == JJKAbilities.CURSED_ENERGY_SHIELD.get() ? 1.5F : 1.0F;
        RandomSource random = RANDOM.get();
        mc.execute(() -> {
        for (int i = 0; i < 12 * scale; i++) {
            double x = entity.getX() + (random.nextDouble() - 0.5D) * (entity.getBbWidth() * 1.5F * scale) - entity.getLookAngle().scale(0.35D).x;
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5D) * (entity.getBbWidth() * 1.5F * scale) - entity.getLookAngle().scale(0.35D).z;
            double speed = (entity.getBbHeight() * 0.3F) * random.nextDouble();
            mc.level.addParticle(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.getCursedEnergyColor(entity), entity.getBbWidth() * 0.5F * scale,
                    0.2F, 6), x, y, z, 0.0D, speed * scale, 0.0D);
        }
        });
    }
}

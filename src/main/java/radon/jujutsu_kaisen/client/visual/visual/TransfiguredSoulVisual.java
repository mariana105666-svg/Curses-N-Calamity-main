package radon.jujutsu_kaisen.client.visual.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.idle_transfiguration.IdleTransfiguration;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.client.visual.base.IVisual;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.util.HelperMethods;

public class TransfiguredSoulVisual implements IVisual {
    public static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createThreadSafe);

    @Override
    public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return false;

        if (!JJKAbilities.getTechniques(mc.player).contains(CursedTechnique.IDLE_TRANSFIGURATION)) return false;

        return entity.hasEffect(JJKEffects.TRANSFIGURED_SOUL.get());
    }

    @Override
    public void tick(LivingEntity entity, ClientVisualHandler.ClientData data) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        MobEffectInstance instance = entity.getEffect(JJKEffects.TRANSFIGURED_SOUL.get());

        if (instance == null) return;

        int amplifier = Math.min(6, instance.getAmplifier());

        // float attackerStrength = IdleTransfiguration.calculateStrength(mc.player);
        // float victimStrength = IdleTransfiguration.calculateStrength(entity);

        // int required = Math.round((victimStrength / attackerStrength) * 2);

        if (amplifier > 0) {
            int count = Math.max(1, Math.round((entity.getBbWidth() + entity.getBbHeight())/2 ) * (1+(amplifier/3))) ;
            RandomSource random = RANDOM.get();
            mc.execute(() -> {
            for (int i = 0; i < count; i++) {
                double x = entity.getX() + (random.nextDouble() - 0.5D) * (entity.getBbWidth() * 2);
                double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
                double z = entity.getZ() + (random.nextDouble() - 0.5D) * (entity.getBbWidth() * 2);
                mc.level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0D, random.nextDouble() * 0.1D, 0.0D);
            }
            });
        }
    }
}

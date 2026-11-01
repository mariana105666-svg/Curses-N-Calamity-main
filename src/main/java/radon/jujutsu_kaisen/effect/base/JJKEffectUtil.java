package radon.jujutsu_kaisen.effect.base;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class JJKEffectUtil {

    private JJKEffectUtil() {}

    public static MobEffectInstance create(MobEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        MobEffectInstance inst = new MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon);
        inst.getCurativeItems().clear();
        return inst;
    }

    public static void addEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        MobEffectInstance inst = create(effect, duration, amplifier, ambient, showParticles, showIcon);
        entity.addEffect(inst);
    }
    public static void addEffect(LivingEntity entity, MobEffectInstance effect) {
        effect.getCurativeItems().clear();
        entity.addEffect(effect);
    }
}
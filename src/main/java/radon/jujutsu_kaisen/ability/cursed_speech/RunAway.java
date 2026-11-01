package radon.jujutsu_kaisen.ability.cursed_speech;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.CursedSpeechParticle;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class RunAway extends CursedSpeech {
    private static final double RANGE = 25.0D;
    private static final double RADIUS = 2.5D;
    private static final int DURATION = 35;

    double REALRADIUS = RADIUS;
    double REALRANGE = RANGE;


    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public Ability.ActivationType getActivationType(LivingEntity owner) {
        return Ability.ActivationType.INSTANT;
    }

    private List<Entity> getEntities(LivingEntity owner) {
        List<Entity> audience = this.getAudience(owner);

        if (audience != null) return audience;

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 src = owner.getEyePosition();

        if (ChantHandler.isChanted(owner, this)) {
            float output = (ChantHandler.getOutput(owner, this));
            REALRANGE = RANGE * output;
            REALRADIUS = RADIUS * output;
            AABB bounds = AABB.ofSize(src, 1.0D, 1.0D, 1.0D).expandTowards(look.scale(REALRANGE)).inflate(REALRADIUS);
            return owner.level().getEntities(owner, bounds, entity -> !(entity instanceof LivingEntity living) || owner.canAttack(living));
        }

        AABB bounds = AABB.ofSize(src, 1.0D, 1.0D, 1.0D).expandTowards(look.scale(RANGE)).inflate(RADIUS);
        return owner.level().getEntities(owner, bounds, entity -> !(entity instanceof LivingEntity living) || owner.canAttack(living));
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        Vec3 src = owner.getEyePosition();

        for (int i = 1; i < REALRANGE + 7; i++) {
            Vec3 dst = src.add(look.scale(i));
                                    ((ServerLevel) owner.level()).sendParticles(new CursedSpeechParticle.CursedSpeechParticleOptions(new Vector3f(0.376F, 1.0F, 0.376F), (float)(src.distanceTo(dst) * 0.5D) ),
                                    dst.x, dst.y, dst.z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
        }

        owner.level().playSound(null, src.x, src.y, src.z, JJKSounds.CURSED_SPEECH.get(), SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);

        //ISorcererData selfCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        //boolean hitTarget = false;
        for (Entity entity : getEntities(owner)) {
            if (!(entity instanceof LivingEntity living)) continue;
                JJKEffectUtil.addEffect(living, new MobEffectInstance(MobEffects.JUMP, Mth.clamp(Math.round(DURATION * this.getPower(owner)), 10*20,20*20), 1, false, false, false));
                JJKEffectUtil.addEffect(living, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Mth.clamp(Math.round(DURATION * this.getPower(owner)), 10*20,20*20), 5, false, false, false));
                JJKEffectUtil.addEffect(living, new MobEffectInstance(MobEffects.DIG_SPEED, Mth.clamp(Math.round(DURATION * this.getPower(owner)), 10*20,20*20), 0, false, false, false));
                if (living.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                    ISorcererData cap = living.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    cap.setDisarmed(Math.round(DURATION * this.getPower(owner)));
                }
               // selfCap.hurtThroat(this.getThroatDamage());
            if (entity instanceof Player player) {
                player.sendSystemMessage(Component.translatable(String.format("chat.%s.run_away", JujutsuKaisen.MOD_ID), owner.getName()));
            }
        }
    }

    @Override
    public int getThroatDamage() {
        return 2 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 100.0F;
    }

    @Override
    public int getCooldown() {
        return 24 * 20;
    }

    @Override
    public Classification getClassification() {
        return Classification.CURSED_SPEECH;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

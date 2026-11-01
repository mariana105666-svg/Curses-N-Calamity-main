package radon.jujutsu_kaisen.ability.cursed_speech;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.CursedSpeechParticle;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class BurnUp extends CursedSpeech {
    private static final double RANGE = 30.0D;
    private static final double RADIUS = 2.5D;
    private static final float DAMAGE = 11.0F;

    double REALRADIUS = RADIUS;
    double REALRANGE = RANGE;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return getEntities(owner).contains(target) && HelperMethods.RANDOM.nextInt(5) == 0 && target != null && owner.hasLineOfSight(target);
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
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

    private void spawnParticles(Entity entity) {
        double x = entity.getX();
        double y = entity.getY() + (entity.getBbHeight() / 2.0F);
        double z = entity.getZ();

        for (int i = 0; i < 24; i++) {
            double scale = HelperMethods.RANDOM.nextDouble() * 0.5D + 0.5D;
            Vec3 speed = new Vec3(HelperMethods.RANDOM.nextGaussian(), HelperMethods.RANDOM.nextGaussian(), HelperMethods.RANDOM.nextGaussian())
                    .normalize().scale(scale);

            double offsetX = x + speed.x;
            double offsetY = y + speed.y;
            double offsetZ = z + speed.z;

            ((ServerLevel) entity.level()).sendParticles((ParticleOptions) ParticleTypes.FLAME, offsetX, offsetY, offsetZ, 0,
                    speed.x, speed.y, speed.z, 1.0D);
        }
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        Vec3 src = owner.getEyePosition();

        for (int i = 1; i < REALRANGE + 7; i++) {
            Vec3 dst = src.add(look.scale(i));
            //((ServerLevel) owner.level()).sendParticles(JJKParticles.CURSED_SPEECH.get(), dst.x, dst.y, dst.z, 0, src.distanceTo(dst) * 0.5D, 0.0D, 0.0D, 1.0D);
            ((ServerLevel) owner.level()).sendParticles(new CursedSpeechParticle.CursedSpeechParticleOptions(new Vector3f(1.0F, 0.514F, 0.188F), (float)(src.distanceTo(dst) * 0.5D) ),
                                    dst.x, dst.y, dst.z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
                                }

        owner.level().playSound(null, src.x, src.y, src.z, JJKSounds.CURSED_SPEECH.get(), SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);
        owner.level().playSound(null, src.x, src.y, src.z, SoundEvents.VEX_CHARGE, SoundSource.MASTER, 1F, 0.5F + HelperMethods.RANDOM.nextFloat() * 0.2F);
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();    
        for (Entity entity : getEntities(owner)) {
            if (entity instanceof LivingEntity living && JJKAbilities.hasToggled(living, JJKAbilities.INFINITY.get())) continue;
             if (entity instanceof Player player) {
                player.sendSystemMessage(Component.translatable(String.format("chat.%s.burn_up", JujutsuKaisen.MOD_ID), owner.getName()));
            }
            cap.delayTickEvent(() -> {
            if (entity != null && entity.hurt(JJKDamageSources.jujutsuAttack(owner, this), DAMAGE * this.getPower(owner))) {
                //Vec3 center = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
                // ((ServerLevel) owner.level()).sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                // ((ServerLevel) owner.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                // owner.level().playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                //         4.0F, (1.0F + (HelperMethods.RANDOM.nextFloat() - HelperMethods.RANDOM.nextFloat()) * 0.2F) * 0.7F);
                // if (entity.hurt(JJKDamageSources.indirectJujutsuAttack(owner, owner, this), DAMAGE * this.getPower(owner))) {
                entity.setSecondsOnFire(4);
                this.spawnParticles(entity);
               // }
                
                entity.hurtMarked = true;
            }
            }, 15);
           
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 50.0F;
    }

    @Override
    public int getCooldown() {
        return 14 * 20;
    }

    @Override
    public int getThroatDamage() {
        return 1 * 20;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public Classification getClassification() {
        return Classification.CURSED_SPEECH;
    }
}

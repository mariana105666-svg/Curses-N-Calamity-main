package radon.jujutsu_kaisen.ability.cursed_speech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.CursedSpeechParticle;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.CombatRules;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class GetCrushed extends CursedSpeech {
    private static final double RANGE = 30.0D;
    private static final double RADIUS = 2.5D;
    private static final float DAMAGE = 17.0F;
    private static final double CRUSH_POWER = 20.0D;

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

    @Override
    public void run(LivingEntity owner) {
        if (owner.level().isClientSide) return;

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        Vec3 src = owner.getEyePosition();

        for (int i = 1; i < REALRANGE + 7; i++) {
            Vec3 dst = src.add(look.scale(i));
              ((ServerLevel) owner.level()).sendParticles(new CursedSpeechParticle.CursedSpeechParticleOptions(new Vector3f(0.345F, 0.075F, 0.345F), (float)(src.distanceTo(dst) * 0.5D) ),
                                    dst.x, dst.y, dst.z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
                                }

        owner.level().playSound(null, src.x, src.y, src.z, JJKSounds.CURSED_SPEECH.get(), SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow(); 
        for (Entity entity : getEntities(owner)) {
            if (!(entity instanceof LivingEntity living) || JJKAbilities.hasToggled(living, JJKAbilities.INFINITY.get())) continue;
            if (entity instanceof Player player) {
                player.sendSystemMessage(Component.translatable(String.format("chat.%s.get_crushed", JujutsuKaisen.MOD_ID), owner.getName()));
            } 
            cap.delayTickEvent(() -> {
            if (entity != null && entity.hurt(JJKDamageSources.jujutsuAttack(owner, this), DAMAGE * this.getPower(owner))) {

            Vec3 center = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
            ((ServerLevel) owner.level()).sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
            ((ServerLevel) owner.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
            owner.level().playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    4.0F, (1.0F + (HelperMethods.RANDOM.nextFloat() - HelperMethods.RANDOM.nextFloat()) * 0.2F) * 0.7F);

            float radius = 5.0F;//Math.min(10.0F, entity.getBbWidth() * entity.getBbHeight() * 2.0F);
            int minX = Mth.floor(entity.getX() - radius - 1.0F);
            int maxX = Mth.floor(entity.getX() + radius + 1.0F);
            int minY = Mth.floor(entity.getY() - radius - 1.0F);
            int maxY = Mth.floor(entity.getY() + radius + 1.0F);
            int minZ = Mth.floor(entity.getZ() - radius - 1.0F);
            int maxZ = Mth.floor(entity.getZ() + radius + 1.0F);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double distance = (x - entity.getX()) * (x - entity.getX()) +
                                (y - entity.getY()) * (y - entity.getY()) +
                                (z - entity.getZ()) * (z - entity.getZ());

                        if (distance <= radius * radius) {
                            BlockPos pos = new BlockPos(x, y, z);

                            if (HelperMethods.isDestroyable(owner.level(), owner, pos)) {
                                owner.level().destroyBlock(pos, false);
                            }
                        }
                    }
                }
            }
             entity.hurtMarked = true;
             ISorcererData capHit = null;
                                 if (entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                                    capHit = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();  
                                 }  
            if (capHit == null || !capHit.isChanneling(JJKAbilities.CURSED_ENERGY_SHIELD.get()  )) {
                entity.setDeltaMovement(0.0D, CRUSH_POWER * this.getPower(owner) * -1.0D, 0.0D);
                JJKEffectUtil.addEffect(living, new MobEffectInstance(JJKEffects.STUN.get(), 20, 0, false, false, false));
            }
           
            }
            
            }, 15);
        }
     
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 125.0F;
    }

    @Override
    public int getCooldown() {
        return 12 * 20;
    }

    @Override
    public int getThroatDamage() {
        return 2 * 20;
    }

    @Override
    public Classification getClassification() {
        return Classification.CURSED_SPEECH;
    }
}

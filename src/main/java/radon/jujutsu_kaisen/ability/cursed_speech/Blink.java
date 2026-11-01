package radon.jujutsu_kaisen.ability.cursed_speech;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.CursedSpeechParticle;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.BlinkS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.ScreenFlashS2CPacket;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class Blink extends CursedSpeech {
    private static final double RANGE = 25.0D;
    private static final double RADIUS = 2.5D;
    private static final int DURATION = 15;

    double REALRADIUS = RADIUS;
    double REALRANGE = RANGE;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return getEntities(owner).contains(target) && HelperMethods.RANDOM.nextInt(5) == 0 && target != null && owner.hasLineOfSight(target);
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
            ((ServerLevel) owner.level()).sendParticles(new CursedSpeechParticle.CursedSpeechParticleOptions(new Vector3f(0.115F, 0.115F, 0.115F), (float)(src.distanceTo(dst) * 0.5D) ),
                                    dst.x, dst.y, dst.z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
        }

        owner.level().playSound(null, src.x, src.y, src.z, JJKSounds.CURSED_SPEECH.get(), SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);
         ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();    
        for (Entity entity : getEntities(owner)) {     
            if (!(entity instanceof LivingEntity living) || JJKAbilities.hasToggled(living, JJKAbilities.INFINITY.get())) continue;
                if (entity instanceof Player player) {
                    player.sendSystemMessage(Component.translatable(String.format("chat.%s.blink", JujutsuKaisen.MOD_ID), owner.getName()));
                }
                //living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Mth.clamp(Math.round(DURATION * this.getPower(owner)), 3*20,5*20), 4, false, false, false));
                cap.delayTickEvent(() -> {  
                    if (entity != null && entity instanceof ServerPlayer player) {

                         ISorcererData capHit = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();    
                        if (!capHit.isChanneling(JJKAbilities.CURSED_ENERGY_SHIELD.get()  )) {
                            PacketHandler.sendToClient(new BlinkS2CPacket(Mth.clamp(Math.round(DURATION * this.getPower(owner)), 4*20,5*20)), player);
                        }
                       
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
        return 10 * 20;
    }

    @Override
    public int getThroatDamage() {
        return 1 * 20;
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

package radon.jujutsu_kaisen.ability.cursed_speech;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.CursedSpeechParticle;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.SummonEntity;
import radon.jujutsu_kaisen.entity.ten_shadows.DivineDogEntity;
import radon.jujutsu_kaisen.entity.ten_shadows.RabbitEscapeEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class Return extends CursedSpeech {
    private static final double RANGE = 25.0D;
    private static final double RADIUS = 2.5D;

    double REALRADIUS = RADIUS;
    double REALRANGE = RANGE;


    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
        //return getEntities(owner).contains(target) && HelperMethods.RANDOM.nextInt(5) == 0 && target != null && owner.hasLineOfSight(target);
    }

    @Override
    public Ability.ActivationType getActivationType(LivingEntity owner) {
        return Ability.ActivationType.INSTANT;
    }

    private  List<Entity> getEntities(LivingEntity owner) {
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
                                    ((ServerLevel) owner.level()).sendParticles(new CursedSpeechParticle.CursedSpeechParticleOptions(new Vector3f(0.729F, 1.0F, 1.0F), (float)(src.distanceTo(dst) * 0.5D) ),
                                    dst.x, dst.y, dst.z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
                                    
        }

        owner.level().playSound(null, src.x, src.y, src.z, JJKSounds.CURSED_SPEECH.get(), SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);
        ISorcererData selfCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        for (Entity entity : getEntities(owner)) {
         if (!(entity instanceof LivingEntity living)) continue;
         if (entity instanceof Player player) {
                        player.sendSystemMessage(
                            Component.translatable(
                                String.format("chat.%s.return", JujutsuKaisen.MOD_ID),
                                owner.getName()
                            )
                        );
                    }
            entity.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                if (cap.getType() == JujutsuType.SHIKIGAMI && living instanceof SummonEntity ownable && ownable.getOwner() != null) {             
                    if (!(entity instanceof Player player)) {
                       LivingEntity shikiowner = ownable.getOwner();
                       if (shikiowner != null && !(shikiowner == owner) ) {
                            float cost = ownable.getCost(shikiowner) * 500.0F;
                            if (ownable instanceof RabbitEscapeEntity ) {
                                cost /= 16;
                            } else if (ownable instanceof DivineDogEntity ) {
                                cost /= 2;
                            }
                            
                            if (cost == 0) {
                                cost = 1000;
                            }
                            else if (cost < 0) {
                                cost = Math.abs(cost);
                            }
                            if (selfCap.getEnergy() < cost) return;
                                selfCap.useEnergy(cost);
                                ISorcererData enemyCap = shikiowner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                                enemyCap.unsummonByClass(ownable.getClass());
                                 if (owner instanceof ServerPlayer player) {
                                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(selfCap.serializeNBT()), player);
                                 }
                       }
                    }            
                    
                }
            });
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 100.0F;
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
    }

    @Override
    public Classification getClassification() {
        return Classification.CURSED_SPEECH;
    }

    @Override
    public int getThroatDamage() {
        return 5 * 20;
    }

     @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

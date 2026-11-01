package radon.jujutsu_kaisen.ability.ratio;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.client.ClientWrapper;
import net.minecraft.world.damagesource.DamageSource;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import net.minecraft.core.particles.ParticleTypes;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import net.minecraft.world.effect.MobEffectInstance;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.particle.TravelParticle;
import radon.jujutsu_kaisen.util.ParticleUtil;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Bisection extends Ability implements Ability.IChannelened, Ability.IDurationable {
    private static final double RANGE = 50.0D;
    private static final int DELAY = 20;
    private static final float DAMAGE = 20.0F;
    private static final int DURATION = 26;
    private static final int STUN = 21;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || !owner.hasLineOfSight(target)) return false;

        if (JJKAbilities.isChanneling(owner, this)) {
            return HelperMethods.RANDOM.nextInt(5) != 0;
        }
        return HelperMethods.RANDOM.nextInt(3) == 0;
    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    /*private @Nullable BlockHitResult getBlockHit(LivingEntity owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        if (result.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) result;
        } else if (result.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) result).getEntity();
            Vec3 offset = entity.position().subtract(0.0D, 5.0D, 0.0D);
            return owner.level().clip(new ClipContext(entity.position(), offset, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        }
        return null;
    }*/


    @Override
    public void run(LivingEntity owner) {
            if (!(owner.level() instanceof ServerLevel level)) return;

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            Vec3 pos = owner.position();
            int index = this.getCharge(owner);

            JJKEffectUtil.addEffect(owner, new MobEffectInstance(JJKEffects.STUN.get(), 2, 1, false, false, false));

            float scale = 1.5F;

       // Minecraft mc = Minecraft.getInstance();

        //if (mc.player == null) return;

        for (int i = 0; i < 12 * scale; i++) {

            double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).x;
            double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * owner.getBbHeight();
            double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).z;
            double speed = (owner.getBbHeight() * 0.8F) * HelperMethods.RANDOM.nextDouble();

            level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.DARK_BLUE, owner.getBbWidth() * 1.0F,
                    0.2F, 16), x, y, z, 0, 0.0D, speed, 0.0D, 1.0D);
             }


            if (index == 15) {
                        if (owner instanceof ServerPlayer player) {
                            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.COLLAPSE.get(), SoundSource.MASTER, 1.0F, 1.0F);
                    //owner.level().sendParticles(ParticleTypes.SONIC_BOOM, pos.X, pos.Y, pos.Z, 0, 0D, 0.0D, 0.0D, 1.0D);
                }
            }
        }



    @Override
    public void onStop(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        owner.swing(InteractionHand.MAIN_HAND);
        int index = this.getCharge(owner);

        LivingEntity target = RotationUtil.getExpandedLookAt(owner,RANGE);

        if (target != null) {
            if (index >= 20 && index < DURATION) {
                Vec3 targeter = target.position();

                owner.teleportTo(targeter.x, targeter.y, targeter.z);

                target.hurt(JJKDamageSources.jujutsuAttack(owner, JJKAbilities.BISECTION.get()), DAMAGE * (this.getPower(owner) * 0.75F));

              

                  if (target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                      ISorcererData capHit = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                      if (!capHit.isChanneling(JJKAbilities.CURSED_ENERGY_SHIELD.get())) {
                        JJKEffectUtil.addEffect(target, new MobEffectInstance(JJKEffects.STUN.get(), STUN, 1, false, false, false));
                        JJKEffectUtil.addEffect(target, new MobEffectInstance(JJKEffects.STAGGER.get(), STUN, 1, false, false, false));
                      }
                    }
                else {
                        JJKEffectUtil.addEffect(target, new MobEffectInstance(JJKEffects.STUN.get(), STUN, 1, false, false, false));
                        JJKEffectUtil.addEffect(target, new MobEffectInstance(JJKEffects.STAGGER.get(), STUN, 1, false, false, false));
                }
                    

                // if (!capHit.isChanneling(JJKAbilities.CURSED_ENERGY_SHIELD.get())) {
                //     target.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), STUN, 1, false, false, false));
                //     target.addEffect(new MobEffectInstance(JJKEffects.STAGGER.get(), STUN, 1, false, false, false));
                // }

                float scale = 1.5F;

                double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).x;
                double y = owner.getY() + HelperMethods.RANDOM.nextDouble() + 0.5D * owner.getBbHeight();
                double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).z;

                level.sendParticles(ParticleTypes.SONIC_BOOM, x, y, z, 0, 0D, 0.0D, 0.0D, 1.0D);

                owner.level().playSound(null, target.getX(), target.getY(), target.getZ(), JJKSounds.SLASH.get(), SoundSource.MASTER,
                        1.0F, 1.0F);
            }
        }
    }


    @Override
    public Status isTriggerable(LivingEntity owner) {
       // BlockHitResult hit = this.getBlockHit(owner);

        //if (hit == null) {
            //return Status.FAILURE;
      //  }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 10.0F;
    }

    @Override
    public int getCooldown() {
        return 12 * 20;
    }

    @Override
    public int getDuration() {
        return 5;
    }

    @Override
    public Classification getClassification() {
        return Classification.SLASHING;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

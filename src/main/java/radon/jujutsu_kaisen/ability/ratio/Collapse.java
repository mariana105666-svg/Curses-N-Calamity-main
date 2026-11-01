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
import radon.jujutsu_kaisen.ExplosionHandler;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.client.particle.EmittingLightningParticle;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import net.minecraft.core.particles.ParticleTypes;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.particle.TravelParticle;
import radon.jujutsu_kaisen.util.ParticleUtil;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Collapse extends Ability implements Ability.IChannelened, Ability.IDurationable {
    private static final int RANGE = 10;
    private static final int DELAY = 20;
    private static final float DAMAGE = 10.0F;
    private static final int DURATION = 25;
    private static final float RADIUS = 3.0F;
    private static final float EXPLOSIVE_POWER = 3.0F;
    private static final float MAX_EXPLOSIVE_POWER = 10.0F;

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

            float scale = 1.5F;

       // Minecraft mc = Minecraft.getInstance();

        //if (mc.player == null) return;

        for (int i = 0; i < 12 * scale; i++) {

            double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).x;
            double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * owner.getBbHeight();
            double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.5F * scale) - owner.getLookAngle().scale(0.35D).z;
            double speed = (owner.getBbHeight() * 0.3F) * HelperMethods.RANDOM.nextDouble();

            level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.FALLING_BLOSSOM_EMOTION, owner.getBbWidth() * 0.5F,
                    0.2F, 16), x, y, z, 0, 0.0D, speed, 0.0D, 1.0D);
        }


            if (index == 12) {
                        if (owner instanceof ServerPlayer player) {
                            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.COLLAPSE.get(), SoundSource.MASTER, 1.0F, 1.0F);
                    //owner.level().sendParticles(ParticleTypes.SONIC_BOOM, pos.X, pos.Y, pos.Z, 0, 0D, 0.0D, 0.0D, 1.0D);
                }
            }
        }


    @Override
    public void onStop(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;

        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        if (result.getType() == HitResult.Type.BLOCK) {
            int index = this.getCharge(owner);
            owner.swing(InteractionHand.MAIN_HAND);

            // for (int i = 0; i < 12; i++) {
            //     level.sendParticles(new EmittingLightningParticle.EmittingLightningParticleOptions(ParticleColors.getCursedEnergyColorBright(owner), RADIUS * 2.0F, 1),
            //             owner.getX(), owner.getY() + (owner.getBbHeight() / 2.0F), owner.getZ(), 0, 0.0D, 0.0D, 0.0D, 0.0D);
            // }
            Vec3 center = owner.position().add(0.0D, owner.getBbHeight() / 2, 0.0D);
            for (int i = 0; i < 12; i++) {
                double theta = HelperMethods.RANDOM.nextDouble() * Math.PI * 2;
                double phi = HelperMethods.RANDOM.nextDouble() * Math.PI;

                Vec3 direction = new Vec3(Math.sin(phi) * Math.cos(theta), Math.sin(phi) * Math.sin(theta), Math.cos(phi));
                Vec3 offset = center.add(direction.multiply(owner.getBbWidth() / 2.0F, owner.getBbHeight() / 2, owner.getBbWidth() / 2.0F));

                level.sendParticles(new EmittingLightningParticle.EmittingLightningParticleOptions(ParticleColors.getCursedEnergyColorBright(owner),
                                direction, RADIUS * 2.0F, 1, true), offset.x, offset.y, offset.z, 0,
                        0.0D, 0.0D, 0.0D, 0.0D);
            }

            Vec3 realpos = result.getLocation();

            if (index >= 17 && index < DURATION) {
                ExplosionHandler.spawn(owner.level().dimension(), realpos, Math.min(MAX_EXPLOSIVE_POWER * 1.1F, ((EXPLOSIVE_POWER) * (this.getPower(owner))) * 1.1F),
                        20, DAMAGE + (this.getPower(owner) * 0.1F), owner, JJKDamageSources.indirectJujutsuAttack(owner, owner, JJKAbilities.COLLAPSE.get()), false);

                BlockHitResult hit = this.getBlockHit(owner, RANGE);
                BlockPos blocked = hit.getBlockPos();

                AABB bounds = new AABB(blocked.getX() - 1.0D, blocked.getY() - 1.0D, blocked.getZ() - 1.0D,
                        blocked.getX() + 1.0D, blocked.getY() + 1.0D, blocked.getZ() + 1.0D);

                double centerX = bounds.getCenter().x;
                double centerY = bounds.getCenter().y;
                double centerZ = bounds.getCenter().z;

                for (int x = (int) bounds.minX; x <= bounds.maxX; x++) {
                    for (int y = (int) bounds.minY; y <= bounds.maxY; y++) {
                        for (int z = (int) bounds.minZ; z <= bounds.maxZ; z++) {
                            BlockPos blocker = new BlockPos(x, y, z);
                            BlockState state = owner.level().getBlockState(blocker);

                            double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) + Math.pow(z - centerZ, 2));

                            if (distance <= RADIUS) {
                             if (HelperMethods.isDestroyable(owner.level(), owner, blocker)) {
                                 if (owner.level().destroyBlock(blocker, false)) {
                                     FallingBlockEntity entity = FallingBlockEntity.fall(owner.level(), blocker, state);
                                     entity.noPhysics = true;

                                 }
                             }
                            }
                        }
                    }
                }

            } else if (index < DURATION || index >= DURATION) {
                ExplosionHandler.spawn(owner.level().dimension(), realpos, Math.min(MAX_EXPLOSIVE_POWER * 0.75F, ((EXPLOSIVE_POWER) * (this.getPower(owner))) * 0.75F),
                        20, DAMAGE + (this.getPower(owner) * 0.1f), owner, JJKDamageSources.indirectJujutsuAttack(owner, owner, JJKAbilities.COLLAPSE.get()), false);
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
        return 13.0F;
    }

    @Override
    public int getCooldown() {
        return 14 * 20;
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

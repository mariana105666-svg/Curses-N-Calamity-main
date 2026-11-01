package radon.jujutsu_kaisen.ability.disaster_plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.entity.effect.ForestSpikeEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class ForestSpikes extends Ability {
    private static final double RANGE = 30.0D;
    private static final float SPREAD = 25.0F;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return HelperMethods.RANDOM.nextInt(3) == 0 && target != null && owner.hasLineOfSight(target);
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private @Nullable BlockHitResult getBlockHit(LivingEntity owner) {
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
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        BlockHitResult hit = this.getBlockHit(owner);

        if (hit == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    private void spawnSpike(LivingEntity owner, Direction dir, BlockPos pos, double offset1, double offset2) {
        ForestSpikeEntity spike = new ForestSpikeEntity(owner, this.getPower(owner));


            Vec3 spawnPos;

            switch (dir.getAxis()) {
                case Y -> spawnPos = pos.getCenter().add(offset1, 0.0D, offset2);
                case X -> spawnPos = pos.getCenter().add(0.0D, offset1, offset2);
                case Z -> spawnPos = pos.getCenter().add(offset1, offset2, 0.0D);
                default -> spawnPos = pos.getCenter();
            }

            float yRot = dir.toYRot() + (HelperMethods.RANDOM.nextFloat() - 0.5F) * 60.0F;
            float xRot = (float) (Mth.atan2(dir.getStepY(), dir.getStepX()) * 180.0F / Mth.PI)
                        + (HelperMethods.RANDOM.nextFloat() - 0.5F) * 60.0F;
            if (dir == Direction.UP || dir == Direction.DOWN) xRot = -xRot;
            if (dir == Direction.WEST) xRot -= 180.0F;
            spike.setYRot(yRot);
            spike.setXRot(xRot);

            Vec3 rayDir = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ()).scale(-1.0D);
            Vec3 rayStart = spawnPos;
            Vec3 rayEnd = spawnPos.add(rayDir.scale(5.0D));
            BlockHitResult hitResult = owner.level().clip(new ClipContext(
                rayStart,
                rayEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                spike
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hitResult.getBlockPos();

                BlockPos airCheck = blockPos.relative(dir);
                BlockState state = owner.level().getBlockState(airCheck);
                if (!state.getCollisionShape(owner.level(), airCheck).isEmpty()) {
                    spike.discard();
                    return;
                }

                // if (!owner.level().getBlockState(airCheck).isAir()) {
                    
                //     spike.discard();
                //     return;
                // }

                double blockCenterX = blockPos.getX() + 0.5D;
        double blockCenterY = blockPos.getY() + 0.5D;
        double blockCenterZ = blockPos.getZ() + 0.5D;

        double extraOffset = -0.25F;
        double pushDistance = 0.5D + spike.getBbHeight() / 2.0F + extraOffset;

        double finalX = blockCenterX + dir.getStepX() * pushDistance;
        double finalY = blockCenterY + dir.getStepY() * pushDistance;
        double finalZ = blockCenterZ + dir.getStepZ() * pushDistance;

                // 
                // double finalX = blockPos.getX() + (dir.getStepX() != 0 ? dir.getStepX() * spike.getBbHeight() / 2.0F + extraOffset : 0.0);
                // double finalY = blockPos.getY() + (dir.getStepY() != 0 ? dir.getStepY() * spike.getBbHeight() / 2.0F + extraOffset : 0.0);
                // double finalZ = blockPos.getZ() + (dir.getStepZ() != 0 ? dir.getStepZ() * spike.getBbHeight() / 2.0F + extraOffset : 0.0);

                spike.moveTo(finalX, finalY, finalZ, yRot, xRot);
                owner.level().addFreshEntity(spike);
            } else {
                spike.discard();
            }
        
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        BlockHitResult hit = this.getBlockHit(owner);

        if (hit != null) {
            owner.level().playSound(null, hit.getBlockPos(), JJKSounds.FOREST_SPIKES.get(), SoundSource.MASTER, 1.0F, 1.0F);

            Direction dir = hit.getDirection();
            BlockPos pos = hit.getBlockPos();
            
            double offset1 = 0.0;
            double offset2 = 0.0;

            spawnSpike(owner, dir, pos,offset1,offset2);
            for (int i = 0; i < 63; i++) {
                
                offset1 = (HelperMethods.RANDOM.nextDouble() - 0.5D) * SPREAD;
                offset2 = (HelperMethods.RANDOM.nextDouble() - 0.5D) * SPREAD;
                spawnSpike(owner, dir, pos,offset1,offset2);
            }
    }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 150.0F;
    }

    @Override
    public int getCooldown() {
        return 15 * 20;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public Classification getClassification() {
        return Classification.PLANTS;
    }
}

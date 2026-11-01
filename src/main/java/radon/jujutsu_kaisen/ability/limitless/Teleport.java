package radon.jujutsu_kaisen.ability.limitless;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Teleport extends Ability {
    private static final double RANGE = 40.0D;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return target != null && !target.isDeadOrDying() ;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private Vec3 getTarget(LivingEntity owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        // if (  result.getType() != HitResult.Type.MISS && ( !HelperMethods.wouldBeFilled(owner, result.getLocation()) ) ) {
        //     return null;
        // }
        // else if (result.getType() == HitResult.Type.MISS && (!HelperMethods.wouldBeFilled(owner, end)  )  ) { 
        //     return null;
        // }
        return result.getType() == HitResult.Type.MISS ? end : result.getLocation();
    }

    @Override
    public void run(LivingEntity owner) {
        Vec3 target = this.getTarget(owner);

        if (target != null) {
                BlockPos pos = new BlockPos(
                (int) Math.floor(target.x),
                (int) Math.floor(target.y),
                (int) Math.floor(target.z)
            );
                if (VeilHandler.isTeleportValid(owner.level(), pos )) {
            owner.swing(InteractionHand.MAIN_HAND);

            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1.0F, 1.0F);

            owner.setPos(target.x, target.y, target.z);

            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 100.0F;
    }

    @Override
    public int getCooldown() {
        return 4 * 20;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {

        return super.isTriggerable(owner);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

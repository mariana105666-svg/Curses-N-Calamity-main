package radon.jujutsu_kaisen.ability.disaster_flames;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.entity.effect.VolcanoEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Volcano extends Ability implements Ability.IChannelened, Ability.IDurationable {
    private static final double RANGE = 30.0D;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (JJKAbilities.isChanneling(owner, this)) {
            return HelperMethods.RANDOM.nextInt(5) != 0;
        }
        return HelperMethods.RANDOM.nextInt(5) == 0 && target != null && owner.hasLineOfSight(target);
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
        owner.swing(InteractionHand.MAIN_HAND);

        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hitter = this.getBlockHit(owner, 10.0D);

            if (hitter != null) {
                Direction dir = hitter.getDirection();
                VolcanoEntity volcano = new VolcanoEntity(owner, this.getPower(owner), hitter.getBlockPos(), dir);
                owner.level().addFreshEntity(volcano);
            }
        }
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 8.0F;
    }

    @Override
    public int getDuration() {
        return 2;
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
        return Classification.FIRE;
    }
}

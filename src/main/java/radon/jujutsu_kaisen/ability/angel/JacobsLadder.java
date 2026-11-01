package radon.jujutsu_kaisen.ability.angel;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.entity.effect.JacobsLadderEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class JacobsLadder extends Ability {
    public static final double RANGE = 50.0D;
    public LivingEntity enemy = null;
    public BlockHitResult block = null;
    @Override
    public boolean isScalable(LivingEntity owner) {
        return true;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return HelperMethods.RANDOM.nextInt(3) == 0 && target != null && this.getTarget(owner) == target;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private @Nullable LivingEntity getTarget(LivingEntity owner) {
        LivingEntity target = RotationUtil.getExpandedLookAt(owner,RANGE);
        if (target != null) {
            if (!owner.canAttack(target)) return null;
            return target;
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        /*Level level = owner.level();
        if (level instanceof ClientLevel) {
            return;
        }*/
        owner.swing(InteractionHand.MAIN_HAND);

        LivingEntity target = this.enemy;
        Vec3 pos;
        if (target != null) {
            pos = target.position();
        } else {
            Vec3 topCenter = Vec3.atCenterOf(this.block.getBlockPos()).add(0, 0.5, 0);
            pos = topCenter;
        }
        
         float output = ChantHandler.getOutput(owner, this);

        JacobsLadderEntity strike = new JacobsLadderEntity(owner, this.getPower(owner), pos, output);
        owner.level().addFreshEntity(strike);
        this.enemy = null;
        this.block = null;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (owner.isShiftKeyDown()) {
            BlockHitResult hit = this.getBlockHit(owner, RANGE);
            if (hit != null) {
                this.block = hit;
                return super.isTriggerable(owner);
            }
        }
        LivingEntity target = this.getTarget(owner);
        this.enemy = target;

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 50.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

package radon.jujutsu_kaisen.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.entity.PuppetEntity;
import radon.jujutsu_kaisen.puppet.PuppetMining;

import java.util.EnumSet;

public class PuppetAttackGoal extends Goal {
    private static final int ATTACK_COOLDOWN = 20;

    private static final int PATH_INTERVAL = 10;

    private final PuppetEntity puppet;
    private final double speed;

    private int cooldown;

    private int repath;

    private int swing;

    public PuppetAttackGoal(PuppetEntity puppet, double speed) {
        this.puppet = puppet;
        this.speed = speed;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Nullable
    private LivingEntity target() {
        LivingEntity target = this.puppet.getTarget();

        return target != null && target.isAlive() ? target : null;
    }

    @Override
    public boolean canUse() {
        return !this.puppet.isWorking() && this.target() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repath = 0;
        this.swing = 0;
    }

    @Override
    public void stop() {
        this.puppet.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.target();

        if (target == null) return;

        this.puppet.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double reach = this.puppet.getBbWidth() * 2.0F * this.puppet.getBbWidth() * 2.0F + target.getBbWidth();

        if (this.cooldown > 0) this.cooldown--;
        if (this.repath > 0) this.repath--;
        if (this.swing > 0) this.swing--;

        if (this.puppet.distanceToSqr(target.getX(), target.getY(), target.getZ()) <= reach) {
            this.puppet.getNavigation().stop();

            if (this.cooldown > 0) return;

            this.cooldown = ATTACK_COOLDOWN;

            this.puppet.swing(InteractionHand.MAIN_HAND);
            this.puppet.doHurtTarget(target);
            return;
        }

        if (!this.puppet.getNavigation().isDone()) return;

        if (this.repath <= 0) {
            this.repath = PATH_INTERVAL;

            Path path = this.puppet.getNavigation().createPath(target, 0);

            if (path != null && !path.isDone()) {
                this.puppet.getNavigation().moveTo(path, this.speed);
                return;
            }
        }

        this.breach(target);
    }

    private void breach(LivingEntity target) {
        if (!(this.puppet.level() instanceof ServerLevel level)) return;

        ServerPlayer owner = this.puppet.getOwner() instanceof ServerPlayer player ? player : null;

        BlockPos at = this.puppet.blockPosition();

        int dx = Mth.floor(target.getX()) - at.getX();
        int dz = Mth.floor(target.getZ()) - at.getZ();

        Direction dir;

        if (dx == 0 && dz == 0) {
            dir = this.puppet.getDirection();
        } else if (Math.abs(dx) >= Math.abs(dz)) {
            dir = dx >= 0 ? Direction.EAST : Direction.WEST;
        } else {
            dir = dz >= 0 ? Direction.SOUTH : Direction.NORTH;
        }

        BlockPos front = at.relative(dir);

        BlockPos lower = target.getY() > this.puppet.getY() + 1.0D ? front.above() : front;

        if (!this.mine(level, owner, lower)) this.mine(level, owner, lower.above());
    }

    private boolean mine(ServerLevel level, @Nullable ServerPlayer owner, BlockPos pos) {
        if (!PuppetMining.canMine(level, owner, pos)) return false;

        Vec3 center = Vec3.atCenterOf(pos);

        this.puppet.getLookControl().setLookAt(center.x, center.y, center.z);

        if (this.swing <= 0) {
            this.swing = 4;
            this.puppet.swing(InteractionHand.MAIN_HAND);
        }

        BlockState state = level.getBlockState(pos);

        float delta = PuppetMining.speed(state, level, pos);

        if (delta <= 0.0F) return false;

        float total = this.puppet.addBreakProgress(pos, delta * PuppetMining.MINE_SPEED);

        level.destroyBlockProgress(this.puppet.getId(), pos, (int) (total * 10.0F));

        if (total < 1.0F) return true;

        level.destroyBlockProgress(this.puppet.getId(), pos, -1);
        level.destroyBlock(pos, true, this.puppet);

        return true;
    }
}

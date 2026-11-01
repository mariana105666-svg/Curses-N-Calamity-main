package radon.jujutsu_kaisen.entity.idle_transfiguration.base;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.idle_transfiguration.IdleTransfiguration;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.control.MoveControl;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;

import radon.jujutsu_kaisen.util.SorcererUtil;
import radon.jujutsu_kaisen.entity.ai.goal.*;
import radon.jujutsu_kaisen.entity.base.ICommandable;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.base.SummonEntity;
import radon.jujutsu_kaisen.entity.sorcerer.SukunaEntity;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.RotationUtil;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public abstract class TransfiguredSoulEntity extends SummonEntity implements ISorcerer, ICommandable {
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private static final RawAnimation SWING = RawAnimation.begin().thenPlay("attack.swing");

    protected TransfiguredSoulEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public TransfiguredSoulEntity(EntityType<? extends TamableAnimal> pType, LivingEntity owner) {
        super(pType, owner.level());

        this.setTame(true);
        this.setOwner(owner);

        Vec3 pos = owner.position()
                .subtract(RotationUtil.getTargetAdjustedLookAngle(owner).multiply(this.getBbWidth(), 0.0D, this.getBbWidth()));
        this.moveTo(pos.x, pos.y, pos.z, RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));

        this.yHeadRot = this.getYRot();
        this.yHeadRotO = this.yHeadRot;
    }

    // @Override
    // public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
    //     LivingEntity owner = this.getOwner();

    //     if (owner != null && pSource.getEntity() == owner && JJKAbilities.hasToggled(owner, JJKAbilities.IDLE_TRANSFIGURATION.get())) {
    //         IdleTransfiguration.retrieve(owner, this);
    //     }
    //     return super.hurt(pSource, pAmount);
    // }

    @Override
    protected void registerGoals() {
        int target = 1;
        int goal = 1;

        this.goalSelector.addGoal(goal++, new WaterWalkingFloatGoal(this));
        this.goalSelector.addGoal(goal++, new MeleeAttackGoal(this, 0.9D, true));
        this.goalSelector.addGoal(goal++, new SorcererGoal(this));
        this.goalSelector.addGoal(goal++, new BetterFollowOwnerGoal(this, 1.0D, 25.0F, 10.0F, false));
        this.goalSelector.addGoal(goal, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(target++, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(target, new OwnerHurtTargetGoal(this));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        LivingEntity target = this.getTarget();
        GroundPathNavigation navigation = new GroundPathNavigation(this, pLevel);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        LivingEntity owner = this.getOwner();
        if (owner != null &&  pPlayer == owner && JJKAbilities.hasToggled(owner, JJKAbilities.IDLE_TRANSFIGURATION.get())) {
            IdleTransfiguration.retrieve(owner, this);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    
    // @Override
    // public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
    //     LivingEntity owner = this.getOwner();

       
    //     return super.hurt(pSource, pAmount);
    // }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.getTarget() != null) {
            this.moveControl.setWantedPosition(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ(), 1.1f);
        }

        if (this.getTarget() != null && HelperMethods.RANDOM.nextInt(5) == 0) {
            this.moveControl.setWantedPosition(this.getTarget().getX() * -1.5F, this.getTarget().getY() * -1.1F, this.getTarget().getZ() * -1.1F, 1.4f);
        }

    }

    private PlayState walkRunPredicate(AnimationState<TransfiguredSoulEntity> animationState) {
        if (animationState.isMoving()) {
            return animationState.setAndContinue(this.isSprinting() ? RUN : WALK);
        }
        return PlayState.STOP;
    }

    private PlayState swingPredicate(AnimationState<TransfiguredSoulEntity> animationState) {
        if (this.swinging) {
            return animationState.setAndContinue(SWING);
        }
        animationState.getController().forceAnimationReset();
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "Walk/Run", this::walkRunPredicate));
        controllerRegistrar.add(new AnimationController<>(this, "Swing", this::swingPredicate));
    }

    @Override
    public boolean canChangeTarget() {
        return true;
    }

    @Override
    public void changeTarget(LivingEntity target) {
        this.setTarget(target);
    }

    @Override
    public boolean hasMeleeAttack() {
        return true;
    }

    @Override
    public boolean hasArms() {
        return true;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public boolean canChant() {
        return false;
    }

    @Override
    public float getExperience() {
        if (this.getOwner() == null) {
            return 0.0f;
        }
        ISorcererData ownerCap = this.getOwner().getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        float exp = ownerCap.getExperience();
        return exp * 0.4f;
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return null;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

    public enum Variant {
        ONE,
        TWO,
        THREE
    }
}

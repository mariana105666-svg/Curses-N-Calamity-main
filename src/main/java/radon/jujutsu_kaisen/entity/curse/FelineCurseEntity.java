package radon.jujutsu_kaisen.entity.curse;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.ai.goal.*;
import radon.jujutsu_kaisen.entity.curse.base.PackCursedSpirit;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class FelineCurseEntity extends PackCursedSpirit implements PlayerRideable {
    private static final EntityDataAccessor<Integer> DATA_LEAP = SynchedEntityData.defineId(FelineCurseEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private static final RawAnimation LEAP = RawAnimation.begin().thenPlay("attack.leap");

    private static final int LEAP_DURATION = 10;
    private static final int LEAP_TICK_INTERVAL = 2;

    private int leapTickCounter = 0;

    public FelineCurseEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public FelineCurseEntity(FelineCurseEntity leader) {
        this(JJKEntities.FELINE_CURSE.get(), leader.level());
        this.setLeader(leader);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (pPlayer == this.getOwner() && !this.isVehicle()) {
            if (pPlayer.startRiding(this)) {
                pPlayer.setYRot(this.getYRot());
                pPlayer.setXRot(this.getXRot());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity living ? living : null;
    }

    private Vec2 getRiddenRotation(LivingEntity pEntity) {
        return new Vec2(pEntity.getXRot() * 0.5F, pEntity.getYRot());
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player pPlayer) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5F;
    }

    @Override
    protected void tickRidden(@NotNull Player pPlayer, @NotNull Vec3 pTravelVector) {
        if (pPlayer.xxa != 0 || pPlayer.zza != 0) {
            Vec2 vec2 = this.getRiddenRotation(pPlayer);
            this.setRot(vec2.y, vec2.x);
            this.yRotO = this.yBodyRot = this.yHeadRot = this.yHeadRotO = this.getYRot();
        }
        super.tickRidden(pPlayer, pTravelVector);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 2 * 6.0D)
                .add(Attributes.ARMOR, 5.0D);
    }


    @Override
    protected @NotNull Vec3 getRiddenInput(@NotNull Player pPlayer, @NotNull Vec3 pTravelVector) {
        float f = pPlayer.xxa * 0.5F;
        float f1 = pPlayer.zza <= 0 ? pPlayer.zza * 0.25F : pPlayer.zza;
        return new Vec3(f, 0.0D, f1);
    }

    @Override
    public double getPassengersRidingOffset() { return this.getBbHeight(); }
    @Override
    public float getStepHeight() { return 1.0F; }

    @Override
    public int getMinCount() { return 1; }
    @Override
    public int getMaxCount() { return 4; }

    @Override
    protected PackCursedSpirit spawn() { return new FelineCurseEntity(this); }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LEAP, 0);
    }

    @Override
    protected void registerGoals() {
        int goal = 1;
        int target = 1;

        this.goalSelector.addGoal(goal++, new WaterWalkingFloatGoal(this));
        if (this.hasMeleeAttack()) this.goalSelector.addGoal(goal++, new MeleeAttackGoal(this, 0.9D, true));
        this.goalSelector.addGoal(goal++, new CustomLeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(goal++, new SorcererGoal(this));
        this.goalSelector.addGoal(goal++, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(target++, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(target++, new NearestAttackableTargetGoal<>(this, IronGolem.class, false, true));
        this.targetSelector.addGoal(target++, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false, true));

        if (this.targetsSorcerers()) this.targetSelector.addGoal(target++, new NearestAttackableSorcererGoal(this, true));
        if (this.targetsCurses()) this.targetSelector.addGoal(target++, new NearestAttackableCurseGoal(this, true));
    }

    @Override
    protected boolean isCustom() { return true; }
    @Override
    public boolean hasMeleeAttack() { return true; }
    @Override
    public boolean hasArms() { return false; }
    @Override
    public boolean canJump() { return false; }
    @Override
    public boolean canChant() { return false; }
    @Override
    public float getExperience() { return SorcererGrade.GRADE_3.getRequiredExperience(); }
    @Override
    public @Nullable CursedTechnique getTechnique() { return null; }

    private PlayState movementPredicate(AnimationState<FelineCurseEntity> state) {
        if (this.entityData.get(DATA_LEAP) > 0) return state.setAndContinue(LEAP);
        if (state.isMoving()) return state.setAndContinue(this.isSprinting() ? RUN : WALK);
        return state.setAndContinue(IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", this::movementPredicate));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (leapTickCounter++ >= LEAP_TICK_INTERVAL) {
            int leap = this.entityData.get(DATA_LEAP);
            if (leap > 0) this.entityData.set(DATA_LEAP, leap - 1);
            leapTickCounter = 0;
        }
    }


    private class CustomLeapAtTargetGoal extends LeapAtTargetGoal {
        public CustomLeapAtTargetGoal(Mob pMob, float pYd) { super(pMob, pYd); }
        @Override
        public void start() { 
            super.start(); 
            FelineCurseEntity.this.entityData.set(DATA_LEAP, LEAP_DURATION); 
        }
    }
}

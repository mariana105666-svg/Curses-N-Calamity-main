package radon.jujutsu_kaisen.entity.sorcerer.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.control.MoveControl;
import radon.jujutsu_kaisen.util.HelperMethods;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.ai.goal.*;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.util.SorcererUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;

public abstract class SorcererEntity extends PathfinderMob implements GeoEntity, ISorcerer {
    //private static final int RARITY = 6;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected SorcererEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);

        Arrays.fill(this.armorDropChances, 1.0F);
        Arrays.fill(this.handDropChances, 1.0F);
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        if (vehicle instanceof Boat || vehicle instanceof AbstractMinecart) {
            return false;
        }
        return super.startRiding(vehicle, force);
    }

    @Override
    public boolean canBeLeashed(@Nullable Player player) {
        return false; 
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
        return true;
    }

    protected abstract boolean isCustom();

    protected boolean canFly() { return false; }

    protected boolean targetsCurses() { return true; }
    protected boolean targetsSorcerers() { return false; }
    protected boolean targetsShikigami() { return false; }

    private void createGoals() {
        int target = 1;
        int goal = 1;

        this.goalSelector.addGoal(goal++, new WaterWalkingFloatGoal(this));

        if (this.hasMeleeAttack()) {
            this.goalSelector.addGoal(goal++, new MeleeAttackGoal(this, 0.75D, true));
        }
        this.goalSelector.addGoal(goal++, new SorcererGoal(this));
        this.goalSelector.addGoal(goal, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(target++, new HurtByTargetGoal(this));

        if (this.targetsSorcerers()) {
            this.targetSelector.addGoal(target++, new NearestAttackableSorcererGoal(this, true));
        }
        if (this.targetsCurses()) {
            this.targetSelector.addGoal(target, new NearestAttackableCurseGoal(this, true));
        }
         if (this.targetsShikigami()) {
            this.targetSelector.addGoal(target, new NearestAttackableShikigamiGoal(this, true));
        }
        
    }

    @Override
    public boolean isPersistenceRequired() {
        return this.getGrade().ordinal() > SorcererGrade.GRADE_1.ordinal();
    }

    @Override
    protected void actuallyHurt(@NotNull DamageSource pDamageSource, float pDamageAmount) {
        super.actuallyHurt(pDamageSource, pDamageAmount);

        if (pDamageSource.getEntity() instanceof LivingEntity attacker && this.canAttack(attacker) && attacker != this) {
            this.setTarget(attacker);
        }
    }

    private boolean isInVillage() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        HolderSet.Named<Structure> villages = serverLevel.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getTag(StructureTags.VILLAGE)
                .orElseThrow();

        for (Holder<Structure> holder : villages) {
            if (serverLevel.structureManager().getStructureAt(pos, holder.get()).isValid()) {
                return true;
            }
        }

        return false;
    }


    

    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor pLevel, @NotNull MobSpawnType pSpawnReason) {
        if (pSpawnReason == MobSpawnType.NATURAL || pSpawnReason == MobSpawnType.CHUNK_GENERATION) {
             if (ConfigHolder.SERVER.sorcererVillageSpawnRate.get() == 0) {
                    return false;
            }    
              if (this.random.nextInt(Math.max(1, Mth.floor((ConfigHolder.SERVER.sorcererVillageSpawnRate.get()  * ((SorcererGrade.GRADE_3.getRequiredExperience() + this.getExperience()) / SorcererGrade.SPECIAL_GRADE.getRequiredExperience() ) ) ))) != 0) return false;

            if (!this.isInVillage()) return false;
        }

        //if (this.getGrade().ordinal() >= SorcererGrade.GRADE_1.ordinal()) {
            if (!pLevel.getEntitiesOfClass(this.getClass(), AABB.ofSize(this.position(), 256.0D, 64.0D, 256.0D)).isEmpty())
                return false;
        //}

        if (!pLevel.getEntitiesOfClass(SorcererEntity.class, AABB.ofSize(this.position(), 32.0D, 8.0D, 32.0D)).isEmpty())
            return false;

        return super.checkSpawnRules(pLevel, pSpawnReason);
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
    public void aiStep() {
        this.updateSwingTime();

        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity passenger = this.getControllingPassenger();

        if (this.getTarget() != null ) {
            ISorcererData cap = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (!cap.hasBurnout()) {
                this.moveControl.setWantedPosition(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ(), 0.9f);
            }

            if (this.random.nextInt(5) == 0 || cap.hasBurnout() && this.random.nextInt(3) == 0) {
                this.moveControl.setWantedPosition(this.getTarget().getX() * -1.5F, this.getTarget().getY() * -1.1F, this.getTarget().getZ() * -1.1F, 1.4f);
            }
        }

        if (passenger != null) {
            this.setSprinting(new Vec3(passenger.xxa, passenger.yya, passenger.zza).lengthSqr() > 0.01D);
        } else {
            this.setSprinting(this.getDeltaMovement().lengthSqr() > 0.01D && this.moveControl.getSpeedModifier() > 1.0D);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 96.0D);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!this.isCustom()) this.createGoals();

        this.getCapability(SorcererDataHandler.INSTANCE).ifPresent(this::init);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

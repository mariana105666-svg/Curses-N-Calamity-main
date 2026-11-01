package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.effect.base.BeamEntity;
import radon.jujutsu_kaisen.util.RotationUtil;

public class GraniteBlastEntity extends BeamEntity {
    public static final double RANGE = 60.0D;

    private static final float MIN_WIDTH = 0.5F;
    private static final float MAX_WIDTH = 1.0F;

    private static final float EXPLOSION_POWER = 2.5F;

    private static final float DAMAGE = 12.0F;

    private static final EntityDataAccessor<Float> DATA_CHARGE =
            SynchedEntityData.defineId(GraniteBlastEntity.class, EntityDataSerializers.FLOAT);

    private boolean detonated;

    @Nullable
    private Vec3 assist;

    private long assistTick = -1L;

    public GraniteBlastEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public GraniteBlastEntity(LivingEntity owner, float power, float charge) {
        this(JJKEntities.GRANITE_BLAST.get(), owner, power, charge);
    }

    protected GraniteBlastEntity(EntityType<? extends Projectile> pType, LivingEntity owner, float power, float charge) {
        super(pType, owner, power);

        this.entityData.set(DATA_CHARGE, Mth.clamp(charge, 0.0F, 1.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_CHARGE, 0.0F);
    }

    public float getChargeRatio() {
        return this.entityData.get(DATA_CHARGE);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putFloat("charge", this.getChargeRatio());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        this.entityData.set(DATA_CHARGE, pCompound.getFloat("charge"));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || this.detonated || !this.shouldExplode()) return;

        if (this.getTime() <= this.getCharge()) return;

        this.detonated = true;

        this.level().explode(this, this.collidePosX, this.collidePosY, this.collidePosZ,
                this.getExplosionPower(), Level.ExplosionInteraction.MOB);
    }

    @Override
    public boolean canDeflect() {
        return false;
    }

    @org.jetbrains.annotations.Nullable
    public org.joml.Vector3f getBeamColor() {
        return null;
    }

    @Override
    public int getFrames() {
        return 3;
    }

    @Override
    public float getScale() {
        return MIN_WIDTH + (MAX_WIDTH - MIN_WIDTH) * this.getChargeRatio();
    }

    @Override
    protected double getRange() {
        return RANGE;
    }

    private static final double ASSIST_REACH = 2.5D;

    protected boolean hasAimAssist() {
        return true;
    }

    @Nullable
    private LivingEntity findAssistTarget(LivingEntity owner) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        double range = this.getRange();

        LivingEntity best = null;
        double closest = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class,
                owner.getBoundingBox().expandTowards(look.scale(range)).inflate(ASSIST_REACH))) {
            if (candidate == owner || !candidate.isAlive()) continue;
            if (!owner.canAttack(candidate)) continue;

            Vec3 centre = candidate.getBoundingBox().getCenter();
            Vec3 offset = centre.subtract(eye);

            double along = offset.dot(look);

            if (along <= 0.0D || along > range) continue;

            double across = offset.subtract(look.scale(along)).length();

            if (across > ASSIST_REACH + (candidate.getBbWidth() / 2.0D)) continue;
            if (across >= closest) continue;

            if (this.level().clip(new ClipContext(eye, centre, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, owner)).getType() != HitResult.Type.MISS) continue;

            best = candidate;
            closest = across;
        }
        return best;
    }

    @Nullable
    private Vec3 assistAim(LivingEntity owner) {
        if (!this.hasAimAssist()) return null;

        long now = this.level().getGameTime();

        if (this.assistTick != now) {
            this.assistTick = now;

            LivingEntity target = this.findAssistTarget(owner);

            this.assist = target == null ? null : target.getBoundingBox().getCenter().subtract(owner.getEyePosition());
        }
        return this.assist;
    }

    @Override
    protected float getAimYRot(LivingEntity owner) {
        Vec3 aim = this.assistAim(owner);

        if (aim == null) return super.getAimYRot(owner);

        return (float) (Mth.atan2(aim.z, aim.x) * (180.0D / Math.PI)) - 90.0F;
    }

    @Override
    protected float getAimXRot(LivingEntity owner) {
        Vec3 aim = this.assistAim(owner);

        if (aim == null) return super.getAimXRot(owner);

        return (float) (-(Mth.atan2(aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z)) * (180.0D / Math.PI)));
    }

    private static final float BURNOUT_SHARE = 0.5F;

    protected boolean isBurntOut() {
        if (!(this.getOwner() instanceof LivingEntity owner)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasBurnout();
    }

    protected float getBaseDamage() {
        return DAMAGE * (1.0F + this.getChargeRatio());
    }

    @Override
    protected float getDamage() {
        return this.getBaseDamage() * (this.isBurntOut() ? BURNOUT_SHARE : 1.0F);
    }

    public float getFlareLength() {
        return 2.5F;
    }

    public float getFlareStart() {
        return 0.15F;
    }

    public boolean shouldSpin() {
        return true;
    }

    public boolean shouldRenderStartFlare() {
        return true;
    }

    public boolean shouldRenderEndFlare() {
        return true;
    }

    protected boolean shouldExplode() {
        return true;
    }

    protected float getExplosionPower() {
        return EXPLOSION_POWER * (1.0F + this.getChargeRatio());
    }

    @Override
    public int getDuration() {
        return 6;
    }

    @Override
    public int getCharge() {
        return 10;
    }

    @Override
    protected @Nullable Ability getSource() {
        return JJKAbilities.GRANITE_BLAST.get();
    }

    @Override
    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        return new Vec3(owner.getX(), owner.getY() + owner.getBbHeight() - (this.getBbHeight() / 2.0F), owner.getZ())
                .add(RotationUtil.getTargetAdjustedLookAngle(owner));
    }
}

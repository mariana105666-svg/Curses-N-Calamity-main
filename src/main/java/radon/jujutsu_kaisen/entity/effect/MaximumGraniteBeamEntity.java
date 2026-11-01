package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.JJKEntities;

public class MaximumGraniteBeamEntity extends GraniteBlastEntity {
    public static final double RANGE = 80.0D;

    private static final float WIDTH = 4.0F;

    private static final float DAMAGE = 8.0F;

    private static final int DURATION = 10 * 20;

    private static final int WIND_UP = 20;

    private static final float TURN_SPEED = 0.375F;

    private static final int ROOTED = 7;

    private float aimYRot;
    private float aimXRot;

    private boolean aimed;
    private long aimTick = -1L;

    private static final EntityDataAccessor<Float> DATA_OUTPUT =
            SynchedEntityData.defineId(MaximumGraniteBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final float LOCKED_OUTPUT = 2.0F;

    public MaximumGraniteBeamEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public MaximumGraniteBeamEntity(LivingEntity owner, float power, float output) {
        super(JJKEntities.MAXIMUM_GRANITE_BEAM.get(), owner, power, 1.0F);

        this.entityData.set(DATA_OUTPUT, output);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_OUTPUT, 1.0F);
    }

    private float share() {
        return Mth.clamp(this.entityData.get(DATA_OUTPUT), 0.0F, LOCKED_OUTPUT);
    }

    @Override
    public float getScale() {
        return WIDTH * this.share();
    }

    @Override
    protected double getRange() {
        return RANGE;
    }

    @Override
    protected float getBaseDamage() {
        return DAMAGE * this.share();
    }

    @Override
    public int getDuration() {
        return DURATION;
    }

    @Override
    public int getCharge() {
        return WIND_UP;
    }

    @Override
    protected double getDestructionRadius() {
        return this.getScale();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putFloat("output", this.entityData.get(DATA_OUTPUT));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("output")) this.entityData.set(DATA_OUTPUT, pCompound.getFloat("output"));
    }

    @Override
    protected boolean shouldExplode() {
        return false;
    }

    @Override
    protected boolean hasAimAssist() {
        return false;
    }

    private void settleAim(LivingEntity owner) {
        long now = this.level().getGameTime();

        if (this.aimTick == now) return;

        this.aimTick = now;

        float wantedY = super.getAimYRot(owner);
        float wantedX = super.getAimXRot(owner);

        if (!this.aimed) {
            this.aimYRot = wantedY;
            this.aimXRot = wantedX;
            this.aimed = true;
            return;
        }

        if (this.share() >= LOCKED_OUTPUT) return;

        this.aimYRot += Mth.clamp(Mth.wrapDegrees(wantedY - this.aimYRot), -TURN_SPEED, TURN_SPEED);
        this.aimXRot = Mth.clamp(this.aimXRot + Mth.clamp(wantedX - this.aimXRot, -TURN_SPEED, TURN_SPEED),
                -90.0F, 90.0F);
    }

    @Override
    protected float getAimYRot(LivingEntity owner) {
        this.settleAim(owner);

        return this.aimYRot;
    }

    @Override
    protected float getAimXRot(LivingEntity owner) {
        this.settleAim(owner);

        return this.aimXRot;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.getOwner() instanceof LivingEntity owner) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, ROOTED, false, false, false));
        }
    }

    @Override
    protected @Nullable Ability getSource() {
        return JJKAbilities.MAXIMUM_GRANITE_BEAM.get();
    }
}

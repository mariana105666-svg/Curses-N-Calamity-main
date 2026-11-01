package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.JJKEntities;

import java.util.List;

public class GraniteBlastVolleyEntity extends GraniteBlastEntity {
    private static final float WIDTH = 0.5F;

    private static final float DAMAGE = 12.0F;

    private static final float EXPLOSION_POWER = 1.75F;

    private static final float DRIFT_DECAY = 0.72F;

    private float yawOffset;
    private float pitchOffset;

    private float yawDrift;
    private float pitchDrift;

    private Vec3 originOffset = Vec3.ZERO;

    private static final EntityDataAccessor<Float> DATA_RANGE =
            SynchedEntityData.defineId(GraniteBlastVolleyEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> DATA_COSMETIC =
            SynchedEntityData.defineId(GraniteBlastVolleyEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(GraniteBlastVolleyEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_HIDE_START =
            SynchedEntityData.defineId(GraniteBlastVolleyEntity.class, EntityDataSerializers.BOOLEAN);

    public GraniteBlastVolleyEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public GraniteBlastVolleyEntity(LivingEntity owner, float power, float yawOffset, float pitchOffset,
                                    float yawDrift, float pitchDrift, Vec3 originOffset,
                                    double range, boolean cosmetic, boolean hideStart) {
        super(JJKEntities.GRANITE_BLAST_VOLLEY.get(), owner, power, 0.0F);

        this.yawOffset = yawOffset;
        this.pitchOffset = pitchOffset;
        this.yawDrift = yawDrift;
        this.pitchDrift = pitchDrift;
        this.originOffset = originOffset;

        this.entityData.set(DATA_RANGE, (float) range);
        this.entityData.set(DATA_COSMETIC, cosmetic);
        this.entityData.set(DATA_HIDE_START, hideStart);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_RANGE, (float) RANGE);
        this.entityData.define(DATA_COSMETIC, false);
        this.entityData.define(DATA_HIDE_START, false);
        this.entityData.define(DATA_LIFETIME, -1);
    }

    public void setLifetime(int ticks) {
        this.entityData.set(DATA_LIFETIME, ticks);
    }

    @Override
    public boolean shouldSpin() {
        return this.entityData.get(DATA_LIFETIME) < 0;
    }

    @Override
    public int getCharge() {
        return this.entityData.get(DATA_LIFETIME) < 0 ? super.getCharge() : 0;
    }

    @Override
    public int getDuration() {
        int lifetime = this.entityData.get(DATA_LIFETIME);

        return lifetime < 0 ? super.getDuration() : lifetime;
    }

    private boolean isCosmetic() {
        return this.entityData.get(DATA_COSMETIC);
    }

    @Override
    public boolean shouldRenderStartFlare() {
        return !this.entityData.get(DATA_HIDE_START);
    }

    @Override
    public boolean shouldRenderEndFlare() {
        return !this.isCosmetic();
    }

    @Override
    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        return super.calculateSpawnPos(owner).add(this.originOffset);
    }

    @Override
    protected double getRange() {
        return this.entityData.get(DATA_RANGE);
    }

    @Override
    protected boolean isStill() {
        return true;
    }

    @Override
    protected boolean hasAimAssist() {
        return false;
    }

    @Override
    public float getFlareLength() {
        return 0.0F;
    }

    @Override
    protected boolean shouldSwing() {
        return false;
    }

    @Override
    public List<Entity> checkCollisions(Vec3 from, Vec3 to) {
        List<Entity> hits = super.checkCollisions(from, to);

        return this.isCosmetic() ? List.of() : hits;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        this.setYaw(this.getYaw() + this.yawOffset);
        this.setPitch(this.getPitch() + this.pitchOffset);

        this.aim();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            this.setYaw(this.getYaw() + this.yawDrift);
            this.setPitch(this.getPitch() + this.pitchDrift);

            this.yawDrift *= DRIFT_DECAY;
            this.pitchDrift *= DRIFT_DECAY;
        }

        this.aim();

        super.tick();
    }

    private void aim() {
        this.renderYaw = this.getYaw();
        this.renderPitch = this.getPitch();

        double range = this.getRange();

        this.endPosX = this.getX() + range * Math.cos(this.getYaw()) * Math.cos(this.getPitch());
        this.endPosZ = this.getZ() + range * Math.sin(this.getYaw()) * Math.cos(this.getPitch());
        this.endPosY = this.getY() + range * Math.sin(this.getPitch());
    }

    @Override
    public float getScale() {
        return WIDTH;
    }

    @Override
    protected float getBaseDamage() {
        return this.isCosmetic() ? 0.0F : DAMAGE;
    }

    @Override
    protected float getExplosionPower() {
        return EXPLOSION_POWER;
    }

    @Override
    protected boolean shouldExplode() {
        return !this.isCosmetic();
    }

    @Override
    protected @Nullable Ability getSource() {
        return JJKAbilities.GRANITE_BLAST_VOLLEY.get();
    }
}

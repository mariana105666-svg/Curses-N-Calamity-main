package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;

public class ForestWaveEntity extends JujutsuProjectile {
    private static final float DAMAGE = 17.0F;
    private static final int DURATION = 4 * 20;
    //private Vec3 scale = new Vec3(1.0D, 1.0D, 1.0D);
    // private static final EntityDataAccessor<Float> SCALE_X = SynchedEntityData.defineId(ForestWaveEntity.class, EntityDataSerializers.FLOAT);
    // private static final EntityDataAccessor<Float> SCALE_Y = SynchedEntityData.defineId(ForestWaveEntity.class, EntityDataSerializers.FLOAT);
    // private static final EntityDataAccessor<Float> SCALE_Z = SynchedEntityData.defineId(ForestWaveEntity.class, EntityDataSerializers.FLOAT);

    private boolean damage;

    public ForestWaveEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }
//, Vec3 scale
    public ForestWaveEntity(LivingEntity owner, float power) {
        super(JJKEntities.FOREST_WAVE.get(), owner.level(), owner, power);
        //this.scale = scale;
        //      this.entityData.set(SCALE_X, (float) scale.x);
        // this.entityData.set(SCALE_Y, (float) scale.y);
        // this.entityData.set(SCALE_Z, (float) scale.z);
    }
    
    // public Vec3 getScale() {
    //     return this.scale;
    // }

    

    // @Override
    // protected void defineSynchedData() {
    //     super.defineSynchedData();
    //     this.entityData.define(SCALE_X, 1.0F);
    //     this.entityData.define(SCALE_Y, 1.0F);
    //     this.entityData.define(SCALE_Z, 1.0F);
    // }

    // @Override
    // public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
    //     super.onSyncedDataUpdated(key);
    //     if (SCALE_X.equals(key) || SCALE_Y.equals(key) || SCALE_Z.equals(key)) {
    //         this.scale = new Vec3(
    //             this.entityData.get(SCALE_X),
    //             this.entityData.get(SCALE_Y),
    //             this.entityData.get(SCALE_Z)
    //         );
    //     }
    // }

    @Override
    public void push(@NotNull Entity pEntity) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    public void setDamage(boolean damage) {
        this.damage = damage;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (this.level().isClientSide) return;

        if (!this.damage) return;

        if (!(this.getOwner() instanceof LivingEntity owner)) return;

        for (Entity entity : this.level().getEntities(this.getOwner(), this.getBoundingBox().inflate(1.0D))) {
            if (!entity.hurt(JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.FOREST_WAVE.get()), DAMAGE * this.getPower())) continue;
            entity.setDeltaMovement(this.position().subtract(entity.position()).normalize().reverse());
            entity.hurtMarked = true;
        }
    }

    @Override
    public void tick() {
        super.tick();

       if (this.getTime() >= DURATION) {
           this.discard();
       }
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putBoolean("damage", this.damage);
        // pCompound.putDouble("scaleX", this.scale.x);
        // pCompound.putDouble("scaleY", this.scale.y);
        // pCompound.putDouble("scaleZ", this.scale.z);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        this.damage = pCompound.getBoolean("damage");
        // double x = pCompound.getDouble("scaleX");
        // double y = pCompound.getDouble("scaleY");
        // double z = pCompound.getDouble("scaleZ");
        // this.scale = new Vec3(x, y, z);
    }
}

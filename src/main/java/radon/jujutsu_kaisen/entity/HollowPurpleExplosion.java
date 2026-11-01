package radon.jujutsu_kaisen.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ExplosionHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.sound.JJKSounds;
import net.minecraft.util.Mth;

public class HollowPurpleExplosion extends JujutsuProjectile {
    public static final int DURATION = 3 * 20;
    private static final float RADIUS = 8.0F;
    private static final float MAX_EXPLOSION = 30.0F;
    private boolean exploded;

    public HollowPurpleExplosion(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);

        this.noCulling = true;
        
    }

    public HollowPurpleExplosion(LivingEntity owner, float power, Vec3 pos) {
        this(JJKEntities.HOLLOW_PURPLE_EXPLOSION.get(), owner.level());

        this.setOwner(owner);
        this.setPower(power);

        float radius = RADIUS * this.getPower();
        this.setPos(pos);
        //this.setPos(pos.subtract(0.0D, 0.0D, 0.0D));
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        float radius = RADIUS * this.getPower();
        return EntityDimensions.fixed(0.1F, 0.1F);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean canDeflect() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        //this.refreshDimensions();
        if (this.tickCount == 1) this.refreshDimensions();
        if (this.level().isClientSide) return;

        if (this.getTime() >= DURATION) {
            this.discard();
            return;
        }

        if (this.getTime() - 1 == 0) {

            if (!(this.getOwner() instanceof LivingEntity owner)) return;
            
            this.playSound(JJKSounds.HOLLOW_PURPLE_EXPLOSION.get(), 3.0F, 1.0F);
    
            float radius = Math.min(MAX_EXPLOSION, RADIUS * this.getPower());
            int duration = (int) (radius / 5.0F * 20);
            /* float radFactor = Mth.clamp(owner.distanceTo(this)/radius,0.0F,1.0F);
            owner.hurt(JJKDamageSources.indirectJujutsuAttack(owner, owner, JJKAbilities.HOLLOW_PURPLE.get()), (this.getPower() * 5.0F)*radFactor); */
            
            ExplosionHandler.spawn(this.level().dimension(), this.position().add(0.0D, this.getBbHeight() / 2.0F, 0.0D), radius,
                    DURATION, this.getPower() * 0.4F, owner, JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.HOLLOW_PURPLE.get()), false);
            
            
        }
    }
}

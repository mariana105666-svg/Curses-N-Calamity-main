package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.util.EntityUtil;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.util.RotationUtil;

public class ForestSpikeEntity extends JujutsuProjectile {
    private static final int DURATION = 5 * 20;
    private static final int DELAY = 10;
    private static final float DAMAGE = 14.0F;

    public ForestSpikeEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public ForestSpikeEntity(LivingEntity owner, float power) {
        super(JJKEntities.FOREST_SPIKE.get(), owner.level(), owner, power);
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void tick() {
        super.tick();

        // if (this.getTime() <= 3) {
        //     this.refreshDimensions();
        // }

        if (this.getTime() >= DURATION) {
            this.discard();
        } else {
            if (!(this.getOwner() instanceof LivingEntity owner)) return;

            if (this.level().isClientSide) return;

            for (Entity entity : this.level().getEntities(null, this.getBoundingBox().expandTowards(RotationUtil.getTargetAdjustedLookAngle(this).scale(5.0D)))) {
    
                    ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                if (entity == owner && !cap.hasSelfHit()) continue;
                if (this.getTime() >= DELAY) {
                if (entity.hurt(JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.FOREST_SPIKES.get()), DAMAGE * this.getPower())) {
                    this.discard();
                }
                }
                
            }
        }
    }
}

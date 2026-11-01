package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.util.RotationUtil;

public class ChargeBlastEntity extends GraniteBlastEntity {
    public static final Vector3f COLOR = new Vector3f(1.0F, 0.58F, 0.12F);

    private static final float DAMAGE = 7.0F;

    private static final float EXPLOSION = 1.0F;

    private static final double CHEST = 0.8D;

    public ChargeBlastEntity(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public ChargeBlastEntity(LivingEntity owner, float power, float charge) {
        super(JJKEntities.CHARGE_BLAST.get(), owner, power, charge);
    }

    @Override
    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        double height = owner.getY() + owner.getBbHeight() * CHEST - (this.getBbHeight() / 2.0F);

        return new Vec3(owner.getX(), height, owner.getZ())
                .add(RotationUtil.getTargetAdjustedLookAngle(owner));
    }

    @Override
    protected float getBaseDamage() {
        return DAMAGE;
    }

    @Override
    protected float getExplosionPower() {
        return EXPLOSION;
    }

    @Override
    public @Nullable Vector3f getBeamColor() {
        return COLOR;
    }

    @Override
    protected @Nullable Ability getSource() {
        return JJKAbilities.CHARGE_BLAST.get();
    }
}

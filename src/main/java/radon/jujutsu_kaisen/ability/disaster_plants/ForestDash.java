package radon.jujutsu_kaisen.ability.disaster_plants;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.effect.ForestDashEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class ForestDash extends Ability implements Ability.IChannelened {
    private static final double SPEED = 2.5D;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return target != null && !target.isDeadOrDying()  && owner.distanceTo(target) >= 3.0D;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public void run(LivingEntity owner) {
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        Vec3 start = owner.position().subtract(owner.getUpVector(0.1F).scale(ForestDashEntity.SIZE));

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getEnergy() <= 6.0F) {
            return;
        }

        for (double i = 0.0D; i <= SPEED * 2; i += ForestDashEntity.SIZE) {
            Vec3 offset = start.add(look.scale(i));

            ForestDashEntity forest = new ForestDashEntity(owner);
            forest.moveTo(offset.x(), offset.y(), offset.z(), 180.0F - RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));
            owner.level().addFreshEntity(forest);
        }
        owner.setDeltaMovement(look.scale(SPEED));
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 6.0F;
    }

    @Override
    public int getCooldown() {
        return 2 * 20;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

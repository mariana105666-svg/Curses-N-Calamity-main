package radon.jujutsu_kaisen.ability.ten_shadows.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.ten_shadows.MaxElephantEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class MaxElephant extends Summon<MaxElephantEntity> {
    public MaxElephant() {
        super(MaxElephantEntity.class);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (!this.isTamed(owner)) return false;

        if (JJKAbilities.hasToggled(owner, this)) {
            return target != null && !target.isDeadOrDying() && HelperMethods.RANDOM.nextInt(1) == 0;
        }
        return target != null && !target.isDeadOrDying() && HelperMethods.RANDOM.nextInt(5) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return this.isTamed(owner) ? ActivationType.TOGGLED : ActivationType.INSTANT;
    }

    public float getCost(LivingEntity owner) {
        float normalcost = 0.75f;
        return this.isTamed(owner) ? normalcost : 250.0F;
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
    }

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.MAX_ELEPHANT.get());
    }

    @Override
    protected MaxElephantEntity summon(LivingEntity owner) {
        return new MaxElephantEntity(owner, this.isTamed(owner));
    }

    @Override
    public boolean canDie() {
        return true;
    }

    @Override
    public boolean isTenShadows() {
        return true;
    }

    @Override
    public boolean canDisable() {
        return false;
    }

    @Override
    protected boolean canTame() {
        return true;
    }


}

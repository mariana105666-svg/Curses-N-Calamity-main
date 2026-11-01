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
import radon.jujutsu_kaisen.entity.ten_shadows.AgitoEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class Agito extends Summon<AgitoEntity> {
    public Agito() {
        super(AgitoEntity.class);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;

        if (JJKAbilities.hasToggled(owner, this)) {
            return HelperMethods.RANDOM.nextInt(20) != 0;
        }
        return HelperMethods.RANDOM.nextInt(10) == 0;
    }

    @Override
    public boolean isTotality() {
        return true;
    }

    @Override
    public List<EntityType<?>> getFusions() {
        return List.of(JJKEntities.NUE.get(), JJKEntities.GREAT_SERPENT.get(), JJKEntities.TRANQUIL_DEER.get());
    }

    @Override
    public float getCost(LivingEntity owner) {
        float normalcost = 1.0f;
        return normalcost;
    }

    @Override
    public int getCooldown() {
        return 45 * 20;
    }

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.AGITO.get());
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
    protected AgitoEntity summon(LivingEntity owner) {
        return new AgitoEntity(owner);
    }
}

package radon.jujutsu_kaisen.ability.disaster_flames;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.entity.EmberInsectFlightEntity;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.List;

public class EmberInsectFlight extends Summon<EmberInsectFlightEntity> {
    public EmberInsectFlight() {
        super(EmberInsectFlightEntity.class);
    }

 @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
       

        if (JJKAbilities.hasToggled(owner,this)) {
            return owner.getBlockStateOn().getCollisionShape(owner.level(), owner.blockPosition()).isEmpty() && HelperMethods.RANDOM.nextInt(5) != 0;
        }
        return owner.fallDistance > 4.0F && !owner.isInFluidType();
    }

    @Override
    public void run(LivingEntity owner) {
        super.run(owner);

        owner.resetFallDistance();
    }

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.EMBER_INSECT_FLIGHT.get());
    }

    @Override
    public boolean isTenShadows() {
        return false;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    protected EmberInsectFlightEntity summon(LivingEntity owner) {
        return new EmberInsectFlightEntity(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.7F;
    }

    @Override
    public boolean display() {
        return false;
    }
}

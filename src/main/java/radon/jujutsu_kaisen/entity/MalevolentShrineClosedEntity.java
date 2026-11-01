package radon.jujutsu_kaisen.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MalevolentShrineClosedEntity extends ClosedDomainExpansionEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MalevolentShrineClosedEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public MalevolentShrineClosedEntity(LivingEntity owner, DomainExpansion ability, int radius) {
        super(JJKEntities.MALEVOLENT_SHRINE_CLOSED.get(), owner, ability, radius);
    }

    @Override
    public void registerControllers(AnimatableManager.@NotNull ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

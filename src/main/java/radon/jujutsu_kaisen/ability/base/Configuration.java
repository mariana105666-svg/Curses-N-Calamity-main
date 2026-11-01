package radon.jujutsu_kaisen.ability.base;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;


public abstract class Configuration extends Ability {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    // @Override
    // public boolean isUnlockable() {
    //     return this == JJKAbilities.DOMAIN_CONFIGURATION.get() || super.isUnlockable();
    // }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        return false;
    }

    protected boolean isParentUnlocked(LivingEntity owner) {
        if (owner == null) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;

        Ability parent = this.getParent(owner);

        return parent != null && cap.isUnlocked(parent);
    }

    @Override
    public int getPointsCost() {
        return 0;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return technique == null || technique.getDomain() == null ? null : technique.getDomain();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 0.0F);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }
}

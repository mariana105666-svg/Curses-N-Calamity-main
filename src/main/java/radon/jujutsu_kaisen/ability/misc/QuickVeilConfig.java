package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.AbilityDisplayInfo;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Configuration;

public class QuickVeilConfig extends Configuration {
    @Override
    public boolean isDisplayed(LivingEntity owner) {
        return this.isParentUnlocked(owner);
    }

    @Override
    public int getPointsCost() {
        return 50;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.VEIL.get();
    }

    @Override
    public AbilityDisplayInfo getDisplay(LivingEntity owner) {
        Vec2 coordinates = this.getDisplayCoordinates();

        return new AbilityDisplayInfo("quick_veil", coordinates.x, coordinates.y);
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 1.0F);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }
}

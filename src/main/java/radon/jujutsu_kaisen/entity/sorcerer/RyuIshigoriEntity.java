package radon.jujutsu_kaisen.entity.sorcerer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;

public class RyuIshigoriEntity extends SorcererEntity {
    private static final float EXPERIENCE = 15000.0F;

    private static final float HEALTH = 160.0F;

    public RyuIshigoriEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public float getExperience() {
        return EXPERIENCE;
    }

    @Override
    public float getFixedMaxHealth() {
        return HEALTH;
    }

    @Override
    public boolean canUseReverse() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RyuIshigoriEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 140.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 16.0D)
                .add(Attributes.ARMOR, 22.0D);
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.CURSED_ENERGY_DISCHARGE;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

    @Override
    public List<Ability> getUnlocked() {
        return List.of(JJKAbilities.SWEET_BURNING_MATSURI.get(), JJKAbilities.SIMPLE_DOMAIN.get(),
                JJKAbilities.QUICKDASH.get());
    }

    @Override
    public @NotNull List<Trait> getTraits() {
        return List.of(Trait.INCARNATED);
    }
}

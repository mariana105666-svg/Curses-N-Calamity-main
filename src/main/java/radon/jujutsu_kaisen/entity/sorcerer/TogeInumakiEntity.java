package radon.jujutsu_kaisen.entity.sorcerer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;

public class TogeInumakiEntity extends SorcererEntity {
    public TogeInumakiEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public float getExperience() {
        return SorcererGrade.SEMI_GRADE_1.getRequiredExperience();
    }
    public static AttributeSupplier.Builder createAttributes() {
        return TogeInumakiEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 140.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.ARMOR, 16.0D);
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.CURSED_SPEECH;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

}

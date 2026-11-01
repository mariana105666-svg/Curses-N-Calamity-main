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

public class NaoyaZeninEntity extends SorcererEntity {
    public NaoyaZeninEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public float getExperience() {
        return (SorcererGrade.GRADE_1.getRequiredExperience() + SorcererGrade.SPECIAL_GRADE.getRequiredExperience())/2.0F ;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return NaoyaZeninEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 140.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.ARMOR, 20.0D);
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.PROJECTION_SORCERY;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

}

package radon.jujutsu_kaisen.entity.idle_transfiguration;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import radon.jujutsu_kaisen.util.SorcererUtil;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.entity.idle_transfiguration.base.TransfiguredSoulEntity;

public class PolymorphicSoulIsomerEntity extends TransfiguredSoulEntity {
    public PolymorphicSoulIsomerEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }



    public PolymorphicSoulIsomerEntity(LivingEntity owner) {
        super(JJKEntities.POLYMORPHIC_SOUL_ISOMER.get(), owner);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 9.0F)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 4 * 3.5D);
    }

    @Override
    public Summon<?> getAbility() {
        return JJKAbilities.POLYMORPHIC_SOUl_ISOMER.get();
    }
}

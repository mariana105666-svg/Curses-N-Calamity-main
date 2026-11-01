package radon.jujutsu_kaisen.entity.idle_transfiguration;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import radon.jujutsu_kaisen.util.SorcererUtil;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.idle_transfiguration.base.TransfiguredSoulVariantEntity;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.entity.idle_transfiguration.base.TransfiguredSoulEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

public class TransfiguredSoulLargeEntity extends TransfiguredSoulVariantEntity {
    public TransfiguredSoulLargeEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public TransfiguredSoulLargeEntity(LivingEntity owner) {
        super(JJKEntities.TRANSFIGURED_SOUL_LARGE.get(), owner);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0F)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 3 * 2.5D);
    }

    @Override
    public Summon<?> getAbility() {
        return JJKAbilities.TRANSFIGURED_SOUL_LARGE.get();
    }
}

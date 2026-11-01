package radon.jujutsu_kaisen.entity.sorcerer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.item.JJKItems;

import java.util.ArrayList;
import java.util.List;

public class YutaOkkotsuEntity extends SorcererEntity {
    public YutaOkkotsuEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public float getExperience() {
        return SorcererGrade.SPECIAL_GRADE.getRequiredExperience() * 2.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return YutaOkkotsuEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 140.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 15.0D)
                .add(Attributes.ARMOR, 20.0D);
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.MIMICRY;
    }

    @Override
    public @NotNull List<Ability> getCustom() {
        return List.of(JJKAbilities.OUTPUT_RCT.get());
    }

    @Override
    public @NotNull List<Trait> getTraits() {
        return List.of(Trait.RCT_OUTPUT);
    }

    @Override
    public List<Ability> getUnlocked() {
        return List.of(JJKAbilities.SIMPLE_DOMAIN.get(), JJKAbilities.RCT1.get(),  JJKAbilities.RCT2.get(), JJKAbilities.RCT3.get(),
                JJKAbilities.OUTPUT_RCT.get(), JJKAbilities.DOMAIN_AMPLIFICATION.get());
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }


    @Override
    public void init(ISorcererData data) {
        super.init(data);

        data.copy(CursedTechnique.CURSED_SPEECH);
        data.setAdditionalEnergy(data.getMaxEnergy() * 0.5F);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JJKItems.GREEN_HANDLE_KATANA.get()));
    }
}

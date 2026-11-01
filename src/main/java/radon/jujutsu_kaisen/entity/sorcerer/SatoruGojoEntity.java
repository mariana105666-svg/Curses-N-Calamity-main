package radon.jujutsu_kaisen.entity.sorcerer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.SorcererUtil;

import java.util.List;

public class SatoruGojoEntity extends SorcererEntity {
    private final ItemStack blindfold;

    public SatoruGojoEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);

        this.blindfold = new ItemStack(JJKItems.BLINDFOLD.get());
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public float getExperience() {
        return SorcererGrade.SPECIAL_GRADE.getRequiredExperience() * 3.5F;
    }

     public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 140.0D);
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.LIMITLESS;
    }

    @Override
    public float getStepHeight() { return 5.0F; }

    @Override
    public @NotNull List<Trait> getTraits() {
        return List.of(Trait.SIX_EYES);
    }

    @Override
    public List<Ability> getUnlocked() {
        return List.of(JJKAbilities.SIMPLE_DOMAIN.get(), JJKAbilities.UNLIMITED_VOID.get(),
                JJKAbilities.ZERO_POINT_TWO_SECOND_DOMAIN_EXPANSION.get(), JJKAbilities.RCT1.get(),  JJKAbilities.RCT2.get(), JJKAbilities.RCT3.get(), JJKAbilities.QUICKDASH.get());
    }

    // @Override
    // public void init(ISorcererData data) {
    //     super.init(data);
    //     data.setAdditionalEnergy( (data.getMaxEnergy()) );
    // }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity target = this.getTarget();

        boolean wear = false;

        if (target == null || !target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
            wear = true;
        } else {
            ISorcererData cap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (SorcererUtil.getGrade(cap.getExperience()).ordinal() < SorcererGrade.GRADE_1.ordinal()) {
                wear = true;
            }
        }
        this.setItemSlot(EquipmentSlot.HEAD, wear ? this.blindfold : ItemStack.EMPTY);
    }
}

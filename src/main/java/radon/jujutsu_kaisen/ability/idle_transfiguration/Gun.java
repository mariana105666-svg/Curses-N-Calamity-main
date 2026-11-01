package radon.jujutsu_kaisen.ability.idle_transfiguration;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.IRMBAble;
import radon.jujutsu_kaisen.ability.base.Transformation;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.projectile.TransfiguredSoulProjectile;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class Gun extends Transformation implements IRMBAble {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean usesHands() {
        return true;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        // if (JJKAbilities.hasToggled(owner, this)) {
        //     return target != null && !target.isDeadOrDying() && HelperMethods.RANDOM.nextInt(20) != 0;
        // }
        return false;
       // return target != null && !target.isDeadOrDying() && HelperMethods.RANDOM.nextInt(5) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.2F;
    }

    @Override
    public boolean isReplacement() {
        return false;
    }

    @Override
    public Item getItem() {
        return JJKItems.GUN.get();
    }

    @Override
    public Part getBodyPart() {
        return Part.RIGHT_ARM;
    }

    @Override
    public boolean onRightClick(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getTransfiguredSouls() == 0) return false;

        cap.decreaseTransfiguredSouls();

        owner.swing(InteractionHand.MAIN_HAND);

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.SHOOT.get(), SoundSource.MASTER, 1.0F, 1.0F);

        TransfiguredSoulProjectile soul = new TransfiguredSoulProjectile(owner);
        owner.level().addFreshEntity(soul);
        return true;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }
    
    @Override
    public boolean isValid(LivingEntity owner) {
        return !JJKAbilities.hasToggled(owner, JJKAbilities.INSTANT_SPIRIT_BODY_OF_DISTORTED_KILLING.get()) && super.isValid(owner);
    }
    

    @Override
    public float getSlimTranslation() {
        return 0.03125F;
    }
}
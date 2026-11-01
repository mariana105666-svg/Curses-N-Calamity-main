package radon.jujutsu_kaisen.ability.idle_transfiguration;


import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Ability.ICharged;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.effect.BodyRepelEntity;
import radon.jujutsu_kaisen.util.HelperMethods;

public class BodyRepel extends Ability implements ICharged {
    private static final int MIN_SOULS = 2;
    private static final int MAX_SOULS = 5;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        if (!owner.hasLineOfSight(target)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();


        if (cap.isChanneling(this)) {
            return HelperMethods.RANDOM.nextInt(20) != 0;
        }
        return HelperMethods.RANDOM.nextInt(40) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    private int getSoulCost(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap == null) return 0;


        return Math.max(MIN_SOULS, Math.min(MAX_SOULS, Math.min(cap.getTransfiguredSouls(), 1 + (this.getCharge(owner) / 2))));
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;

        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.souls", JujutsuKaisen.MOD_ID),
                this.getSoulCost(owner)), false);
    }

    @Override
    public boolean isValid(LivingEntity owner) {

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();


        if (cap.getTransfiguredSouls() < MIN_SOULS) return false;

        return super.isValid(owner);
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        int souls = this.getSoulCost(owner);


        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap == null) return false;

        cap.useTransfiguredSouls(souls);

        owner.level().addFreshEntity(new BodyRepelEntity(owner, souls));

        return true;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 50.0F;
    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}
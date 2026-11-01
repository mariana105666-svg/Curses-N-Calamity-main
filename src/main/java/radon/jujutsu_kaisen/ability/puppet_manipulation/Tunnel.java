package radon.jujutsu_kaisen.ability.puppet_manipulation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.puppet.TunnelHandler;

public class Tunnel extends Ability implements Ability.IToggled {
    public static final double RANGE = 15.0D;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return true;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasPuppetRegister() && !cap.getPuppets().isEmpty();
    }

    @Override
    public void run(LivingEntity owner) {
    }

    @Override
    public void onEnabled(LivingEntity owner) {
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        if (owner instanceof ServerPlayer player) TunnelHandler.cancel(player);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }
}

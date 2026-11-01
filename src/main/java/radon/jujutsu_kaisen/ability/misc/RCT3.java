package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;


public class RCT3 extends RCT2 {
    private static final float BURNOUT_PER_TICK = 5.0F;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getBurnout() > 0) {
            return true;
        }
        return super.shouldTrigger(owner, target);
    }

    @Override
    public void run(LivingEntity owner) {
        super.run(owner);

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        int burnout = cap.getBurnout();

        if (burnout > 0 && (owner.isShiftKeyDown() || !(owner instanceof Player)  ) ) {
            float scale = 1.0F + ISorcererData.BURNOUT_SPEED_BONUS
                    * Mth.clamp(cap.getBurnoutSpeed(), 0.0F, ISorcererData.BURNOUT_STAT_MAX) / ISorcererData.BURNOUT_STAT_MAX;

            int charge = this.getCharge(owner);
            int drain = Mth.floor(BURNOUT_PER_TICK * scale * (charge + 1)) - Mth.floor(BURNOUT_PER_TICK * scale * charge);

            cap.setBurnout(Math.max(0, burnout - drain));

            if (charge % 20 == 0) {
                cap.increaseBrainDamage();
            }

            if (charge % ISorcererData.BURNOUT_STAT_INTERVAL == 0) {
                cap.addBurnoutSpeed(ISorcererData.BURNOUT_STAT_PER_AWARD);
                cap.addBrainRecovery(ISorcererData.BURNOUT_STAT_PER_AWARD);
            }
        }
    }

     @Override
    public boolean usesHands() {
        return false;
    }


    @Override
    public float healMult() {
        return 2.25f;
    }

    @Override
    public float getCost(LivingEntity owner) {
        float cost = super.getCost(owner);

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getBurnout() > 0 && owner.isShiftKeyDown()) {
            cost += 100.0F / 20;
        }
        return cost;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.RCT2.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(4.0F, 2.0F);
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.rct3Cost.get();
    }

    @Override
    protected float getMultiplier() {
        return 8.5f;
    }
}

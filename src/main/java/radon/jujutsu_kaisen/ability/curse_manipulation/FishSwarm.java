package radon.jujutsu_kaisen.ability.curse_manipulation;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.AbsorbedCurse;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.curse.FishCurseEntity;
import radon.jujutsu_kaisen.entity.curse.WormCurseEntity;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class FishSwarm extends Ability {

    public static final double RANGE = 80.0D;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        return HelperMethods.RANDOM.nextInt(40) == 0 && this.getTarget(owner) == target;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;


        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;

        return cap.hasCurse(JJKEntities.FISH_CURSE.get());
        // int amount = 0;

        // for (int i = 0; i < cap.getCurses().size(); i++) {
        //     if (cap.getCurses().get(i).getType() != JJKEntities.FISH_CURSE.get()) continue;
        //     amount++;
        // }
        //return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Nullable
    private LivingEntity getTarget(LivingEntity owner) {
        if (RotationUtil.getLookAtHit(owner, RANGE) instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            return target;
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        LivingEntity target = this.getTarget(owner);

        if (target == null) return;


        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap == null) return;

        for (int i = 0; i < 5; i++) {
            AbsorbedCurse curse = cap.getCurse(JJKEntities.FISH_CURSE.get());

            if (curse == null || curse.equals(null) || !(JJKAbilities.summonCurse(owner, curse, false) instanceof FishCurseEntity fish)) return;



            Vec3 start = owner.getEyePosition();
            fish.setPos(start.add(-4+i,4,1));
            Vec3 velocity = target.position().subtract(owner.position()).normalize().scale(2.5);
            fish.setDeltaMovement(velocity);
            fish.setTarget(target);
        }
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        LivingEntity target = this.getTarget(owner);

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap == null) return 0.0F;


        AbsorbedCurse curse = cap.getCurse(JJKEntities.FISH_CURSE.get());
        return curse == null ? 0.0F : JJKAbilities.getCurseCost(curse);
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

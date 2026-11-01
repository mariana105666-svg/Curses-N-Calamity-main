package radon.jujutsu_kaisen.ability.shrine;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.projectile.FireArrowProjectile;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import net.minecraft.world.phys.Vec2;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsMode;
import radon.jujutsu_kaisen.ability.JJKAbilities;

public class FireArrow extends Ability {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;       
        return owner.getHealth() / owner.getMaxHealth() < 0.9F && target.getHealth() / target.getMaxHealth() < 0.5F && owner.hasLineOfSight(target);
        //return target != null && HelperMethods.RANDOM.nextInt(4) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        FireArrowProjectile arrow = new FireArrowProjectile(owner, this.getPower(owner));
        owner.level().addFreshEntity(arrow);
    }

    @Override
    public int getPointsCost() {
        return 50;
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(0.0F, 3.0F);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 350.0F;
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return (technique == CursedTechnique.SHRINE || cap.hasStolen(CursedTechnique.SHRINE )  ) && super.isDisplayed(owner);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        return !(owner instanceof net.minecraft.world.entity.player.Player) || cap.hasNode(ISorcererData.FUGA_NODE);
    }

    @Override
    public Classification getClassification() {
        return Classification.FIRE;
    }
}

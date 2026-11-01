package radon.jujutsu_kaisen.ability.shrine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Ability.ActivationType;
import radon.jujutsu_kaisen.ability.base.Ability.Classification;
import radon.jujutsu_kaisen.ability.base.Ability.ICharged;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.entity.effect.SpiderwebEntity;
import radon.jujutsu_kaisen.entity.projectile.DismantleProjectile;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Spiderweb extends Ability implements ICharged {
    //private static final int RANGE = 9;
    public static final int DELAY = 20;
    public static final float EXPLOSIVE_POWER = 3.0F;
    public static final float MAX_EXPLOSIVE_POWER = 20.0F;
    public static final int MAX_CHARGE = 15;
    private static final int RANGE = 3;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;

        if (owner.level().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).get()) {
            return owner.getNavigation().isStuck();
        }
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

   private @Nullable BlockHitResult getBlockHit(LivingEntity owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        if (result.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) result;
        } else if (result.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) result).getEntity();
            Vec3 offset = entity.position().subtract(0.0D, 5.0D, 0.0D);
            return owner.level().clip(new ClipContext(entity.position(), offset, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);
        JJKEffectUtil.addEffect(owner, new MobEffectInstance(JJKEffects.STUN.get(), 2, 1, false, false, false));

        if (!(owner instanceof Player) || !owner.level().isClientSide) return;

        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(((float) Math.min(MAX_CHARGE, this.getCharge(owner)) / MAX_CHARGE) * 100)), false);
    }

    @Override
    public void onStart(LivingEntity owner) {
        BlockHitResult hit = this.getBlockHit(owner);

        if (hit == null) return;

        SpiderwebEntity spiderweb = new SpiderwebEntity(owner, this.getPower(owner),
                hit.getBlockPos(), hit.getDirection());

        owner.level().addFreshEntity(spiderweb);
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        BlockHitResult hit = this.getBlockHit(owner);

        if (hit == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 50.0F * ((float) Math.min(MAX_CHARGE, this.getCharge(owner)) / MAX_CHARGE);
    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

    @Override
    public Classification getClassification() {
        return Classification.SLASHING;
    }






   

    // @Override
    // public void run(LivingEntity owner) {
    //     if (owner.level().isClientSide) return;

    //     owner.swing(InteractionHand.MAIN_HAND, true);

    //     BlockHitResult hit = this.getBlockHit(owner);

    //     if (hit != null) {
    //         ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //         float radius = Math.min(MAX_EXPLOSIVE_POWER, EXPLOSIVE_POWER * this.getPower(owner));
    //         float real = (radius % 2 == 0) ? radius + 1 : radius;

    //         Vec3 center = hit.getBlockPos().getCenter().add(RotationUtil.getTargetAdjustedLookAngle(owner).scale(real * 0.01F));

    //         AABB bounds = AABB.ofSize(center, real, real, real);

    //         for (int i = 0; i < HelperMethods.RANDOM.nextInt(DELAY / 4, DELAY / 2); i++) {
    //             cap.delayTickEvent(() -> {
    //                 owner.level().playSound(null, center.x, center.y, center.z,
    //                         JJKSounds.SLASH.get(), SoundSource.MASTER, 1.0F, 1.0F);

    //                 BlockPos.betweenClosedStream(bounds).forEach(pos -> {
    //                     int bound = Math.max(1, Math.round(radius) * 2);
    //                     if (HelperMethods.RANDOM.nextInt(bound) == 0) {
    //                     //if (HelperMethods.RANDOM.nextInt(Math.round(radius) * 2) == 0) {
    //                         Vec3 current = pos.getCenter();
    //                         owner.level().addFreshEntity(new DismantleProjectile(owner, this.getPower(owner) * 1.35F,
    //                                 (HelperMethods.RANDOM.nextFloat() - 0.5F) * 360.0F, current, HelperMethods.RANDOM.nextInt(DismantleProjectile.MIN_LENGTH, DismantleProjectile.MAX_LENGTH + 1), true, true));
    //                     }
    //                 });
    //             }, i * 2);
    //         }
    //     }
    // }


    // @Override
    // public Status isTriggerable(LivingEntity owner) {
    //     BlockHitResult hit = this.getBlockHit(owner);

    //     if (hit == null) {
    //         return Status.FAILURE;
    //     }
    //     return super.isTriggerable(owner);
    // }

    // @Override
    // public float getCost(LivingEntity owner) {
    //     return 200.0F;
    // }

    // @Override
    // public int getCooldown() {
    //     return 15 * 20;
    // }

    // @Override
    // public Classification getClassification() {
    //     return Classification.SLASHING;
    // }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}

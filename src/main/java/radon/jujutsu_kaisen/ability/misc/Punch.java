package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RiderShieldingMount;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.Pact;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.item.cursed_tool.HitenStaffItem;
import radon.jujutsu_kaisen.item.cursed_tool.PlayfulCloudItem;
import radon.jujutsu_kaisen.item.cursed_tool.PolearmStaffItem;
import radon.jujutsu_kaisen.item.cursed_tool.SteelGauntletItem;
import radon.jujutsu_kaisen.item.cursed_tool.SlaughterDemonItem;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.ArrayList;
import java.util.List;

public class Punch extends Ability implements Ability.ICharged{
    private static final float DAMAGE = 7.5F;
    private static final double RANGE = 7.5D;
    private static final double LAUNCH_POWER = 3.0D;
    private static final int STAGGER = 0;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
         if (owner.level().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).get()) {
            if (owner.isInWall()) return true;
        }
        if (target == null || target.isDeadOrDying()) return false;
        if (owner.hasEffect(JJKEffects.STAGGER.get())) {
            return false;
        }
        if (owner.level().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).get()) {
            if (owner.getNavigation().isStuck() && !owner.isInFluidType()) return true;
            HitResult hit = RotationUtil.getLookAtHit(owner, 1.0D);

            if (hit.getType() == HitResult.Type.BLOCK) {
                if (owner.level().getBlockState(((BlockHitResult) hit).getBlockPos()).getBlock().defaultDestroyTime() > Block.INDESTRUCTIBLE) {
                    return true;
                }
            }
        }
        if (owner.distanceTo(target) > RANGE) return false;
        return HelperMethods.RANDOM.nextInt(1) == 0;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isMelee() {
        return true;
    }



    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;
        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(((float) Math.min(20, this.getCharge(owner)) / 20) * 100)), false);
    }

    @Override
    public Status isStillUsable(LivingEntity owner) {
        return super.isStillUsable(owner);
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STAGGER.get())) {
            return false;
        }

        owner.swing(InteractionHand.MAIN_HAND);
        if (!(owner.level() instanceof ServerLevel level)) return false;
        float power = (float) Math.min(20, this.getCharge(owner)) / 20;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        float num = 3;
        if (power >= 0.25) {
            num = 4;
        }

        double newRange = RANGE;
        int newStagger = STAGGER;

        int dash = cap.getDash();
        if (dash > 0) {
            num+=2;
            newRange+=1.5;
            Vec3 pos = owner.getEyePosition().add(look);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.MASTER, 1.0F, 1.5F);

        }

        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PolearmStaffItem) {
            newRange+=1.2;
        }

        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SlaughterDemonItem) {
            newStagger += 10.0;
        }


        if (cap.getSpeedStacks() > 0) {
            newRange = RANGE + ((double) cap.getSpeedStacks() / 2);
        }

        List<String> targets = new ArrayList<String>();
        Level level1 = owner.level();
          final boolean[] broke = {false};

        for (int i = 0; i < num; i++) {
            double finalNewRange = newRange;
            int finalStagger = newStagger;
            final int copyIndex = i;
          //final float copyNum = num;
            cap.delayTickEvent(() -> {
           
                Vec3 offset = owner.getEyePosition().add(look.scale(finalNewRange / 2-2));
      
                for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(offset, finalNewRange, finalNewRange+2, finalNewRange),
                        entity -> entity != owner )) {
                                broke[0] = true;
                                 System.out.println("hurt pig");
                    //&& owner.hasLineOfSight(entity)
                    boolean found = false;
                    for (String target: targets) {
                        if (target == entity.getStringUUID()) {
                            found = true;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    targets.add(entity.getStringUUID());
                    if (HelperMethods.friendsCheck(owner, entity)) continue;
                        
                    if (level1 instanceof ServerLevel) {
                        Vec3 center = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
                        entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 0.9F, 1.2F);

                        entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.MASTER, 1.5F, 1.3F);
                        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                            entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.MASTER, 1.5F, 0.8F);
                        }

                        ((ServerLevel) level1).sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                    }

                        int tim = 8;

                        if (power == 1.0F) {
                            if (!cap.hasToggled(JJKAbilities.RATIO_RULE.get()) && (!JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION))) {
                                cap.moreBlackFlash(true);
                            }

                            if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                                tim = 10;
                            }

                            cap.delayTickEvent(() -> {
                                cap.moreBlackFlash(false);
                            }, 3);
                        }

                        if (power >= 0.65 && power <= 0.75 && cap.hasToggled(JJKAbilities.RATIO_RULE.get())) {
                            int cooldown = cap.getRemainingCooldown(JJKAbilities.RATIO_RULE.get());
                            if (cooldown <= 0) {
                                cap.moreBlackFlash(true);
                                tim = 14;
                                if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                                    tim = 16;
                                }

                                cap.delayTickEvent(() -> {
                                    cap.moreBlackFlash(false);
                                }, 3);
                            }
                        }

                    float newDMG;
                    newDMG = DAMAGE;

                    if (!(owner instanceof Player player)) {
                        newDMG/=1.65F;
                    }
                    
                    if (owner instanceof Player player) {
                        player.attack(entity);
                    } else {
                        owner.doHurtTarget(entity);
                    }

                    entity.invulnerableTime = 0;

                    float newPower = (float) (LAUNCH_POWER*(0.8+0.4*power));
                    newDMG *= (float) (1+0.75 *power);
                    float totalDmg = (newDMG * this.getPower(owner));
                    if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION)) {
                        if (entity.hurt(owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), 1.25F*totalDmg)) {
                            entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F) * 1.5F)
                                    .multiply(1.0D, 0.25D, 1.0D));
                            JJKEffectUtil.addEffect(entity, new MobEffectInstance(JJKEffects.STUN.get(), tim, 0, false, false, false));
                            JJKEffectUtil.addEffect(entity, new MobEffectInstance(JJKEffects.STAGGER.get(), finalStagger, 0, false, false, false));
                        }
                    }

                    else {
                        if (entity.hurt(JJKDamageSources.jujutsuAttack(owner, this), totalDmg)) {
                            if (owner instanceof Player player) {
                                entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F))
                                        .multiply(1.0D, 0.25D, 1.0D));
                            }
                            else {
                                entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F))
                                        .multiply(2.0D, 0.5D, 2.0D));
                            }
                            JJKEffectUtil.addEffect(entity, new MobEffectInstance(JJKEffects.STUN.get(), tim, 0, false, false, false));
                            JJKEffectUtil.addEffect(entity, new MobEffectInstance(JJKEffects.STAGGER.get(), finalStagger, 0, false, false, false));
                        }
                    }

                    }
                
                if (power == 1.0F && copyIndex == 1 && broke[0] == false) {
                    broke[0] = true;
                    float newDMG;
                    newDMG = DAMAGE;
                    if (!(owner instanceof Player player)) {
                        newDMG/=1.65F;
                    }
                    newDMG *= (float) (1+0.75 *power);
                    float totalDMG = (newDMG * this.getPower(owner));
                    Vec3 end = owner.getEyePosition().add(look.scale(RANGE / 2));
                    AABB bounds = AABB.ofSize(end, 3.0D, 3.0D, 3.0D).inflate(1.0D);
                    BlockPos.betweenClosedStream(bounds).forEach(posSelf -> {
                        if (posSelf.getCenter().distanceTo(bounds.getCenter()) > RANGE) return;

                        BlockHitResult blockHit = owner.level().clip(new ClipContext(owner.getEyePosition(), posSelf.getCenter(),
                                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));

                    if (blockHit.getType() == HitResult.Type.BLOCK && !blockHit.getBlockPos().equals(posSelf)) return;
                    if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION)) {
                        if (!HelperMethods.isDestroyable(level, owner, posSelf, owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), 1.25F*totalDMG)) return;
                    }
                    else {
                        if (!HelperMethods.isDestroyable(level, owner, posSelf, JJKDamageSources.jujutsuAttack(owner, this), totalDMG)) return;
                    }
                        owner.level().destroyBlock(posSelf, true, owner);
                    });
                }
                }, i*1);
                
            }


        //if ((owner.level() instanceof ServerLevel level)) {
            if (dash > 0) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y+1, pos.z, 0, 0D, 0.0D, 0.0D, 1.0D);
            }
            for (int i = 0; i < 4; i++) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                Item item = owner.getItemInHand(InteractionHand.MAIN_HAND).getItem();
                level.sendParticles(JJKAbilities.hasToggled(owner, JJKAbilities.INSTANT_SPIRIT_BODY_OF_DISTORTED_KILLING.get()) || JJKAbilities.hasToggled(owner, JJKAbilities.ARM_BLADE.get()) || item instanceof SwordItem && !(item instanceof SteelGauntletItem) ? ParticleTypes.SWEEP_ATTACK : ParticleTypes.CLOUD,
                        pos.x + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.y + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.z + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        0, 0.0D, 0.0D, 0.0D, 1.0D);
            }
            for (int i = 0; i < 4; i++) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                level.sendParticles(ParticleTypes.CRIT,
                        pos.x + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.y + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.z + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        0, 0.0D, 0.0D, 0.0D, 1.0D);
            }

            Vec3 pos = owner.getEyePosition().add(look);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.MASTER, 1.0F, 1F+(HelperMethods.RANDOM.nextFloat() - 0.5f) * .2f);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.MASTER, 1.0F, 1.6F+(HelperMethods.RANDOM.nextFloat() - 0.5f) * .4f);
            
       // }



        return true;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        return owner.isUsingItem() ? Status.FAILURE : super.isTriggerable(owner);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.hasMeleeAttack() && sorcerer.hasArms()) && super.isValid(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION) ? 0.0F : 20.0F;
    }

    @Override
    public int getCooldown() {
        return 10;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }
}

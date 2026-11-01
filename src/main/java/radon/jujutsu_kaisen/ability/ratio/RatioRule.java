package radon.jujutsu_kaisen.ability.ratio;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.JJKParticles;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;

public class RatioRule extends Ability implements Ability.IAttack, Ability.IToggled {
    private static final float MAX_DAMAGE = 40.0F;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.addBlackFlash()) {
            return target != null && !target.isDeadOrDying() && owner.distanceTo(target) <= 40.0D;
        }
        else return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    private float calculateDamage(DamageSource source, LivingEntity owner, LivingEntity target) {
        float damage = target.getMaxHealth();
        float armor = (float) target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float f = 2.0F + toughness / 4.0F;
        float f1 = Mth.clamp(armor - damage / f, armor * 0.2F, 20.0F);
        damage /= 1.0F - f1 / 25.0F;

        MobEffectInstance instance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);

        if (instance != null) {
            int resistance = instance.getAmplifier();
            int i = (resistance + 1) * 5;
            int j = 25 - i;

            if (j == 0) {
                return damage;
            } else {
                float x = 25.0F / (float) j;
                damage = damage * x;
            }
        }

        // int k = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), source);

        // if (k > 0) {
        //     float f2 = Mth.clamp(k, 0.0F, 20.0F);
        //     damage /= 1.0F - f2 / 25.0F;
        // }
        return damage * 0.5f;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 200.0F;
    }

    @Override
    public boolean attack(DamageSource source, LivingEntity owner, LivingEntity target) {
        if (owner.level().isClientSide) return false;
        if (!HelperMethods.isMelee(source)) return false;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.addBlackFlash()) {
            ((ServerLevel) owner.level()).getChunkSource().broadcastAndSend(owner, new ClientboundAnimatePacket(target, ClientboundAnimatePacket.CRITICAL_HIT));
            target.hurt(JJKDamageSources.jujutsuAttack(owner, JJKAbilities.RATIO_RULE.get()), calculateDamage(source, owner, target) * (this.getPower(owner) * 0.15F ));
            owner.level().playSound(null, target.getX(), target.getY(), target.getZ(), JJKSounds.RATIO_RULE.get(), SoundSource.MASTER,
                    1.0F, 1.1F);
            return true;
        }
        else return false;
    }

    @Override
    public int getCooldown() {
        return 10 * 20;
    }

    @Override
    public Classification getClassification() {
        return Classification.SLASHING;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }
}

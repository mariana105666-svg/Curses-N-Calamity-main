package radon.jujutsu_kaisen.ability.idle_transfiguration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.VeilHandler;
import net.minecraft.world.level.block.Block;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.SelfEmbodimentOfPerfectionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class SelfEmbodimentOfPerfection extends DomainExpansion implements DomainExpansion.IClosedDomain {
    // @Override
    // public @Nullable ParticleOptions getEnvironmentParticle() {
    //     return ParticleTypes.WHITE_ASH;
    // }

    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        super.onHitEntity(domain, owner, entity, instant);

        if (IdleTransfiguration.checkSukuna(owner, entity)) return;
        
       // float attackerStrength = IdleTransfiguration.calculateStrength(owner);
        //float victimStrength = IdleTransfiguration.calculateStrength(entity);

        //int required = Math.round((victimStrength / Math.round(attackerStrength*2/7)) * 2);
        int amplifier = 2;
        MobEffectInstance existing = entity.getEffect(JJKEffects.TRANSFIGURED_SOUL.get());
        if (existing != null) {
            amplifier = existing.getAmplifier() + 1;
        }
        if (amplifier > 6) {
            amplifier = 6;
        }
        MobEffectInstance instance = new MobEffectInstance(JJKEffects.TRANSFIGURED_SOUL.get(), Math.round(20 * 20 * getStrength(owner, instant)),
                amplifier, false, true, true);
        JJKEffectUtil.addEffect(entity, instance);

        if (!owner.level().isClientSide) {
            PacketDistributor.TRACKING_ENTITY.with(() -> entity).send(new ClientboundUpdateMobEffectPacket(entity.getId(), instance));
        }
    }

    @Override
    public void onHitBlock(DomainExpansionEntity domain, LivingEntity owner, BlockPos pos) {

    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.DOMAIN;
    }

    @Override
    protected DomainExpansionEntity createBarrier(LivingEntity owner) {
        int radius = Math.round(this.getRadius(owner));

        ClosedDomainExpansionEntity domain = new ClosedDomainExpansionEntity(owner, this, radius);
        owner.level().addFreshEntity(domain);

        SelfEmbodimentOfPerfectionEntity entity = new SelfEmbodimentOfPerfectionEntity(domain);

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        //double forwardDistance = 3.5D;
        Vec3 pos = owner.position()
                .add(owner.getUpVector(1.0F).scale(entity.getBbHeight()))
                .subtract(look.multiply(entity.getBbWidth(), 0.0D, entity.getBbWidth()));
        //        .add(look.scale(forwardDistance)); 

        entity.moveTo(pos.x, pos.y, pos.z, RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));

        double d0 = look.horizontalDistance();
        entity.setYRot((float) (Mth.atan2(look.x, look.z) * (double) (180.0F / (float) Math.PI)));
        entity.setXRot((float) (Mth.atan2(look.y, d0) * (double) (180.0F / (float) Math.PI)));

        owner.level().addFreshEntity(entity);

        return domain;
    }

    @Override
    public List<Block> getBottomFloorBlocks() {
        return List.of(JJKBlocks.DOMAIN_FILLER.get());
    }

    @Override
    public List<Block> getBlocks() {
        return List.of(JJKBlocks.DOMAIN_SKY.get());
    }
}

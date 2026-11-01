package radon.jujutsu_kaisen.ability.projection_sorcery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.TimeCellMoonPalaceEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionCenterEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.projectile.FilmGaugeProjectile;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public class TimeCellMoonPalace extends DomainExpansion implements DomainExpansion.IClosedDomain {
    @Override
    public @Nullable ParticleOptions getEnvironmentParticle() {
        return ParticleTypes.WHITE_ASH;
    }

    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        super.onHitEntity(domain, owner, entity, instant);

        if (instant || owner.level().getGameTime() % 40 == 0) {
            DomainExpansionCenterEntity center = domain.getDomainCenter();

            if (center == null) return;

            owner.level().addFreshEntity(new FilmGaugeProjectile(owner, this.getPower(owner), entity, center));
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

        TimeCellMoonPalaceEntity entity = new TimeCellMoonPalaceEntity(domain);

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        Vec3 pos = owner.position()
                .subtract(RotationUtil.getTargetAdjustedLookAngle(owner).multiply(entity.getBbWidth(), 0.0D, entity.getBbWidth()));
        entity.moveTo(pos.x, pos.y, pos.z, RotationUtil.getTargetAdjustedYRot(owner), RotationUtil.getTargetAdjustedXRot(owner));

        double d0 = look.horizontalDistance();
        entity.setYRot((float) (Mth.atan2(look.x, look.z) * (double) (180.0F / (float) Math.PI)));
        entity.setXRot((float) (Mth.atan2(look.y, d0) * (double) (180.0F / (float) Math.PI)));
        if (!owner.level().isClientSide) {
        owner.level().addFreshEntity(entity);
        }

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

    

    //  @Override
    // public List<Block> getFillBlocks() {
    //     return List.of(JJKBlocks.TIME_CELL_MOON_PALACE.get());
    // }
}

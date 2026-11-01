package radon.jujutsu_kaisen.ability.disaster_tides;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
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
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

import java.util.List;

public class HorizonOfTheCaptivatingSkandha extends DomainExpansion implements DomainExpansion.IClosedDomain {
    private static final float DAMAGE = 10.0F;

    @Override
    public List<Block> getBlocks() {
        return List.of(JJKBlocks.FAKE_SKY.get());
    }

    @Override
    public List<Block> getFillBlocks() {
        return List.of(JJKBlocks.HORIZON_OF_THE_CAPTIVATING_SKANDHA_FILL.get());
    }

    @Override
    public List<Block> getDecorationBlocks() {
        return List.of(JJKBlocks.FAKE_WATER_DOMAIN.get());
    }

    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        if (instant || owner.level().getGameTime() % 20 == 0) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            entity.hurt(JJKDamageSources.indirectJujutsuAttack(domain, owner, JJKAbilities.DEATH_SWARM.get()),
                    DAMAGE * this.getPower(owner) * getStrength(owner,false));

            if (owner.hasLineOfSight(entity)) {
                Ability fish = JJKAbilities.DEATH_SWARM.get();
                ((IDomainAttack) fish).performEntity(owner, entity, domain);
            }
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

        return domain;
    }

    @Override
    public boolean canPlaceDecoration(ClosedDomainExpansionEntity domain, BlockPos pos) {
        LivingEntity owner = domain.getOwner();
        return owner != null && pos.getY() < owner.getBlockY();
    }
}

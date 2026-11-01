package radon.jujutsu_kaisen.ability.disaster_plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Block;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

import java.util.List;

public class ShiningSeaOfFlowers extends DomainExpansion implements DomainExpansion.IClosedDomain {
    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        super.onHitEntity(domain, owner, entity, instant);

        JJKEffectUtil.addEffect(entity, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Math.round(10 * 20 * this.getStrength(owner, instant)),
                4, false, false, false));
        JJKEffectUtil.addEffect(entity, new MobEffectInstance(MobEffects.DIG_SLOWDOWN, Math.round(10 * 20* this.getStrength(owner, instant)),
                4, false, false, false));
        JJKEffectUtil.addEffect(entity, new MobEffectInstance(MobEffects.WEAKNESS, Math.round(10 * 20 * this.getStrength(owner, instant)),
                4, false, false, false));
        if (JJKAbilities.hasToggled(entity, JJKAbilities.CURSED_ENERGY_FLOW.get()) && !JJKAbilities.hasToggled(entity, JJKAbilities.FALLING_BLOSSOM_EMOTION.get())) {
            JJKEffectUtil.addEffect(entity, new MobEffectInstance(JJKEffects.CURSED_BUD.get(), Math.round(10 * 20 * this.getStrength(owner, instant)), 0));
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
    public List<Block> getBlocks() {
        return List.of(JJKBlocks.FAKE_SKY.get());
    }

    @Override
    public List<Block> getFillBlocks() {
        return List.of(JJKBlocks.SHINING_SEA_OF_FLOWERS_FILL.get());
    }

    @Override
    public List<Block> getFloorBlocks() {
        return List.of(JJKBlocks.SHINING_SEA_OF_FLOWERS_FLOOR.get());
    }

    @Override
    public List<Block> getDecorationBlocks() {
        return List.of(JJKBlocks.SHINING_SEA_OF_FLOWERS_DECORATION_ONE.get(),
                JJKBlocks.SHINING_SEA_OF_FLOWERS_DECORATION_TWO.get(),
                JJKBlocks.SHINING_SEA_OF_FLOWERS_DECORATION_THREE.get(),
                JJKBlocks.SHINING_SEA_OF_FLOWERS_DECORATION_FOUR.get());
    }

    @Override
    public boolean canPlaceFloor(ClosedDomainExpansionEntity domain, BlockPos pos) {
        return !domain.level().getBlockState(pos).isAir() && domain.level().getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean canPlaceDecoration(ClosedDomainExpansionEntity domain, BlockPos pos) {
        return domain.level().getBlockState(pos.below()).is(JJKBlocks.SHINING_SEA_OF_FLOWERS_FLOOR.get());
    }
}

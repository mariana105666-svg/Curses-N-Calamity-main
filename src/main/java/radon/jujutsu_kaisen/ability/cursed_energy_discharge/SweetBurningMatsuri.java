package radon.jujutsu_kaisen.ability.cursed_energy_discharge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

public class SweetBurningMatsuri extends DomainExpansion implements DomainExpansion.IClosedDomain {
    private static final float EXPLOSIVE_POWER = 4.0F;

    private static final float DAMAGE_MULTIPLIER = 2.0F;

    private static final int INTERVAL = 30;

    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        super.onHitEntity(domain, owner, entity, instant);

        if (!instant && owner.level().getGameTime() % INTERVAL != 0) return;

        entity.level().explode(owner, JJKDamageSources.indirectJujutsuAttack(domain, owner, this), null,
                entity.getX(), entity.getY() + (entity.getBbHeight() / 2.0F), entity.getZ(),
                EXPLOSIVE_POWER, false, Level.ExplosionInteraction.NONE);
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID)
    public static class SweetBurningMatsuriEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource() instanceof JJKDamageSources.JujutsuDamageSource source)) return;
            if (source.getAbility() != JJKAbilities.SWEET_BURNING_MATSURI.get()) return;

            event.setAmount(event.getAmount() * DAMAGE_MULTIPLIER);
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
        return List.of(JJKBlocks.DOMAIN_SKY.get());
    }

    @Override
    public List<Block> getBottomFloorBlocks() {
        return List.of(JJKBlocks.DOMAIN_FILLER.get());
    }
}

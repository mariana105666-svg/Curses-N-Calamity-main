package radon.jujutsu_kaisen.ability.shrine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Block;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.entity.MalevolentShrineClosedEntity;
import radon.jujutsu_kaisen.entity.MalevolentShrineEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

public class MalevolentShrine extends DomainExpansion implements DomainExpansion.IClosedDomain {
    public static final int DELAY = 2 * 17;
    private static final int INTERVAL = 5;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        boolean enemyDomain = false;
        IDomain selfDomain = null;
       // ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
        for (DomainExpansionEntity d : domain.getClashers() ) {   
            if (d.getOwner() == owner) {
                selfDomain = d;
            }
            else if (d.getOwner() != owner) {
                enemyDomain = true;
            }
        }
    }

        if (enemyDomain == true) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
                cap.toggle(JJKAbilities.DOMAIN_AMPLIFICATION.get());
            }
            if (!cap.hasToggled(this)) {
                return true;
            }

            if (cap.hasToggled(this)) {
                return false;
            }
        }

        else if (selfDomain != null && enemyDomain != true) {
            if (target != null) {
                return (selfDomain.getDomain().distanceTo(target) >= 130.0D);
            }

            if (target == null) {
                return HelperMethods.RANDOM.nextInt(15) == 0;
            }

        }
        return target != null && owner.distanceTo(target) <= 25.0D && owner.getHealth() / owner.getMaxHealth() < 0.9F && HelperMethods.RANDOM.nextInt(4) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.DOMAIN;
    }


    @Override
    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
        super.onHitEntity(domain, owner, entity, instant);

        if (instant || domain.getTime() == DELAY || (domain.level().getGameTime() % INTERVAL == 0 && domain.getTime() >= DELAY)) {
            if (this.getHitEnvironment() && JJKAbilities.hasTrait(entity, Trait.HEAVENLY_RESTRICTION)) {
                Ability dismantle = JJKAbilities.DISMANTLE.get();
                ((IDomainAttack) dismantle).performEntity(owner, entity, domain);
            }
            else if (!JJKAbilities.hasTrait(entity, Trait.HEAVENLY_RESTRICTION)) {
                Ability cleave = JJKAbilities.CLEAVE.get();
                ((IDomainAttack) cleave).performEntity(owner, entity, domain);
            }
        }
    }

    @Override
    public boolean getHitEnvironment() {
        return true;
    }

    @Override
    public void onHitBlock(DomainExpansionEntity domain, LivingEntity owner, BlockPos pos) {
        if (HelperMethods.RANDOM.nextInt(10) != 0) return;

        Ability dismantle = JJKAbilities.DISMANTLE.get();
        ((IDomainAttack) dismantle).performBlock(owner, domain, pos);
    }

   

    @Override
    protected DomainExpansionEntity createBarrier(LivingEntity owner) {
        int radius = Math.round(this.getRadius(owner));

        MalevolentShrineClosedEntity domain = new MalevolentShrineClosedEntity(owner, this, radius);
        owner.level().addFreshEntity(domain);

        return domain;
    }

    @Override
    protected DomainExpansionEntity createOpenBarrier(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        float size = cap.isOpenDomain()
                ? (float) cap.getDomainConfig(radon.jujutsu_kaisen.capability.data.sorcerer.DomainConfiguration.OPEN_DOMAIN_SIZE)
                : cap.getDomainSize();

        int width = Math.round(this.getWidth() * size);
        int height = Math.round(this.getHeight() * size);

        MalevolentShrineEntity domain = new MalevolentShrineEntity(owner, this, width, height);
        owner.level().addFreshEntity(domain);

        return domain;
    }

    public int getWidth() {
        return 112;
    }

    public int getHeight() {
        return 85;
    }

    @Override
    public List<Block> getBlocks() {
        return List.of(JJKBlocks.DOMAIN_SKY.get());
    }

    @Override
    public List<Block> getBottomFloorBlocks() {
        return List.of(JJKBlocks.DOMAIN_FILLER.get() );
    }
}

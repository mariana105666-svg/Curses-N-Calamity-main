package radon.jujutsu_kaisen.block.entity;


import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

public interface IDomainBarrier extends IBarrier {
    int getRadius();

    List<DomainExpansionEntity> getClashers();

    DomainExpansionEntity checkSureHitEffect();

    default DomainExpansionEntity sureHitTarget(LivingEntity target) {
        DomainExpansionEntity surehit = this.checkSureHitEffect();
        if (surehit != null) {
            if (surehit.isAffected(target, false)) {
                return surehit;
            }
        }
        return null;
    }
}   

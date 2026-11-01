package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

public interface IBarrier {
    Level level();

    default boolean isAffected(BlockPos pos) {
         return this.isInsideBarrier(pos);
    }

    // default boolean isOwned(BlockPos pos) {
    //     if (!(this.level() instanceof ServerLevel level)) return false;
    //     return VeilHandler.isOwnedBy(level, pos, this);
    // }

    // default boolean isOwnedByNonDomain(BlockPos pos) {
    //     if (!(this.level() instanceof ServerLevel level)) return false;
    //     return VeilHandler.isOwnedNonDomain(level, pos);
    // }


    boolean isBarrier(BlockPos pos);

    boolean isInsideBarrier(BlockPos pos);

    boolean isBarrierOrInside(BlockPos pos);

    AABB getBounds();



    // default float getStrength() {
    //     LivingEntity owner = this.getOwner();
    //     if (owner == null) return 0.0F;
    //     ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
    //     float domainMod = 1.0F;
    //     // if (JJKAbilities.CHIMERA_SHADOW_GARDEN.get() == this.ability ) {
    //     //     domainMod *= 0.66;
    //     // }
    //     return (cap.getAbilityPower() * (owner.getHealth() / owner.getMaxHealth())) * domainMod ;
    // }

    // default float getStrength() {
    //     LivingEntity owner = this.getOwner();

    //     if (owner == null) return 0;

    //     IJujutsuCapability cap = owner.getCapability(JujutsuCapabilityHandler.INSTANCE);

    //     if (cap == null) return 0;

    //     ISkillData data = cap.getSkillData();

    //     int skill = data.getSkill(Skill.BARRIER) + 1;

    //     return Math.round(skill * (this instanceof IDomain ? ConfigHolder.SERVER.domainStrength.get().floatValue() : 1.0F)) *
    //             (owner.getHealth() / owner.getMaxHealth());
    // }
    
}
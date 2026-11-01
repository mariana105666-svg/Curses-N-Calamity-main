package radon.jujutsu_kaisen.block.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.entity.VeilEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;


import java.util.List;

public interface IVeil extends IBarrier {
    List<Modifier> getModifiers();

    VeilEntity getVeil();

    int getRadius();

    LivingEntity getOwner();


    // default boolean canDamage(LivingEntity victim) {
    //     LivingEntity owner = this.getOwner();

    //     if (owner == null) return true;

    //     return VeilUtil.canDamage(this.getModifiers());
    // }

    // default boolean canDestroy(Entity entity, BlockPos target) {
    //     return VeilUtil.canDestroy(entity, target, ((Entity) this).getUUID(), this.getModifiers());
    // }
}

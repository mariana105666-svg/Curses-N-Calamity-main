package radon.jujutsu_kaisen.ability.base;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public interface IRMBAble {
    boolean onRightClick(LivingEntity owner);
}

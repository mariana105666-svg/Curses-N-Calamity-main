package radon.jujutsu_kaisen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

@OnlyIn(Dist.CLIENT)
public class ResurrectionHandler {

    public static void handle(int src, float health) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null) return;

        Entity e = level.getEntity(src);
        if (e instanceof LivingEntity living) {
            if (living.isDeadOrDying()) {
                living.setHealth(health);
            }
            living.deathTime = 0;
        }
    }
}

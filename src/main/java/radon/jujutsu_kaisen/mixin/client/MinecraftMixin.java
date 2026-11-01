package radon.jujutsu_kaisen.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Redirect(method = "shouldEntityAppearGlowing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z"))
    public boolean isCurrentlyGlowing(Entity instance) {
       
        

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        Entity owner = player;
        ClientVisualHandler.ClientData playerdata = ClientVisualHandler.get(owner);
        if (playerdata != null && playerdata.toggled.contains(JJKAbilities.MYTHICAL_BEAST_AMBER.get() )) {
            if (instance.isCurrentlyGlowing()) return true;
        }
        if (!player.hasLineOfSight(instance)) return false;
        

        ClientVisualHandler.ClientData data = ClientVisualHandler.get(instance);
        
      

        return data != null && data.toggled.contains(JJKAbilities.DOMAIN_AMPLIFICATION.get());
    }
}

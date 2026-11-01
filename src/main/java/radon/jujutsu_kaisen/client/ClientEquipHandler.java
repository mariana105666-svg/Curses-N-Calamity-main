package radon.jujutsu_kaisen.client;

import net.minecraft.resources.ResourceLocation;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.client.gui.screen.JutwotsuScreen;
import radon.jujutsu_kaisen.client.gui.screen.MeleeScreen;
import radon.jujutsu_kaisen.network.packet.s2c.EquipAbilityS2CPacket;

public class ClientEquipHandler {
    private ClientEquipHandler() {
    }

    public static void equip(ResourceLocation key) {
        if (EquipAbilityS2CPacket.NOTHING.equals(key)) {
            MeleeScreen.select(null);
            JutwotsuScreen.select(null);
            return;
        }

        Ability ability = JJKAbilities.getValue(key);

        if (ability == null) return;

        switch (ability.getMenuType()) {
            case MELEE -> MeleeScreen.select(ability);
            case J2TSU -> JutwotsuScreen.select(ability);
            default -> { }
        }
    }
}

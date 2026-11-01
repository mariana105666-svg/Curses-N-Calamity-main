package radon.jujutsu_kaisen.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.client.JJKKeys;
import radon.jujutsu_kaisen.client.gui.screen.base.RadialScreen;

import java.util.ArrayList;
import java.util.List;

public class JutwotsuScreen extends RadialScreen {
    @Nullable
    private static Ability selected;

    public static void select(@Nullable Ability ability) {
        selected = ability;
    }

    public static @Nullable Ability getSelected() {
        if (selected == null) return null;

        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;

        if (player != null && !selected.isValid(player)) selected = null;

        return selected;
    }

    private boolean isToggle(DisplayItem item) {
        return item.ability != null && this.minecraft != null && this.minecraft.player != null
                && item.ability.getActivationType(this.minecraft.player) == Ability.ActivationType.TOGGLED;
    }

    @Override
    protected boolean isActive(DisplayItem item) {
        if (this.isToggle(item)) return super.isActive(item);

        return JutwotsuScreen.getSelected() == item.ability || super.isActive(item);
    }

    @Override
    protected List<DisplayItem> getItems() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return List.of();

        List<Ability> abilities = JJKAbilities.getAbilities(this.minecraft.player);
        abilities.removeIf(ability -> ability.getMenuType() != MenuType.J2TSU);

        return new ArrayList<>(abilities.stream().map(DisplayItem::new).toList());
    }

    @Override
    public void onClose() {
        super.onClose();

        if (this.hovered != -1) {
            if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;

            DisplayItem item = this.getCurrent().get(this.hovered);

            ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap != null && this.isToggle(item)) {
                if (cap.hasToggled(item.ability)) {
                    radon.jujutsu_kaisen.ability.AbilityHandler.untrigger(this.minecraft.player, item.ability);
                    PacketHandler.sendToServer(new radon.jujutsu_kaisen.network.packet.c2s.UntriggerAbilityC2SPacket(
                            JJKAbilities.getKey(item.ability)));
                } else {
                    PacketHandler.sendToServer(new radon.jujutsu_kaisen.network.packet.c2s.TriggerAbilityC2SPacket(
                            JJKAbilities.getKey(item.ability)));
                }
            }

            selected = item.ability;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);

        if (this.minecraft == null || this.minecraft.player == null) return;

        if (!this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!cap.hasTechnique(CursedTechnique.MIMICRY)) return;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int x = centerX;
        int y = centerY - RADIUS_OUT - this.font.lineHeight * 2;

        pGuiGraphics.drawCenteredString(this.font, Component.translatable(String.format("gui.%s.ability.right_click", JujutsuKaisen.MOD_ID)), x, y, 16777215);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == JJKKeys.ACTIVATE_J2TSU_MENU.getKey().getValue()) {
            this.onClose();
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }
}
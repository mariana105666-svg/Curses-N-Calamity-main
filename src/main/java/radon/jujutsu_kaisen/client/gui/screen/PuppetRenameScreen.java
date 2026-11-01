package radon.jujutsu_kaisen.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.RenamePuppetC2SPacket;
import radon.jujutsu_kaisen.puppet.PuppetUtil;

public class PuppetRenameScreen extends Screen {
    private static final Component TITLE = Component.translatable(String.format("gui.%s.puppet_rename", JujutsuKaisen.MOD_ID));

    private static final int WIDTH = 160;

    private final int slot;
    private final String current;

    private EditBox name;

    public PuppetRenameScreen(int slot, String current) {
        super(TITLE);

        this.slot = slot;
        this.current = current;
    }

    @Override
    protected void init() {
        int x = (this.width - WIDTH) / 2;
        int y = this.height / 2 - 20;

        this.name = new EditBox(this.font, x, y, WIDTH, 20, TITLE);
        this.name.setMaxLength(PuppetUtil.MAX_NAME_LENGTH);
        this.name.setValue(this.current);
        this.name.moveCursorToEnd();

        this.addRenderableWidget(this.name);
        this.setInitialFocus(this.name);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.confirm())
                .bounds(x, y + 26, WIDTH, 20).build());
    }

    private void confirm() {
        String value = this.name.getValue().trim();

        if (!value.isEmpty() && !value.equals(this.current)) {
            PacketHandler.sendToServer(new RenamePuppetC2SPacket(this.slot, value));
        }
        this.onClose();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 257 || pKeyCode == 335) {
            this.confirm();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        this.renderBackground(pGuiGraphics);

        pGuiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, this.height / 2 - 34, 16777215);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

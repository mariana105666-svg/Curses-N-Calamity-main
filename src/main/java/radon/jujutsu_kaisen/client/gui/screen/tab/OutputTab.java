package radon.jujutsu_kaisen.client.gui.screen.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen;
import radon.jujutsu_kaisen.client.gui.screen.widget.AbilityOutputListWidget;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.SetAbilityOutputC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.SetOutputLockC2SPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class OutputTab extends JJKTab {
    private static final Component TITLE = Component.translatable(String.format("gui.%s.output", JujutsuKaisen.MOD_ID));
    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/stone.png");

    @Nullable
    private AbilityOutputListWidget.Entry selected;

    private ForgeSlider output;
    private Button zone;
    private Button reset;
    private Button lock;

    private double previous = Double.NaN;

    public OutputTab(Minecraft minecraft, JujutsuScreen screen, JJKTabType type, int index, int page) {
        super(minecraft, screen, type, index, page, Items.COMPARATOR.getDefaultInstance(), TITLE, BACKGROUND, false);
    }

    @Nullable
    private ISorcererData cap() {
        if (this.minecraft == null || this.minecraft.player == null) return null;

        return this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
    }

    @Override
    public void tick() {
        if (this.selected == null || this.output == null) return;

        double value = this.output.getValue();

        if (value == this.previous) return;

        this.previous = value;

        this.send((float) value / 100.0F, this.isZoneLocked());
    }

    private boolean isZoneLocked() {
        ISorcererData cap = this.cap();

        return cap != null && this.selected != null && cap.isZoneLocked(this.selected.get());
    }

    private void send(float value, boolean locked) {
        if (this.selected == null) return;

        ISorcererData cap = this.cap();

        if (cap == null) return;

        Ability ability = this.selected.get();

        cap.setAbilityOutput(ability, value);
        cap.setZoneLocked(ability, locked);

        PacketHandler.sendToServer(new SetAbilityOutputC2SPacket(JJKAbilities.getKey(ability), value, locked, false));
    }

    public void setSelected(AbilityOutputListWidget.Entry entry) {
        this.selected = entry;

        ISorcererData cap = this.cap();

        if (cap == null) return;

        Ability ability = entry.get();

        float value = cap.hasAbilityOutput(ability) ? cap.getAbilityOutput(ability) : cap.getOutput();

        this.previous = Math.round(Math.min(1.0F, value) * 100.0F);

        this.output.setValue(this.previous);
        this.output.visible = true;
        this.output.active = true;

        this.zone.visible = true;
        this.zone.active = true;
        this.zone.setMessage(this.zoneLabel());

        this.reset.visible = true;
        this.reset.active = true;
    }

    private Component zoneLabel() {
        return Component.translatable(String.format("gui.%s.output.zone.%s", JujutsuKaisen.MOD_ID,
                this.isZoneLocked() ? "locked" : "boosted"));
    }

    private Component lockLabel() {
        ISorcererData cap = this.cap();

        return Component.translatable(String.format("gui.%s.output.lock.%s", JujutsuKaisen.MOD_ID,
                cap != null && cap.isOutputLocked() ? "on" : "off"));
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildList(Consumer<T> consumer, Function<Ability, T> result) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        for (Ability ability : JJKAbilities.getAbilities(this.minecraft.player)) {
            if (!ability.isScalable(this.minecraft.player) && !ability.isChantable()) continue;

            consumer.accept(result.apply(ability));
        }
    }

    @Override
    public void drawContents(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.drawContents(pGuiGraphics, pX, pY);

        if (this.minecraft == null) return;

        int i = (this.screen.width - JujutsuScreen.WINDOW_WIDTH) / 2;
        int j = (this.screen.height - JujutsuScreen.WINDOW_HEIGHT) / 2;

        int xOffset = i + (JujutsuScreen.WINDOW_WIDTH - JujutsuScreen.WINDOW_INSIDE_WIDTH);
        int yOffset = j + (JujutsuScreen.WINDOW_HEIGHT - JujutsuScreen.WINDOW_INSIDE_HEIGHT);

        pGuiGraphics.drawString(this.minecraft.font, TITLE, xOffset, yOffset, 16777215, true);

        if (this.selected == null) return;

        pGuiGraphics.drawString(this.minecraft.font, this.selected.get().getName(),
                xOffset + 86, yOffset + this.minecraft.font.lineHeight + 1, 16777215, true);
    }

    @Override
    public void addWidgets() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        int i = (this.screen.width - JujutsuScreen.WINDOW_WIDTH) / 2;
        int j = (this.screen.height - JujutsuScreen.WINDOW_HEIGHT) / 2;

        int xOffset = i + (JujutsuScreen.WINDOW_WIDTH - JujutsuScreen.WINDOW_INSIDE_WIDTH);
        int yOffset = j + (JujutsuScreen.WINDOW_HEIGHT - JujutsuScreen.WINDOW_INSIDE_HEIGHT);

        this.addRenderableWidget(new AbilityOutputListWidget(this::buildList, this::setSelected, this.minecraft, 78, 85,
                xOffset, yOffset + this.minecraft.font.lineHeight + 1));

        this.output = new ForgeSlider(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 18, 130, 16,
                Component.empty(), Component.literal("%"), 10.0D, 100.0D, 100.0D, 1.0D, 0, true);
        this.output.visible = false;
        this.output.active = false;
        this.addRenderableWidget(this.output);

        this.zone = Button.builder(Component.empty(), button -> {
            this.send((float) this.output.getValue() / 100.0F, !this.isZoneLocked());
            button.setMessage(this.zoneLabel());
        }).size(130, 20).pos(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 38).build();
        this.zone.visible = false;
        this.zone.active = false;
        this.addRenderableWidget(this.zone);

        this.reset = Button.builder(Component.translatable(String.format("gui.%s.output.reset", JujutsuKaisen.MOD_ID)), button -> {
            if (this.selected == null) return;

            ISorcererData cap = this.cap();

            if (cap == null) return;

            Ability ability = this.selected.get();

            cap.clearAbilityOutput(ability);
            cap.setZoneLocked(ability, false);

            PacketHandler.sendToServer(new SetAbilityOutputC2SPacket(JJKAbilities.getKey(ability), 1.0F, false, true));

            this.previous = Math.round(cap.getOutput() * 100.0F);
            this.output.setValue(this.previous);
            this.zone.setMessage(this.zoneLabel());
        }).size(130, 20).pos(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 60).build();
        this.reset.visible = false;
        this.reset.active = false;
        this.addRenderableWidget(this.reset);

        this.lock = Button.builder(this.lockLabel(), button -> {
            ISorcererData cap = this.cap();

            if (cap == null) return;

            boolean locked = !cap.isOutputLocked();

            cap.setOutputLocked(locked);

            PacketHandler.sendToServer(new SetOutputLockC2SPacket(locked));

            button.setMessage(this.lockLabel());
        }).size(130, 20).pos(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 82).build();
        this.addRenderableWidget(this.lock);
    }
}

package radon.jujutsu_kaisen.client.gui.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ability.base.Ability;

public class AbilityOutputListWidget extends JJKSelectionList<Ability, AbilityOutputListWidget.Entry> {
    public AbilityOutputListWidget(IBuilder<Ability, Entry> builder, ICallback<Entry> callback, Minecraft minecraft, int width, int height, int x, int y) {
        super(builder, callback, minecraft, width, height, x, y);
    }

    @Override
    public void refreshList() {
        super.refreshList();

        this.builder.build(this::addEntry, AbilityOutputListWidget.Entry::new);
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final Ability ability;

        Entry(Ability ability) {
            this.ability = ability;
        }

        public Ability get() {
            return this.ability;
        }

        @Override
        public void render(@NotNull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            FormattedText formatted = FormattedText.of(this.ability.getName().getString());

            int delta = AbilityOutputListWidget.this.minecraft.font.width(formatted) - pWidth;

            formatted = AbilityOutputListWidget.this.minecraft.font.substrByWidth(formatted, pWidth);

            if (delta > 0) {
                String text = formatted.getString();
                formatted = FormattedText.of(String.format("%s...", text.substring(0, text.length() - 3)));
            }
            pGuiGraphics.drawString(AbilityOutputListWidget.this.minecraft.font, Language.getInstance()
                            .getVisualOrder(FormattedText.composite(formatted)),
                    pLeft + 3, pTop + 2, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            AbilityOutputListWidget.this.callback.setSelected(this);
            AbilityOutputListWidget.this.setSelected(this);
            return false;
        }

        @Override
        public @NotNull Component getNarration() {
            return this.ability.getName();
        }
    }
}

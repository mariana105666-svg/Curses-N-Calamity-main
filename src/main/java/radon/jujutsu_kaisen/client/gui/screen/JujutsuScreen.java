package radon.jujutsu_kaisen.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import radon.jujutsu_kaisen.client.gui.screen.tab.AbilityTab;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.client.gui.screen.tab.*;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class JujutsuScreen extends Screen {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    public static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140; 
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;

    private static final int SIZE_BUTTON = 11;
    private static final int SIZE_BUTTON_X = 4;
    private static final int SIZE_BUTTON_Y = 4;

    private static final int SIZE_BUTTON_FILL = 0xFF2B2B2B;
    private static final int SIZE_BUTTON_HOVER = 0xFF4A4A4A;
    private static final int SIZE_BUTTON_BORDER = 0xFF000000;

    private static final int BORDER_LEFT = 9;
    private static final int BORDER_RIGHT = 9;
    private static final int BORDER_TOP = 18;
    private static final int BORDER_BOTTOM = 9;

    private static final int TAB_ROOM = 32;

    private static int abilitySize = 1;

    private static int active = 1;

    public static int insideWidth() {
        return WINDOW_INSIDE_WIDTH * active;
    }

    public static int insideHeight() {
        return WINDOW_INSIDE_HEIGHT * active;
    }

    public static int windowWidth() {
        return insideWidth() + BORDER_LEFT + BORDER_RIGHT;
    }

    public static int windowHeight() {
        return insideHeight() + BORDER_TOP + BORDER_BOTTOM;
    }

    public static void scissor(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.enableScissor(x, y, x + width, y + height);
    }

    private int maxSize() {
        int byWidth = (this.width - BORDER_LEFT - BORDER_RIGHT) / WINDOW_INSIDE_WIDTH;
        int byHeight = (this.height - BORDER_TOP - BORDER_BOTTOM - TAB_ROOM) / WINDOW_INSIDE_HEIGHT;

        return Math.max(1, Math.min(byWidth, byHeight));
    }

    private boolean isOverSize(double x, double y) {
        if (!(this.selectedTab instanceof AbilityTab)) return false;

        int left = (this.width - windowWidth()) / 2 + SIZE_BUTTON_X;
        int top = (this.height - windowHeight()) / 2 + SIZE_BUTTON_Y;

        return x >= left && x < left + SIZE_BUTTON && y >= top && y < top + SIZE_BUTTON;
    }

    private void renderSize(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        if (!(this.selectedTab instanceof AbilityTab)) return;

        int left = offsetX + SIZE_BUTTON_X;
        int top = offsetY + SIZE_BUTTON_Y;

        graphics.fill(left - 1, top - 1, left + SIZE_BUTTON + 1, top + SIZE_BUTTON + 1, SIZE_BUTTON_BORDER);
        graphics.fill(left, top, left + SIZE_BUTTON, top + SIZE_BUTTON,
                this.isOverSize(mouseX, mouseY) ? SIZE_BUTTON_HOVER : SIZE_BUTTON_FILL);

        Component label = Component.literal(String.valueOf(abilitySize));

        graphics.drawString(this.font, label,
                left + (SIZE_BUTTON - this.font.width(label)) / 2 + 1,
                top + (SIZE_BUTTON - this.font.lineHeight) / 2 + 1, -1, false);
    }

    private final List<JJKTab> tabs = new ArrayList<>();

    @Nullable
    private JJKTab selectedTab;
    private static int tabPage, maxPages;

    private boolean isScrolling;

    public JujutsuScreen() {
        super(GameNarrator.NO_TITLE);
    }

    private void setSelectedTab(@NotNull JJKTab tab) {
        if (this.selectedTab != null) {
            for (GuiEventListener widget : this.selectedTab.getRenderables()) {
                this.removeWidget(widget);
            }
            this.selectedTab.removeWidgets();
        }
        this.selectedTab = tab;
        this.selectedTab.addWidgets();
    }

    public void removeWidgets() {
        if (this.selectedTab != null) {
            for (GuiEventListener widget : this.selectedTab.getRenderables()) {
                this.removeWidget(widget);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.selectedTab != null) {
            this.selectedTab.tick();
        }
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(@NotNull T pWidget) {
        return super.addRenderableWidget(pWidget);
    }

    public static boolean hasQuickVeil() {
        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;

        if (player == null) return false;

        radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData cap =
                player.getCapability(radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler.INSTANCE)
                        .resolve().orElse(null);

        return cap != null && cap.isUnlocked(radon.jujutsu_kaisen.ability.JJKAbilities.QUICK_VEIL_CONFIG.get());
    }

    @Override
    protected void init() {
        this.tabs.clear();
        this.selectedTab = null;

        int index = 0;
        this.tabs.add(new StatsTab(this.minecraft, this, JJKTabType.ABOVE, 0, 0));
        index++;
        this.tabs.add(new DomainCustomizationTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new PactTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new BindingVowTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new AbilityTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new ChantTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new CursedEnergyColorTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        index++;
        this.tabs.add(new OutputTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));

        if (hasQuickVeil()) {
            index++;
            this.tabs.add(new QuickVeilTab(this.minecraft, this, JJKTabType.ABOVE, index % JJKTabType.MAX_TABS, index / JJKTabType.MAX_TABS));
        }

        this.setSelectedTab(this.tabs.get(0));

        if (this.tabs.size() > JJKTabType.MAX_TABS) {
            int guiLeft = (this.width - WINDOW_WIDTH) / 2;
            int guiTop = (this.height - WINDOW_HEIGHT) / 2;
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0))
                    .pos(guiLeft, guiTop - 50).size(20, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages))
                    .pos(guiLeft + WINDOW_WIDTH - 20, guiTop - 50).size(20, 20).build());
            maxPages = this.tabs.size() / JJKTabType.MAX_TABS;
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && this.isOverSize(pMouseX, pMouseY)) {
            abilitySize = abilitySize >= this.maxSize() ? 1 : abilitySize + 1;
            return true;
        }

        if (this.selectedTab != null) {
            int i = (this.width - windowWidth()) / 2;
            int j = (this.height - windowHeight()) / 2;
            this.selectedTab.mouseClicked(pMouseX - i - 9, pMouseY - j - 18, pButton);
        }
        
        if (pButton == 0) {
            int i = (this.width - WINDOW_WIDTH) / 2;
            int j = (this.height - WINDOW_HEIGHT) / 2;

            for (JJKTab tab : this.tabs) {
                if (tab.getPage() == tabPage && tab.isMouseOver(i, j, pMouseX, pMouseY)) {
                    this.setSelectedTab(tab);
                    break;
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.selectedTab != null) {
            this.selectedTab.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        abilitySize = Mth.clamp(abilitySize, 1, this.maxSize());
        active = this.selectedTab instanceof AbilityTab ? abilitySize : 1;

        int i = (this.width - windowWidth()) / 2;
        int j = (this.height - windowHeight()) / 2;
        this.renderBackground(pGuiGraphics);

        if (maxPages != 0) {
            net.minecraft.network.chat.Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
            int width = this.font.width(page);
            pGuiGraphics.drawString(this.font, page.getVisualOrderText(), i + (windowWidth() / 2) - (width / 2), j - 44, -1);
        }
        this.renderInside(pGuiGraphics, pMouseX, pMouseY, i, j);
        this.renderWindow(pGuiGraphics, i, j);
        this.renderSize(pGuiGraphics, i, j, pMouseX, pMouseY);
        this.renderTooltips(pGuiGraphics, pMouseX, pMouseY, i, j);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private void renderInside(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
        if (this.selectedTab != null) {
            this.selectedTab.drawContents(pGuiGraphics, pOffsetX + WINDOW_INSIDE_X, pOffsetY + WINDOW_INSIDE_Y);
        }
    }

    private void renderFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        int innerW = width - BORDER_LEFT - BORDER_RIGHT;
        int innerH = height - BORDER_TOP - BORDER_BOTTOM;

        int srcW = WINDOW_WIDTH - BORDER_LEFT - BORDER_RIGHT;
        int srcH = WINDOW_HEIGHT - BORDER_TOP - BORDER_BOTTOM;

        int right = x + width - BORDER_RIGHT;
        int bottom = y + height - BORDER_BOTTOM;

        int srcRight = WINDOW_WIDTH - BORDER_RIGHT;
        int srcBottom = WINDOW_HEIGHT - BORDER_BOTTOM;

        graphics.blit(WINDOW_LOCATION, x, y, BORDER_LEFT, BORDER_TOP, 0.0F, 0.0F, BORDER_LEFT, BORDER_TOP, 256, 256);
        graphics.blit(WINDOW_LOCATION, right, y, BORDER_RIGHT, BORDER_TOP, srcRight, 0.0F, BORDER_RIGHT, BORDER_TOP, 256, 256);
        graphics.blit(WINDOW_LOCATION, x, bottom, BORDER_LEFT, BORDER_BOTTOM, 0.0F, srcBottom, BORDER_LEFT, BORDER_BOTTOM, 256, 256);
        graphics.blit(WINDOW_LOCATION, right, bottom, BORDER_RIGHT, BORDER_BOTTOM, srcRight, srcBottom, BORDER_RIGHT, BORDER_BOTTOM, 256, 256);

        graphics.blit(WINDOW_LOCATION, x + BORDER_LEFT, y, innerW, BORDER_TOP, BORDER_LEFT, 0.0F, srcW, BORDER_TOP, 256, 256);
        graphics.blit(WINDOW_LOCATION, x + BORDER_LEFT, bottom, innerW, BORDER_BOTTOM, BORDER_LEFT, srcBottom, srcW, BORDER_BOTTOM, 256, 256);
        graphics.blit(WINDOW_LOCATION, x, y + BORDER_TOP, BORDER_LEFT, innerH, 0.0F, BORDER_TOP, BORDER_LEFT, srcH, 256, 256);
        graphics.blit(WINDOW_LOCATION, right, y + BORDER_TOP, BORDER_RIGHT, innerH, srcRight, BORDER_TOP, BORDER_RIGHT, srcH, 256, 256);
    }

    public void renderWindow(GuiGraphics pGuiGraphics, int pOffsetX, int pOffsetY) {
        RenderSystem.enableBlend();
        this.renderFrame(pGuiGraphics, pOffsetX, pOffsetY, windowWidth(), windowHeight());

        for (JJKTab tab : this.tabs) {
            if (tab.getPage() == tabPage) {
                tab.drawTab(pGuiGraphics, pOffsetX, pOffsetY, tab == this.selectedTab);
            }
        }

        for (JJKTab tab : this.tabs) {
            if (tab.getPage() == tabPage) {
                tab.drawIcon(pGuiGraphics, pOffsetX, pOffsetY);
            }
        }
    }

    private void renderTooltips(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
        if (this.selectedTab != null) {
            this.selectedTab.drawTooltips(pGuiGraphics, pMouseX, pMouseY, pOffsetX, pOffsetY);
        }

        for (JJKTab JJKTab : this.tabs) {
            if (JJKTab.getPage() == tabPage && JJKTab.isMouseOver(pOffsetX, pOffsetY, pMouseX, pMouseY)) {
                pGuiGraphics.renderTooltip(this.font, JJKTab.getTitle(), pMouseX, pMouseY);
            }
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton != 0) {
            this.isScrolling = false;
            return false;
        } else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.scroll(pDragX, pDragY);
            }
            return true;
        }
    }
}

package radon.jujutsu_kaisen.client.gui.screen.tab;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen;
import radon.jujutsu_kaisen.client.veil.QuickVeilSettings;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;

import java.util.ArrayList;
import java.util.List;

public class QuickVeilTab extends JJKTab {
    private static final Component TITLE = Component.translatable(String.format("gui.%s.quick_veil", JujutsuKaisen.MOD_ID));
    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/stone.png");

    private static final int INSIDE_X = 9;
    private static final int INSIDE_Y = 18;

    private static final int COLUMNS = 2;
    private static final int SPACING = 2;
    private static final int ROW_HEIGHT = 14;
    private static final int BUTTON_WIDTH = (JujutsuScreen.WINDOW_INSIDE_WIDTH - SPACING) / COLUMNS;

    private final List<Button> options = new ArrayList<>();

    private final List<Button> adminOnly = new ArrayList<>();

    private ForgeSlider sizeSlider;

    public QuickVeilTab(Minecraft minecraft, JujutsuScreen screen, JJKTabType type, int index, int page) {
        super(minecraft, screen, type, index, page, Items.BARRIER.getDefaultInstance(), TITLE, BACKGROUND, false);
    }

    private boolean canUseAdmin() {
        if (this.minecraft == null || this.minecraft.player == null) return false;

        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasAdminVeil();
    }

    @Override
    public void tick() {
        if (this.sizeSlider != null) QuickVeilSettings.setSize(this.sizeSlider.getValueInt());
    }

    @Override
    public void addWidgets() {
        this.options.clear();
        this.adminOnly.clear();

        int left = ((this.screen.width - JujutsuScreen.WINDOW_WIDTH) / 2) + INSIDE_X;
        int top = ((this.screen.height - JujutsuScreen.WINDOW_HEIGHT) / 2) + INSIDE_Y;

        Modifier.Type[] rules = QuickVeilSettings.RULES;

        for (int i = 0; i < rules.length; i++) {
            Modifier.Type type = rules[i];

            int x = left + (i % COLUMNS) * (BUTTON_WIDTH + SPACING);
            int y = top + (i / COLUMNS) * ROW_HEIGHT;

            Button rule = Button.builder(this.ruleLabel(type), button -> {
                QuickVeilSettings.cycle(type);
                button.setMessage(this.ruleLabel(type));
            }).bounds(x, y, BUTTON_WIDTH, ROW_HEIGHT - 1).build();

            this.addRenderableWidget(rule);

            if (type != Modifier.Type.CURSE && type != Modifier.Type.SORCERER) this.options.add(rule);
        }

        int extrasY = top + ((rules.length + COLUMNS - 1) / COLUMNS) * ROW_HEIGHT;

        Button transparent = Button.builder(this.transparentLabel(), button -> {
            QuickVeilSettings.setTransparent(!QuickVeilSettings.isTransparent());
            button.setMessage(this.transparentLabel());
        }).bounds(left, extrasY, BUTTON_WIDTH, ROW_HEIGHT - 1).build();

        this.addRenderableWidget(transparent);

        Button color = Button.builder(this.colorLabel(), button -> {
            QuickVeilSettings.cycleColor();
            button.setMessage(this.colorLabel());
        }).bounds(left + BUTTON_WIDTH + SPACING, extrasY, BUTTON_WIDTH, ROW_HEIGHT - 1).build();

        this.addRenderableWidget(color);
        this.options.add(color);

        int sizeY = extrasY + ROW_HEIGHT;

        this.sizeSlider = new ForgeSlider(left, sizeY, (BUTTON_WIDTH * COLUMNS) + SPACING, ROW_HEIGHT - 1,
                Component.translatable(String.format("gui.%s.veil_configuration.size", JujutsuKaisen.MOD_ID))
                        .copy().append(": "),
                Component.empty(),
                ConfigHolder.SERVER.minimumVeilSize.get(),
                ConfigHolder.SERVER.maximumVeilSize.get(),
                Mth.clamp(QuickVeilSettings.getSize(),
                        ConfigHolder.SERVER.minimumVeilSize.get(),
                        ConfigHolder.SERVER.maximumVeilSize.get()),
                true);

        this.addRenderableWidget(this.sizeSlider);

        if (this.canUseAdmin()) {
            int adminY = sizeY + ROW_HEIGHT;

            this.addRenderableWidget(Button.builder(this.adminLabel(), button -> {
                QuickVeilSettings.setAdmin(!QuickVeilSettings.isAdmin());
                button.setMessage(this.adminLabel());
                this.refreshOptions();
            }).bounds(left, adminY, (BUTTON_WIDTH * COLUMNS) + SPACING, ROW_HEIGHT - 1).build());

            int adminRulesY = adminY + ROW_HEIGHT;

            Modifier.Type[] adminRules = QuickVeilSettings.ADMIN_RULES;

            for (int i = 0; i < adminRules.length; i++) {
                Modifier.Type type = adminRules[i];

                int x = left + (i % COLUMNS) * (BUTTON_WIDTH + SPACING);
                int y = adminRulesY + (i / COLUMNS) * ROW_HEIGHT;

                Button rule = Button.builder(this.ruleLabel(type), button -> {
                    QuickVeilSettings.cycle(type);
                    button.setMessage(this.ruleLabel(type));
                }).bounds(x, y, BUTTON_WIDTH, ROW_HEIGHT - 1).build();

                this.addRenderableWidget(rule);
                this.adminOnly.add(rule);
            }
        }

        this.refreshOptions();
    }

    private void refreshOptions() {
        boolean admin = QuickVeilSettings.isAdmin();

        for (Button option : this.options) {
            option.active = !admin;
        }

        for (Button option : this.adminOnly) {
            option.visible = admin;
        }
    }

    private Component ruleLabel(Modifier.Type type) {
        Modifier.Action action = QuickVeilSettings.get(type);

        Component name = Component.translatable(String.format("gui.%s.veil_configuration.%s",
                JujutsuKaisen.MOD_ID, type.name().toLowerCase()));

        Component value = Component.translatable(String.format("gui.%s.veil_configuration.action.%s",
                JujutsuKaisen.MOD_ID, action.name().toLowerCase()));

        ChatFormatting formatting = switch (action) {
            case ALLOW -> ChatFormatting.GREEN;
            case DENY -> ChatFormatting.RED;
            case NONE -> ChatFormatting.GRAY;
        };
        return Component.empty().append(name).append(": ").append(value.copy().withStyle(formatting));
    }

    private Component transparentLabel() {
        boolean transparent = QuickVeilSettings.isTransparent();

        Component name = Component.translatable(String.format("gui.%s.veil_configuration.transparent", JujutsuKaisen.MOD_ID));

        Component value = Component.translatable(String.format("gui.%s.veil_configuration.action.%s",
                JujutsuKaisen.MOD_ID, transparent ? "allow" : "none"));

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(transparent ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private Component colorLabel() {
        int color = QuickVeilSettings.getColor();

        Component name = Component.translatable(String.format("gui.%s.veil_configuration.color", JujutsuKaisen.MOD_ID));

        Component value = color < 0
                ? Component.translatable(String.format("gui.%s.veil_configuration.action.none", JujutsuKaisen.MOD_ID))
                : Component.literal(DyeColor.values()[color].getName());

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(color < 0 ? ChatFormatting.GRAY : ChatFormatting.AQUA));
    }

    private Component adminLabel() {
        boolean admin = QuickVeilSettings.isAdmin();

        Component name = Component.translatable(String.format("gui.%s.veil_configuration.admin", JujutsuKaisen.MOD_ID));

        Component value = Component.translatable(String.format("gui.%s.veil_configuration.action.%s",
                JujutsuKaisen.MOD_ID, admin ? "allow" : "none"));

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(admin ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
    }
}

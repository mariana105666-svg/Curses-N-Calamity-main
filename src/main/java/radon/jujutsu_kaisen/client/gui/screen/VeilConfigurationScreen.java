package radon.jujutsu_kaisen.client.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.item.veil.modifier.AbilityModifier;
import radon.jujutsu_kaisen.item.veil.modifier.AdminModifier;
import radon.jujutsu_kaisen.item.veil.modifier.DomainModifier;
import radon.jujutsu_kaisen.item.veil.modifier.ColorModifier;
import radon.jujutsu_kaisen.item.veil.modifier.CurseModifier;
import radon.jujutsu_kaisen.item.veil.modifier.CurseSpawnModifier;
import radon.jujutsu_kaisen.item.veil.modifier.GriefingModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.OwnerBypassModifier;
import radon.jujutsu_kaisen.item.veil.modifier.SorcererModifier;
import radon.jujutsu_kaisen.item.veil.modifier.SorcererSpawnModifier;
import radon.jujutsu_kaisen.item.veil.modifier.TeleportModifier;
import radon.jujutsu_kaisen.item.veil.modifier.TransparentModifier;
import radon.jujutsu_kaisen.item.veil.modifier.ViolenceModifier;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.CreateVeilC2SPacket;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VeilConfigurationScreen extends Screen {
    private static final Modifier.Type[] RULES = {
            Modifier.Type.CURSE,
            Modifier.Type.SORCERER,
            Modifier.Type.GRIEFING,
            Modifier.Type.OWNER_BYPASS,
            Modifier.Type.TELEPORT,
            Modifier.Type.CURSE_SPAWN,
            Modifier.Type.SORCERER_SPAWN,
            Modifier.Type.VIOLENCE
    };

    private static final Modifier.Type[] ADMIN_RULES = {
            Modifier.Type.DOMAIN,
            Modifier.Type.ABILITY
    };

    private static final Set<Modifier.Type> ADMIN_KEEPS =
            EnumSet.of(Modifier.Type.CURSE, Modifier.Type.SORCERER,
                    Modifier.Type.DOMAIN, Modifier.Type.ABILITY);

    private static final int BUTTON_WIDTH = 158;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 2;
    private static final int COLUMNS = 2;

    private final Map<Modifier.Type, Modifier.Action> rules = new EnumMap<>(Modifier.Type.class);

    private final List<Button> options = new ArrayList<>();

    private final List<Button> adminOnly = new ArrayList<>();

    private static final int DEFAULT_SIZE = 64;

    private ForgeSlider sizeSlider;

    private boolean admin;

    private boolean transparent;

    private int color = -1;

    public VeilConfigurationScreen() {
        super(Component.translatable(String.format("gui.%s.veil_configuration", JujutsuKaisen.MOD_ID)));

        for (Modifier.Type type : RULES) {
            this.rules.put(type, Modifier.Action.NONE);
        }

        for (Modifier.Type type : ADMIN_RULES) {
            this.rules.put(type, Modifier.Action.NONE);
        }
    }

    private static boolean canUseAdmin() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return false;

        ISorcererData cap = minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasAdminVeil();
    }

    @Override
    protected void init() {
        super.init();

        this.options.clear();
        this.adminOnly.clear();

        int rows = (RULES.length + COLUMNS - 1) / COLUMNS + 1;
        int gridWidth = (BUTTON_WIDTH * COLUMNS) + (SPACING * (COLUMNS - 1));
        int left = (this.width - gridWidth) / 2;
        int top = (this.height - (rows + 1) * (BUTTON_HEIGHT + SPACING)) / 2;

        for (int i = 0; i < RULES.length; i++) {
            Modifier.Type type = RULES[i];

            int x = left + (i % COLUMNS) * (BUTTON_WIDTH + SPACING);
            int y = top + (i / COLUMNS) * (BUTTON_HEIGHT + SPACING);

            Button rule = this.addRenderableWidget(Button.builder(this.ruleLabel(type), button -> {
                this.rules.put(type, cycle(this.rules.get(type)));
                button.setMessage(this.ruleLabel(type));
            }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            if (!ADMIN_KEEPS.contains(type)) this.options.add(rule);
        }

        int extrasY = top + ((RULES.length + COLUMNS - 1) / COLUMNS) * (BUTTON_HEIGHT + SPACING);

        this.addRenderableWidget(Button.builder(this.transparentLabel(), button -> {
            this.transparent = !this.transparent;
            button.setMessage(this.transparentLabel());
        }).bounds(left, extrasY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.options.add(this.addRenderableWidget(Button.builder(this.colorLabel(), button -> {
            this.color = this.color + 1 >= DyeColor.values().length ? -1 : this.color + 1;
            button.setMessage(this.colorLabel());
        }).bounds(left + BUTTON_WIDTH + SPACING, extrasY, BUTTON_WIDTH, BUTTON_HEIGHT).build()));

        int sizeY = extrasY + BUTTON_HEIGHT + SPACING;

        this.sizeSlider = new ForgeSlider(left, sizeY, (BUTTON_WIDTH * COLUMNS) + SPACING, BUTTON_HEIGHT,
                Component.translatable(String.format("gui.%s.veil_configuration.size", JujutsuKaisen.MOD_ID))
                        .copy().append(": "),
                Component.empty(),
                ConfigHolder.SERVER.minimumVeilSize.get(),
                ConfigHolder.SERVER.maximumVeilSize.get(),
                Mth.clamp(DEFAULT_SIZE,
                        ConfigHolder.SERVER.minimumVeilSize.get(),
                        ConfigHolder.SERVER.maximumVeilSize.get()),
                true);

        this.addRenderableWidget(this.sizeSlider);

        int lastY = sizeY;

        if (canUseAdmin()) {
            int adminY = sizeY + BUTTON_HEIGHT + SPACING;

            this.addRenderableWidget(Button.builder(this.adminLabel(), button -> {
                this.admin = !this.admin;
                button.setMessage(this.adminLabel());
                this.refreshOptions();
            }).bounds(left, adminY, (BUTTON_WIDTH * COLUMNS) + SPACING, BUTTON_HEIGHT).build());

            int adminRulesY = adminY + BUTTON_HEIGHT + SPACING;

            for (int i = 0; i < ADMIN_RULES.length; i++) {
                Modifier.Type type = ADMIN_RULES[i];

                int x = left + (i % COLUMNS) * (BUTTON_WIDTH + SPACING);
                int y = adminRulesY + (i / COLUMNS) * (BUTTON_HEIGHT + SPACING);

                this.adminOnly.add(this.addRenderableWidget(Button.builder(this.ruleLabel(type), button -> {
                    this.rules.put(type, cycle(this.rules.get(type)));
                    button.setMessage(this.ruleLabel(type));
                }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()));
            }

            lastY = adminRulesY + (((ADMIN_RULES.length - 1) / COLUMNS) * (BUTTON_HEIGHT + SPACING));
        }

        this.refreshOptions();

        int confirmY = lastY + BUTTON_HEIGHT + SPACING * 3;

        this.addRenderableWidget(Button.builder(
                Component.translatable(String.format("gui.%s.veil_configuration.confirm", JujutsuKaisen.MOD_ID)),
                button -> this.confirm()).bounds(left, confirmY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable(String.format("gui.%s.veil_configuration.cancel", JujutsuKaisen.MOD_ID)),
                button -> this.onClose()).bounds(left + BUTTON_WIDTH + SPACING, confirmY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private static Modifier.Action cycle(Modifier.Action action) {
        return switch (action) {
            case NONE -> Modifier.Action.ALLOW;
            case ALLOW -> Modifier.Action.DENY;
            case DENY -> Modifier.Action.NONE;
        };
    }

    private Component ruleLabel(Modifier.Type type) {
        Modifier.Action action = this.rules.get(type);

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
        Component name = Component.translatable(String.format("gui.%s.veil_configuration.transparent", JujutsuKaisen.MOD_ID));

        Component value = Component.translatable(String.format("gui.%s.veil_configuration.action.%s",
                JujutsuKaisen.MOD_ID, this.transparent ? "allow" : "none"));

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(this.transparent ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private Component colorLabel() {
        Component name = Component.translatable(String.format("gui.%s.veil_configuration.color", JujutsuKaisen.MOD_ID));

        Component value = this.color < 0
                ? Component.translatable(String.format("gui.%s.veil_configuration.action.none", JujutsuKaisen.MOD_ID))
                : Component.literal(DyeColor.values()[this.color].getName());

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(this.color < 0 ? ChatFormatting.GRAY : ChatFormatting.AQUA));
    }

    private void refreshOptions() {
        for (Button option : this.options) {
            option.active = !this.admin;
        }

        for (Button option : this.adminOnly) {
            option.visible = this.admin;
        }
    }

    private Component adminLabel() {
        Component name = Component.translatable(String.format("gui.%s.veil_configuration.admin", JujutsuKaisen.MOD_ID));

        Component value = Component.translatable(String.format("gui.%s.veil_configuration.action.%s",
                JujutsuKaisen.MOD_ID, this.admin ? "allow" : "none"));

        return Component.empty().append(name).append(": ")
                .append(value.copy().withStyle(this.admin ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
    }

    private void confirm() {
        int size = this.sizeSlider.getValueInt();

        if (this.admin) {
            List<Modifier> sealed = new ArrayList<>();
            sealed.add(new AdminModifier(Modifier.Action.ALLOW));

            if (this.transparent) sealed.add(new TransparentModifier(Modifier.Action.ALLOW));

            for (Modifier.Type type : ADMIN_KEEPS) {
                Modifier.Action action = this.rules.get(type);

                if (action == null || action == Modifier.Action.NONE) continue;

                sealed.add(create(type, action));
            }

            PacketHandler.sendToServer(new CreateVeilC2SPacket(sealed, size));

            this.onClose();
            return;
        }

        List<Modifier> modifiers = new ArrayList<>();

        for (Modifier.Type type : RULES) {
            Modifier.Action action = this.rules.get(type);

            if (action == Modifier.Action.NONE) continue;

            modifiers.add(create(type, action));
        }

        if (this.transparent) {
            modifiers.add(new TransparentModifier(Modifier.Action.ALLOW));
        }

        if (this.color >= 0) {
            modifiers.add(new ColorModifier(DyeColor.values()[this.color], Modifier.Action.ALLOW));
        }

        PacketHandler.sendToServer(new CreateVeilC2SPacket(modifiers, size));

        this.onClose();
    }

    private static Modifier create(Modifier.Type type, Modifier.Action action) {
        return switch (type) {
            case CURSE -> new CurseModifier(action);
            case SORCERER -> new SorcererModifier(action);
            case GRIEFING -> new GriefingModifier(action);
            case OWNER_BYPASS -> new OwnerBypassModifier(action);
            case TELEPORT -> new TeleportModifier(action);
            case CURSE_SPAWN -> new CurseSpawnModifier(action);
            case SORCERER_SPAWN -> new SorcererSpawnModifier(action);
            case VIOLENCE -> new ViolenceModifier(action);
            case DOMAIN -> new DomainModifier(action);
            case ABILITY -> new AbilityModifier(action);
            default -> new Modifier(type, action);
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}

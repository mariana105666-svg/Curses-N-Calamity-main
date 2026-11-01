package radon.jujutsu_kaisen.client.gui.screen.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.gui.screen.widget.AbilityWidget;
import radon.jujutsu_kaisen.client.gui.tooltip.NodeTooltip;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;
import radon.jujutsu_kaisen.ability.JJKAbilities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AbilityTab extends JJKTab {
    private static final Component TITLE = Component.translatable(String.format("gui.%s.ability", JujutsuKaisen.MOD_ID));

    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/stone.png");

    private final Map<Ability, AbilityWidget> abilities = new HashMap<>();

    private final List<AbilityWidget> roots = new ArrayList<>();
    private float fade;

    public AbilityTab(Minecraft minecraft, JujutsuScreen screen, JJKTabType type, int index, int page) {
        super(minecraft, screen, type, index, page, Items.ENDER_PEARL.getDefaultInstance(), TITLE, BACKGROUND, true);
        if (this.minecraft.player == null) return;

        for (RegistryObject<Ability> entry : JJKAbilities.ABILITIES.getEntries()) {
            Ability ability = entry.get();

            if (ability.isDisplayed(this.minecraft.player) && ability.getParent(this.minecraft.player) == null) {
                this.addAbilityAndChildren(ability);
            }
        }
    }

    private void addAbilityAndChildren(Ability ability) {
        this.addAbility(ability);

        for (RegistryObject<Ability> entry : JJKAbilities.ABILITIES.getEntries()) {
            Ability current = entry.get();

            if (current.isDisplayed(this.minecraft.player) && current.getParent(this.minecraft.player) == ability) {
                this.addAbilityAndChildren(current);
            }
        }
    }

    @Override
    public void drawContents(GuiGraphics pGuiGraphics, int pX, int pY) {
        JujutsuScreen.scissor(pGuiGraphics, pX, pY, JujutsuScreen.insideWidth(), JujutsuScreen.insideHeight());
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float) pX, (float) pY, 0.0F);

        this.drawSpace(pGuiGraphics);

        if (TREE_VISIBLE) {
            this.drawCustom(pGuiGraphics, Mth.floor(this.scrollX), Mth.floor(this.scrollY));
        }

        this.drawEmblem(pGuiGraphics);

        pGuiGraphics.pose().popPose();
        pGuiGraphics.disableScissor();

    }

    private boolean centred;

    private static final int SKY_TOP = 0xFF05060F;
    private static final int SKY_BOTTOM = 0xFF120E22;

    private static final int STARS = 110;

    private static final boolean TREE_VISIBLE = false;

    private static final float HEX = 17.0F;
    private static final float HEX_EDGE = 1.5F;

    private static final int HEX_BORDER = 0xFF6D5AA8;
    private static final int HEX_FILL = 0xFF0A0818;

    private static final float LABEL_SCALE = 1.0F;
    private static final int LABEL_COLOUR = 0xFFFFFFFF;

    private static final float HEX_HOVER = 1.12F;
    private static final float HEX_EASE = 0.25F;

    private static final float STAT_MAX = ISorcererData.BURNOUT_STAT_MAX;

    private static final java.util.EnumSet<Node> REVERSE_NODES = java.util.EnumSet.of(
            Node.REVERSE_CURSED_TECHNIQUE, Node.RCT_PURIFY, Node.RCT_OUTPUT, Node.RCT_OUTPUT_PURIFY,
            Node.BURNOUT_HEALING, Node.BURNOUT_HEAL_SPEED, Node.BRAIN_RECOVERY);

    public static boolean DEBUG_NODES = false;

    private static final int ICON_SOURCE = 36;
    private static final float ICON = 22.0F;

    private Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> detail() {
        if (this.hovered == null) return Optional.empty();

        return switch (this.hovered.readout) {
            case BAR -> Optional.of(new NodeTooltip(this.hovered.description(), true, this.value(this.hovered.stat), this.hovered.stat.max, this.hovered.stat.label()));
            case NUMBER -> Optional.of(new NodeTooltip(this.hovered.description(), false, this.value(this.hovered.stat), 0.0F, Component.empty()));
            case NONE -> Optional.empty();
        };
    }

    private float value(Stat stat) {
        if (this.minecraft.player == null) return 0.0F;

        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return 0.0F;

        return switch (stat) {
            case EFFICIENCY -> cap.getEfficiency();
            case EFFICIENCY_MODERATE -> cap.getEfficiencyModerate();
            case EFFICIENCY_ADVANCED -> cap.getEfficiencyAdvanced();
            case REVERSE -> cap.getReverseEfficiency();
            case MASTERY -> cap.getTechniqueMastery();
            case SIMPLE_DOMAIN -> cap.getSimpleDomainMastery();
            case REFINEMENT -> cap.getRefinement();
            case BURNOUT_SPEED -> cap.getBurnoutSpeed();
            case BRAIN_RECOVERY -> cap.getBrainRecovery();
            case NONE -> 0.0F;
        };
    }

    private enum Readout {
        NONE,
        BAR,
        NUMBER
    }

    private enum Stat {
        NONE(0.0F, ""),
        EFFICIENCY(ISorcererData.CE_MAX, ""),
        EFFICIENCY_MODERATE(ISorcererData.CE2_MAX, ""),
        EFFICIENCY_ADVANCED(ISorcererData.CE3_MAX, ""),
        REVERSE(ISorcererData.RCT_MAX, ""),
        MASTERY(ISorcererData.TECHNIQUE_MASTERY_MAX, "mastery"),
        SIMPLE_DOMAIN(ISorcererData.SIMPLE_DOMAIN_MAX, "mastery"),
        REFINEMENT(0.0F, ""),
        BURNOUT_SPEED(STAT_MAX, ""),
        BRAIN_RECOVERY(STAT_MAX, "");

        private final float max;
        private final String labelKey;

        Stat(float max, String labelKey) {
            this.max = max;
            this.labelKey = labelKey;
        }

        private Component label() {
            return this.labelKey.isEmpty()
                    ? Component.empty()
                    : Component.translatable(String.format("gui.%s.%s", JujutsuKaisen.MOD_ID, this.labelKey));
        }
    }

    private enum Node {
        CURSED_ENERGY("CE", 0, 0, 1.0F, "cursed_energy_node", "cursed_energy_node", Readout.BAR, Stat.EFFICIENCY, ""),
        CURSED_TECHNIQUE("CT", -76, -52, 1.0F, "cursed_technique_node", "cursed_technique_node", Readout.BAR, Stat.MASTERY, "", 0),
        CURSED_ENERGY_MODERATE("CE2", 76, -52, 1.0F, "cursed_energy_moderate_node", "cursed_energy_node", Readout.BAR, Stat.EFFICIENCY_MODERATE, "", 0),
        REVERSE_CURSED_TECHNIQUE("RCT", 152, -52, 1.0F, "rct_node", "rct_node", Readout.BAR, Stat.REVERSE, "", 2),
        CURSED_ENERGY_ADVANCED("CE3", 76, -104, 1.0F, "cursed_energy_advanced_node", "cursed_energy_node", Readout.BAR, Stat.EFFICIENCY_ADVANCED, "", 2),
        BARRIER_TECHNIQUES("BT", 40, -156, 1.0F, "barrier_node", "barrier_node", Readout.NONE, Stat.NONE, "", 4),
        DOMAIN_EXPANSION("DE", -76, -176, 2.0F, "domain_node", "domain_node", Readout.NONE, Stat.NONE, "", 1),
        DOMAIN_REFINEMENT("DR", -152, -200, 1.0F, "domain_refinement_node", "domain_refinement_node", Readout.NUMBER, Stat.REFINEMENT, "", 6),
        OPEN_DOMAIN("OD", -228, -252, 1.0F, "open_domain_node", "open_domain_node", Readout.NONE, Stat.NONE, "open_domain_node.warning", 7),
        INSTANT_DOMAIN("0.2", -228, -148, 1.0F, "instant_domain_node", "instant_domain_node", Readout.NONE, Stat.NONE, "", 7),
        RCT_PURIFY("P", 228, 0, 1.0F, "rct_purify_node", "rct_purify_node", Readout.NONE, Stat.NONE, "", 3),
        RCT_OUTPUT("RO", 152, 0, 1.0F, "rct_output_node", "rct_output_node", Readout.NONE, Stat.NONE, "", 3),
        RCT_OUTPUT_PURIFY("OP", 190, 52, 1.0F, "rct_output_purify_node", "rct_output_purify_node", Readout.NONE, Stat.NONE, "", 11, 10),
        BURNOUT_HEALING("BH", 228, -52, 1.0F, "burnout_healing_node", "burnout_healing_node", Readout.NONE, Stat.NONE, "", 3),
        BURNOUT_HEAL_SPEED("HS", 304, -104, 1.0F, "burnout_speed_node", "burnout_speed_node", Readout.BAR, Stat.BURNOUT_SPEED, "", 13),
        BRAIN_RECOVERY("BR", 304, 0, 1.0F, "brain_recovery_node", "brain_recovery_node", Readout.BAR, Stat.BRAIN_RECOVERY, "", 13),
        VEIL("V", 116, -156, 1.0F, "veil_node", "veil_node", Readout.NONE, Stat.NONE, "", 5),
        MAHORAGA("", -152, -78, 1.0F, "mahoraga_node", "", Readout.NONE, Stat.NONE, "", "general_wheel", 1),
        MAHORAGA_WHEEL("W", -228, -78, 1.0F, "mahoraga_wheel_node", "mahoraga_wheel_node", Readout.NONE, Stat.NONE, "", 17),
        REVERSE_BARRIER("RBS", -76, -256, 1.0F, "reverse_barrier_node", "reverse_barrier_node", Readout.NONE, Stat.NONE, "", 6),
        SELECTIVE_TARGETING("ST", -76, -328, 1.0F, "selective_targeting_node", "selective_targeting_node", Readout.NONE, Stat.NONE, "", 19),
        FUGA("F", -152, -52, 1.0F, "fuga_node", "", Readout.NONE, Stat.NONE, "", 1),
        SIMPLE_DOMAIN("SD", -76, 0, 1.0F, "simple_domain_node", "simple_domain_node", Readout.BAR, Stat.SIMPLE_DOMAIN, "", 0),
        ENLARGED_SIMPLE_DOMAIN("ESD", -152, -156, 1.0F, "enlarged_simple_domain_node", "", Readout.NONE, Stat.NONE, "", 22),
        QUICK_DRAW("QD", -152, 52, 1.0F, "quick_draw_node", "", Readout.NONE, Stat.NONE, "", 22),
        HOLLOW_WICKER_BASKET("HWB", -152, 0, 1.0F, "hollow_wicker_basket_node", "", Readout.NONE, Stat.NONE, "", 22),
        FALLING_BLOSSOM("FBE", -152, 104, 1.0F, "falling_blossom_node", "", Readout.NONE, Stat.NONE, "", 22),
        MAXIMUM_METEOR("MM", -152, -52, 1.0F, "maximum_meteor_node", "", Readout.NONE, Stat.NONE, "", 1),
        MINI_UZUMAKI("MiniU", -152, -52, 1.0F, "mini_uzumaki_node", "", Readout.NONE, Stat.NONE, "", 1),
        MAXIMUM_UZUMAKI("MU", -228, -52, 1.0F, "maximum_uzumaki_node", "", Readout.NONE, Stat.NONE, "", 28),
        DOMAIN_AMPLIFICATION("DA", 0, -228, 1.0F, "domain_amplification_node", "domain_amplification_node", Readout.NONE, Stat.NONE, "", 6),
        CURSED_ENERGY_BOMB("CEB", -304, -52, 1.0F, "cursed_energy_bomb_node", "", Readout.NONE, Stat.NONE, "", 1),
        CURSED_ENERGY_BLAST("CB", -304, -104, 1.0F, "cursed_energy_blast_node", "", Readout.NONE, Stat.NONE, "", 1),
        INCOMPLETE_DOMAIN("ID", -20, -130, 1.0F, "incomplete_domain_node", "incomplete_domain_node", Readout.NONE, Stat.NONE, "", 6);

        private final String label;
        private final int offsetX;
        private final int offsetY;
        private final float size;
        private final String key;
        private final String descriptionKey;
        private final Readout readout;
        private final Stat stat;
        private final String warningKey;
        private final String icon;
        private final int[] parents;

        Node(String label, int offsetX, int offsetY, float size, String key, String descriptionKey, Readout readout, Stat stat, String warningKey, int... parents) {
            this(label, offsetX, offsetY, size, key, descriptionKey, readout, stat, warningKey, "", parents);
        }

        Node(String label, int offsetX, int offsetY, float size, String key, String descriptionKey, Readout readout, Stat stat, String warningKey, String icon, int... parents) {
            this.label = label;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.size = size;
            this.key = key;
            this.descriptionKey = descriptionKey;
            this.readout = readout;
            this.stat = stat;
            this.warningKey = warningKey;
            this.icon = icon;
            this.parents = parents;
        }

        private float radius() {
            return HEX * this.size;
        }

        private List<Node> parents() {
            List<Node> found = new ArrayList<>();

            for (int index : this.parents) found.add(values()[index]);

            return found;
        }

        @Nullable
        private ResourceLocation texture() {
            return this.icon.isEmpty()
                    ? null
                    : new ResourceLocation(JujutsuKaisen.MOD_ID, String.format("textures/gui/container/%s.png", this.icon));
        }

        private Component title() {
            return Component.translatable(String.format("gui.%s.%s", JujutsuKaisen.MOD_ID, this.key)).withStyle(ChatFormatting.BOLD);
        }

        private Component description() {
            return Component.translatable(String.format("gui.%s.%s.description", JujutsuKaisen.MOD_ID, this.descriptionKey));
        }

        private List<Component> lines() {
            List<Component> lines = new ArrayList<>();

            lines.add(this.title());

            if (this.readout == Readout.NONE && !this.descriptionKey.isEmpty()) {
                lines.add(this.description().copy().withStyle(ChatFormatting.GRAY));
            }

            if (!this.warningKey.isEmpty()) {
                lines.add(Component.translatable(String.format("gui.%s.%s", JujutsuKaisen.MOD_ID, this.warningKey))
                        .withStyle(ChatFormatting.RED));
            }

            return lines;
        }


    }

    private static final int PAN_PAD = 26;

    private float nodeX(Node node) {
        return JujutsuScreen.insideWidth() / 2.0F + node.offsetX + (float) this.scrollX;
    }

    private float nodeY(Node node) {
        return JujutsuScreen.insideHeight() / 2.0F + node.offsetY + (float) this.scrollY;
    }

    private boolean unlocked(Node node) {
        if (DEBUG_NODES) return true;

        if (this.minecraft.player == null) return false;

        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;
        if (cap.hasTrait(radon.jujutsu_kaisen.capability.data.sorcerer.Trait.HEAVENLY_RESTRICTION)) return false;

        if (cap.getType() == radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType.CURSE && REVERSE_NODES.contains(node)) return false;

        if (node == Node.CURSED_ENERGY || node == Node.CURSED_TECHNIQUE) return true;

        return cap.hasNode(node.key);
    }

    private int spanLow(boolean horizontal) {
        int low = 0;

        for (Node node : Node.values()) {
            if (!this.unlocked(node)) continue;

            low = Math.min(low, Math.round((horizontal ? node.offsetX : node.offsetY) - node.radius()));
        }

        return low - PAN_PAD;
    }

    private int spanHigh(boolean horizontal) {
        int high = 0;

        for (Node node : Node.values()) {
            if (!this.unlocked(node)) continue;

            high = Math.max(high, Math.round((horizontal ? node.offsetX : node.offsetY) + node.radius()));
        }

        return high + PAN_PAD;
    }

    @Override
    public void scroll(double dragX, double dragY) {
        this.scrollX = this.clampPan(this.scrollX + dragX, JujutsuScreen.insideWidth(), true);
        this.scrollY = this.clampPan(this.scrollY + dragY, JujutsuScreen.insideHeight(), false);
    }

    private double clampPan(double value, int view, boolean horizontal) {
        int low = this.spanLow(horizontal);
        int high = this.spanHigh(horizontal);

        if (high - low <= view) return 0.0D;

        return Mth.clamp(value, view / 2.0D - high, -low - view / 2.0D);
    }

    private final float[] nodeScale = new float[Node.values().length];

    @Nullable
    private Node hovered;

    private void drawSpace(GuiGraphics graphics) {
        int width = JujutsuScreen.insideWidth();
        int height = JujutsuScreen.insideHeight();

        graphics.fillGradient(0, 0, width, height, SKY_TOP, SKY_BOTTOM);

        int count = STARS * width * height / (JujutsuScreen.WINDOW_INSIDE_WIDTH * JujutsuScreen.WINDOW_INSIDE_HEIGHT);

        for (int i = 0; i < count; i++) {
            int x = scatter(i * 2) % width;
            int y = scatter(i * 2 + 1) % height;

            float depth = (scatter(i * 3) % 100) / 100.0F;

            int px = Math.floorMod(x + Mth.floor(this.scrollX), width);
            int py = Math.floorMod(y + Mth.floor(this.scrollY), height);

            int shade = 0x66 + (int) (depth * 0x99);
            int colour = 0xFF000000 | (shade << 16) | (shade << 8) | 0xFF;

            graphics.fill(px, py, px + 1, py + 1, colour);
        }
    }

    private static int scatter(int index) {
        int h = index * 0x9E3779B1;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return h & 0x7FFFFFFF;
    }

    private static final int STRAND_STEPS = 28;

    private static final float STRAND_END = 3.2F;
    private static final float STRAND_WAIST = 0.35F;
    private static final float STRAND_FLARE = 3.0F;

    private static final float STRAND_R = 0.44F;
    private static final float STRAND_G = 0.37F;
    private static final float STRAND_B = 0.72F;

    private void drawStrands(GuiGraphics graphics) {
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix = graphics.pose().last().pose();

        for (Node node : Node.values()) {
            if (!this.unlocked(node)) continue;

            for (Node parent : node.parents()) {
                if (!this.unlocked(parent)) continue;

                float ax = this.nodeX(parent);
                float ay = this.nodeY(parent);
                float bx = this.nodeX(node);
                float by = this.nodeY(node);

                float dx = bx - ax;
                float dy = by - ay;

                float length = Mth.sqrt(dx * dx + dy * dy);

                if (length <= 0.0F) continue;

                float ux = dx / length;
                float uy = dy / length;

                float px = -uy;
                float py = ux;

                for (int i = 0; i < STRAND_STEPS; i++) {
                    float t0 = (float) i / STRAND_STEPS;
                    float t1 = (float) (i + 1) / STRAND_STEPS;

                    float w0 = strandWidth(t0);
                    float w1 = strandWidth(t1);

                    float a0 = strandAlpha(t0);
                    float a1 = strandAlpha(t1);

                    float x0 = ax + ux * length * t0;
                    float y0 = ay + uy * length * t0;
                    float x1 = ax + ux * length * t1;
                    float y1 = ay + uy * length * t1;

                    vertex(buffer, matrix, x0 + px * w0, y0 + py * w0, a0);
                    vertex(buffer, matrix, x1 + px * w1, y1 + py * w1, a1);
                    vertex(buffer, matrix, x1 - px * w1, y1 - py * w1, a1);
                    vertex(buffer, matrix, x0 - px * w0, y0 - py * w0, a0);
                }
            }
        }

        graphics.flush();
    }

    private static float strandWidth(float t) {
        float toward = Math.abs(t - 0.5F) * 2.0F;

        return STRAND_WAIST + (STRAND_END - STRAND_WAIST) * (float) Math.pow(toward, STRAND_FLARE);
    }

    private static float strandAlpha(float t) {
        float toward = Math.abs(t - 0.5F) * 2.0F;

        return 0.30F + 0.55F * toward * toward;
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float alpha) {
        buffer.vertex(matrix, x, y, 0.0F).color(STRAND_R, STRAND_G, STRAND_B, alpha).endVertex();
    }

    private void drawEmblem(GuiGraphics graphics) {
        Font font = this.minecraft.font;

        this.drawStrands(graphics);

        for (Node node : Node.values()) {
            if (!this.unlocked(node)) continue;

            int index = node.ordinal();

            if (this.nodeScale[index] <= 0.0F) this.nodeScale[index] = 1.0F;

            this.nodeScale[index] += ((this.hovered == node ? HEX_HOVER : 1.0F) - this.nodeScale[index]) * HEX_EASE;

            float scale = this.nodeScale[index];

            float cx = this.nodeX(node);
            float cy = this.nodeY(node);

            float radius = node.radius() * scale;

            hexagon(graphics, cx, cy, radius, HEX_BORDER);
            hexagon(graphics, cx, cy, radius - HEX_EDGE * node.size * scale, HEX_FILL);

            ResourceLocation texture = node.texture();

            if (texture != null) {
                float span = ICON * node.size * scale;

                graphics.pose().pushPose();
                graphics.pose().translate(cx, cy, 0.0F);
                graphics.pose().scale(span / ICON_SOURCE, span / ICON_SOURCE, 1.0F);
                graphics.blit(texture, -ICON_SOURCE / 2, -ICON_SOURCE / 2, 0, 0.0F, 0.0F,
                        ICON_SOURCE, ICON_SOURCE, ICON_SOURCE, ICON_SOURCE);
                graphics.pose().popPose();

                continue;
            }

            Component label = Component.literal(node.label).withStyle(ChatFormatting.BOLD);

            int width = font.width(label);

            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy, 0.0F);
            graphics.pose().scale(LABEL_SCALE * node.size * scale, LABEL_SCALE * node.size * scale, 1.0F);
            graphics.drawString(font, label, -width / 2, -font.lineHeight / 2, LABEL_COLOUR, false);
            graphics.pose().popPose();
        }
    }

    @Nullable
    private Node nodeAt(double x, double y) {
        for (Node node : Node.values()) {
            if (!this.unlocked(node)) continue;

            float radius = node.radius() * Math.max(1.0F, this.nodeScale[node.ordinal()]);
            float half = radius * 0.8660254F;

            double dx = Math.abs(x - this.nodeX(node));
            double dy = Math.abs(y - this.nodeY(node));

            if (dy > half) continue;

            if (dx <= radius * (1.0D - dy / (2.0D * half))) return node;
        }

        return null;
    }

    private static void hexagon(GuiGraphics graphics, float cx, float cy, float radius, int colour) {
        float half = radius * 0.8660254F;

        int top = Mth.ceil(cy - half);
        int bottom = Mth.floor(cy + half);

        for (int y = top; y < bottom; y++) {
            float dy = Math.abs(y + 0.5F - cy);
            float reach = radius * (1.0F - dy / (2.0F * half));

            graphics.fill(Math.round(cx - reach), y, Math.round(cx + reach), y + 1, colour);
        }
    }

    @Override
    protected void drawCustom(GuiGraphics graphics, int x, int y) {
        for (AbilityWidget root : this.roots) {
            root.drawConnectivity(graphics, x, y, true);
            root.drawConnectivity(graphics, x, y, false);
            root.draw(graphics, x, y);
        }
    }

    @Nullable
    public AbilityWidget getAbility(Ability ability) {
        return this.abilities.get(ability);
    }

    @Override
    public void drawTooltips(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
        super.drawTooltips(pGuiGraphics, pMouseX, pMouseY, pOffsetX, pOffsetY);

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float) (pOffsetX + 9), (float) (pOffsetY + 18), 400.0F);
        RenderSystem.enableDepthTest();

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0.0D, 0.0D, -200.0D);
        pGuiGraphics.fill(0, 0, JujutsuScreen.WINDOW_INSIDE_WIDTH, JujutsuScreen.WINDOW_INSIDE_HEIGHT, Mth.floor(this.fade * 255.0F) << 24);
        boolean hovered = false;
        int i = Mth.floor(this.scrollX);
        int j = Mth.floor(this.scrollY);

        if (TREE_VISIBLE && pMouseX - pOffsetX - 9 > 0 && pMouseX - pOffsetX - 9 < JujutsuScreen.WINDOW_INSIDE_WIDTH && pMouseY - pOffsetY - 18 > 0 && pMouseY - pOffsetY - 18 < JujutsuScreen.WINDOW_INSIDE_HEIGHT) {
            for (AbilityWidget widget : this.abilities.values()) {
                if (widget.isMouseOver(i, j, pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18)) {
                    hovered = true;
                    widget.drawHover(pGuiGraphics, i, j, this.fade, pOffsetX, pOffsetY);
                    break;
                }
            }
        }
        pGuiGraphics.pose().popPose();

        if (hovered) {
            this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
        } else {
            this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
        }
        RenderSystem.disableDepthTest();
        pGuiGraphics.pose().popPose();

        this.hovered = this.nodeAt(pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18);

        if (this.hovered != null) {
            pGuiGraphics.renderTooltip(this.minecraft.font, this.hovered.lines(), this.detail(), pMouseX, pMouseY);
        }
    }

    public void addAbility(Ability ability) {
        AbilityWidget widget = new AbilityWidget(this, this.minecraft, ability);

        if (ability.getParent(this.minecraft.player) == null) {
            this.roots.add(widget);
        }

        this.abilities.put(ability, widget);

        int i = widget.getX();
        int j = i + 28;
        int k = widget.getY();
        int l = k + 27;

        this.minX = Math.min(this.minX, i);
        this.maxX = Math.max(this.maxX, j);
        this.minY = Math.min(this.minY, k);
        this.maxY = Math.max(this.maxY, l);

        for (AbilityWidget child : this.abilities.values()) {
            child.attachToParent();
        }
    }

    @Override
    public void addWidgets() {

    }

    @Override
    public void mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int i = Mth.floor(this.scrollX);
        int j = Mth.floor(this.scrollY);

        if (TREE_VISIBLE && pMouseX > 0 && pMouseX < JujutsuScreen.WINDOW_INSIDE_WIDTH && pMouseY > 0 && pMouseY < JujutsuScreen.WINDOW_INSIDE_HEIGHT) {
            for (AbilityWidget widget : this.abilities.values()) {
                if (widget.isMouseOver(i, j, (int) pMouseX, (int) pMouseY)) {
                    widget.unlock();
                    break;
                }
            }
        }
    }
}
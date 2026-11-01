package radon.jujutsu_kaisen.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Vector3f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.gui.OverlayPosition;
import radon.jujutsu_kaisen.config.ConfigHolder;

import java.util.ArrayList;
import java.util.List;

public class CursedEnergyOverlay {
    public static ResourceLocation BAR = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/gui/overlay/energy_bar.png");

    public static ResourceLocation BLACK_FLASH_BAR = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/gui/overlay/blackflash_bar.png");

    public static ResourceLocation BACKGROUND = new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/gui/overlay/energy_bar_background.png");

    private static final int BAR_WIDTH = 236;

    private static final int BAR_HEIGHT = 42;

    private static final float BAR_SCALE = 0.5F;

    private static final int FILL_TRIM = 1;

    private static final int BOTTOM_MARGIN = 8;

    private static final int SIDE_MARGIN = 20;

    private static final int LABEL_GAP = 6;

    private static final int READOUT_INSET = 29;

    private static final int DRAWN_WIDTH = Math.round(BAR_WIDTH * BAR_SCALE);
    private static final int DRAWN_HEIGHT = Math.round(BAR_HEIGHT * BAR_SCALE);

    private static final float SCALE = 0.6F;

    public static IGuiOverlay OVERLAY = (gui, graphics, partialTicks, width, height) -> {
        Minecraft mc = gui.getMinecraft();

        if (mc.player == null) return;

        if (!mc.player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
        ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, SCALE);

        List<Component> above = new ArrayList<>();
        above.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.output", JujutsuKaisen.MOD_ID), Math.round(cap.getOutput() * 100)));
        above.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.experience", JujutsuKaisen.MOD_ID), cap.getExperience()));

        if (cap.hasTechnique(CursedTechnique.IDLE_TRANSFIGURATION)) {
            above.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.transfigured_souls", JujutsuKaisen.MOD_ID), cap.getTransfiguredSouls()));
        }

        List<Component> below = new ArrayList<>();

        int burnout = cap.getBurnout();

        if (burnout > 0) {
            below.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.burnout", JujutsuKaisen.MOD_ID), Math.round((float) burnout / 20)).withStyle(ChatFormatting.DARK_RED));
        }

        for (Ability ability : cap.getToggled()) {
            if (!(ability instanceof Ability.IAttack)) continue;

            int cooldown = cap.getRemainingCooldown(ability);

            if (cooldown > 0) {
                below.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.cooldown", JujutsuKaisen.MOD_ID), ability.getName(), Math.round((float) cooldown / 20)));
            }
        }

        Ability channeled = cap.getChanneled();

        if (channeled instanceof Ability.IAttack) {
            int cooldown = cap.getRemainingCooldown(channeled);

            if (cooldown > 0) {
                below.add(Component.translatable(String.format("gui.%s.cursed_energy_overlay.cooldown", JujutsuKaisen.MOD_ID), channeled.getName(), Math.round((float) cooldown / 20)));
            }
        }

        int lineHeight = mc.font.lineHeight;

        int total = DRAWN_HEIGHT + (below.isEmpty() ? 0 : lineHeight + below.size() * lineHeight);

        OverlayPosition anchor = ConfigHolder.CLIENT.overlayPosition.get();

        int labelWidth = 0;

        for (Component line : above) {
            labelWidth = Math.max(labelWidth, Math.round(mc.font.width(line) * SCALE));
        }

        int blockWidth = DRAWN_WIDTH + LABEL_GAP + labelWidth;

        int originX = anchor.isRight() ? width - SIDE_MARGIN - blockWidth : SIDE_MARGIN;

        int aboveY = anchor.isTop() ? BOTTOM_MARGIN : height - BOTTOM_MARGIN - total;

        int textX = originX + DRAWN_WIDTH + LABEL_GAP;
        int textY = aboveY + Math.max(0, (DRAWN_HEIGHT - above.size() * (lineHeight - 1)) / 2);

        for (Component line : above) {
            graphics.drawString(gui.getFont(), line, Math.round(textX * (1.0F / SCALE)), Math.round(textY * (1.0F / SCALE)), 16777215);
            textY += mc.font.lineHeight - 1;
        }
        graphics.pose().popPose();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        graphics.pose().pushPose();
        graphics.pose().scale(BAR_SCALE, BAR_SCALE, 1.0F);

        int barX = Math.round(originX / BAR_SCALE);
        int barY = Math.round(aboveY / BAR_SCALE);

        if (cap.getEnergy() > 0.0F) {
            float fraction = Math.min(1.0F, cap.getEnergy() / cap.getMaxEnergy());

            int filled = Math.min(BAR_WIDTH - FILL_TRIM, Math.round(fraction * BAR_WIDTH));

            if (filled > 0) {
                ResourceLocation bar = cap.isInZone()
                        ? BLACK_FLASH_BAR
                        : CursedEnergyBarTexture.get(cap.getCursedEnergyColor());

                graphics.blit(bar, barX, barY, 0, 0,
                        filled, BAR_HEIGHT - FILL_TRIM, BAR_WIDTH, BAR_HEIGHT);
            }
        }

        graphics.blit(BACKGROUND, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, SCALE);

        if (cap.getEnergy() > 0.0F) {
            float readoutY = aboveY + (DRAWN_HEIGHT - mc.font.lineHeight * SCALE) / 2.0F;

            graphics.drawString(gui.getFont(), String.format("%.1f / %.1f", cap.getEnergy(), cap.getMaxEnergy()),
                    Math.round((originX + READOUT_INSET - SIDE_MARGIN) * (1.0F / SCALE)), Math.round(readoutY * (1.0F / SCALE)), 16777215);
        }

        aboveY += DRAWN_HEIGHT;

        int belowY = aboveY + mc.font.lineHeight;

        for (Component line : below) {
            graphics.drawString(gui.getFont(), line, Math.round(originX * (1.0F / SCALE)), Math.round(belowY * (1.0F / SCALE)), 16777215);
            belowY += mc.font.lineHeight;
        }

        graphics.pose().popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    };
}

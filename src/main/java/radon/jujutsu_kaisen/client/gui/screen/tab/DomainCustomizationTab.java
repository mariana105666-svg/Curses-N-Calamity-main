package radon.jujutsu_kaisen.client.gui.screen.tab;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import cpw.mods.modlauncher.api.INameMappingService.Domain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.DomainConfiguration;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ValueType;
import radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen;
import radon.jujutsu_kaisen.client.gui.screen.widget.BindingVowListWidget;
import radon.jujutsu_kaisen.client.gui.screen.widget.DomainConfigurationListWidget;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.AddBindingVowC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.RemoveBindingVowC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.SetDomainConfigurationC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.SetDomainSizeC2SPacket;

public class DomainCustomizationTab extends JJKTab {
    private static final Component TITLE = Component.translatable(String.format("gui.%s.domain_customization", JujutsuKaisen.MOD_ID));
    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/stone.png");

    
    private float oldSize;

    private Button enable;
    private Button disable;

    private MultiLineTextWidget description;

    @Nullable
    private DomainConfigurationListWidget.Entry config;
    private final Map<DomainConfiguration, ForgeSlider> sliders = new EnumMap<>(DomainConfiguration.class);



    public DomainCustomizationTab(Minecraft minecraft, JujutsuScreen screen, JJKTabType type, int index, int page) {
        super(minecraft, screen, type, index, page, Items.NETHER_STAR.getDefaultInstance(), TITLE, BACKGROUND, false);
    }

    @Override
    public void tick() {
        if (this.config == null) return;
        if (this.config.get().getType() != ValueType.FLOAT) return;
        ForgeSlider slider = sliders.get(this.config.get());
        if (slider == null) return;
        float size = (float) slider.getValue();

        if (size != this.oldSize) {
            if (this.minecraft != null && this.minecraft.player != null && this.config != null && this.config.get().getType() == ValueType.FLOAT) {
                ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                PacketHandler.sendToServer(new SetDomainConfigurationC2SPacket(this.config.get(), size));
                cap.setDomainConfig(this.config.get(), size);
                //cap.setDomainSize(size);
            }
            this.oldSize = size;
        }
    }

    @Override
    public void drawContents(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.drawContents(pGuiGraphics, pX, pY);

        int i = (this.screen.width - JujutsuScreen.WINDOW_WIDTH) / 2;
        int j = (this.screen.height - JujutsuScreen.WINDOW_HEIGHT) / 2;

        int xOffset = i + (JujutsuScreen.WINDOW_WIDTH - JujutsuScreen.WINDOW_INSIDE_WIDTH);
        int yOffset = j + (JujutsuScreen.WINDOW_HEIGHT - JujutsuScreen.WINDOW_INSIDE_HEIGHT);

        pGuiGraphics.drawString(this.minecraft.font, Component.translatable(String.format("gui.%s.domain_customization.configurations", JujutsuKaisen.MOD_ID)),
                xOffset, yOffset, 16777215, true);
        if (this.config == null || this.config.get().getType() == ValueType.BOOLEAN ) return; 
        pGuiGraphics.drawString(this.minecraft.font, Component.translatable(String.format("domain_configuration.%s.%s", JujutsuKaisen.MOD_ID, this.config.get().getRawName())),
                xOffset + 86, yOffset + this.minecraft.font.lineHeight + 58, 16777215, true);

        }

    private void refreshToggleButtons(@Nullable ISorcererData cap) {
        if (cap == null || this.config == null) return;

        if (this.config.get().getType() == ValueType.BOOLEAN) {
            boolean value = cap.getDomainConfig(this.config.get());

            this.enable.active = !value;
            this.disable.active = value;
        } else {
            this.enable.active = false;
            this.disable.active = false;
        }
    }

    public void setSelectedDomainConfiguration(DomainConfigurationListWidget.Entry entry) {
        this.config = entry;   
        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        if (this.config.get().getType() == ValueType.BOOLEAN) {
            this.enable.visible = true;
            this.disable.visible = true;
            this.enable.setMessage( Component.translatable(
            "domain_configuration.%s.%s.add".formatted(
                JujutsuKaisen.MOD_ID,
                this.config.get().getRawName()
            )));
            this.disable.setMessage( Component.translatable(
            "domain_configuration.%s.%s.remove".formatted(
                JujutsuKaisen.MOD_ID,
                this.config.get().getRawName()
            )));
            this.refreshToggleButtons(cap);
             for (ForgeSlider slider : sliders.values()) {
                slider.visible = false;
                slider.active = false;
            }
        }
        else if (this.config.get().getType() == ValueType.FLOAT) {
            this.oldSize = Float.NaN;
            this.enable.visible = false;
            this.disable.visible = false;

            for (ForgeSlider slider : sliders.values()) {
                slider.visible = false;
                slider.active = false;
            }

            DomainConfiguration cfg = this.config.get();
            ForgeSlider slider = sliders.get(cfg);

            if (slider != null) {
                slider.visible = true;
                slider.active = true;
            }
        }
        this.description.setMessage(entry.get().getDescription());
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildDomainConfigurationList(Consumer<T> consumer, Function<DomainConfiguration, T> result) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        for (DomainConfiguration config : DomainConfiguration.values()) {
            if (!config.isAvailable(cap)) continue;

            consumer.accept(result.apply(config));
        }
    }

    @Override
    public void addWidgets() {
        if (this.minecraft == null || this.minecraft.player == null) return;
         int i = (this.screen.width - JujutsuScreen.WINDOW_WIDTH) / 2;
        int j = (this.screen.height - JujutsuScreen.WINDOW_HEIGHT) / 2;

        int xOffset = i + (JujutsuScreen.WINDOW_WIDTH - JujutsuScreen.WINDOW_INSIDE_WIDTH);
        int yOffset = j + (JujutsuScreen.WINDOW_HEIGHT - JujutsuScreen.WINDOW_INSIDE_HEIGHT);
         ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
        if (cap == null) return;
         this.addRenderableWidget(new DomainConfigurationListWidget(this::buildDomainConfigurationList, this::setSelectedDomainConfiguration, this.minecraft, 78, 85,
                xOffset, yOffset + this.minecraft.font.lineHeight + 1));
         this.enable = Button.builder(Component.translatable(String.format("gui.%s.binding_vow.add", JujutsuKaisen.MOD_ID)), pButton -> {
            if (this.config == null) return;

            PacketHandler.sendToServer(new SetDomainConfigurationC2SPacket(this.config.get(), true));
            cap.setDomainConfig(this.config.get(), true);

            this.refreshToggleButtons(cap);
        }).size(61, 20).pos(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 66).build();
        this.addRenderableWidget(this.enable);

        this.disable = Button.builder(Component.translatable(String.format("gui.%s.binding_vow.remove", JujutsuKaisen.MOD_ID)), pButton -> {
            if (this.config == null) return;

            PacketHandler.sendToServer(new SetDomainConfigurationC2SPacket(this.config.get(), false));
            cap.setDomainConfig(this.config.get(), false);

            this.refreshToggleButtons(cap);
        }).size(61, 20).pos(xOffset + 155, yOffset + this.minecraft.font.lineHeight + 66).build();
        
        this.disable.visible = false;
        this.disable.active = false;
        this.enable.visible = false;
        this.enable.active = false;

        this.addRenderableWidget(this.disable);
        for (DomainConfiguration cfg : DomainConfiguration.values()) {
        if (cfg.getType() != ValueType.FLOAT) continue;

        float min = (float) cfg.getMinimumValue();
        float max = (float) cfg.getMaximumValue();
        float value = (float) cap.getDomainConfig(cfg);

        ForgeSlider slider = new ForgeSlider(
            xOffset + 86,
            yOffset + this.minecraft.font.lineHeight + 68,
            130,
            16,
            Component.empty(),
            Component.empty(),
            min,
            max,
            value,
            0.1D,
            0,
            true
        );

        slider.visible = false;
        slider.active = false;

        sliders.put(cfg, slider);
        this.addRenderableWidget(slider);
    }
        //this.sizeSlider = new ForgeSlider(i + ((JujutsuScreen.WINDOW_WIDTH + 40) / 2), j + ((JujutsuScreen.WINDOW_HEIGHT - 8) / 2), 160, 16, Component.empty(), Component.empty(),
                    //ConfigHolder.SERVER.minimumDomainSize.get().floatValue(), ConfigHolder.SERVER.maximumDomainSize.get().floatValue(), cap.getDomainSize(), 0.1D, 0, true);

    // this.sizeSlider = new ForgeSlider(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 68, 130, 16, Component.empty(), Component.empty(),
    //         0.0F, 1.0F, cap.getDomainSize(),0.1D,0, true
    // );
    //         this.sizeSlider.visible = false;
    //     this.sizeSlider.active = false;
    // this.addRenderableWidget(this.sizeSlider);
    //  this.addRenderableWidget(this.sizeSlider);
        this.description = new MultiLineTextWidget(xOffset + 86, yOffset + this.minecraft.font.lineHeight + 1, Component.empty(), this.minecraft.font)
                .setMaxWidth(129);
        this.addRenderableWidget(this.description);     
    }
}

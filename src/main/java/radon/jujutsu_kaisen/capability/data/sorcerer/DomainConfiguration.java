package radon.jujutsu_kaisen.capability.data.sorcerer;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.config.ConfigHolder;

public enum DomainConfiguration {
    SIZE(ValueType.FLOAT, 1.0f, ConfigHolder.SERVER.minimumDomainSize.get().floatValue(), ConfigHolder.SERVER.maximumDomainSize.get().floatValue(), ""),
    SHELL_BALANCE(ValueType.BOOLEAN, false, ISorcererData.REVERSE_BARRIER_NODE),
    SURE_HIT_TOGGLE(ValueType.BOOLEAN, true, ISorcererData.SELECTIVE_TARGETING_NODE),
    SURE_HIT_ALLIES(ValueType.BOOLEAN, true, ISorcererData.SELECTIVE_TARGETING_NODE),
    OPEN_BARRIER(ValueType.BOOLEAN, false, ""),
    OPEN_DOMAIN_SIZE(ValueType.FLOAT, 1.0f, ConfigHolder.SERVER.minimumDomainSize.get().floatValue(), 1.0f, ISorcererData.OPEN_DOMAIN_NODE),
    OPEN_DOMAIN(ValueType.BOOLEAN, false, ISorcererData.OPEN_DOMAIN_NODE);


    private final ValueType type;
    private final Object defaultValue;
    private final Object minimumValue;
    private final Object maximumValue;
    private final String node;

    DomainConfiguration(ValueType type, Object defaultValue, Object minimumValue, Object maximumValue, String node) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.node = node;
    }

    DomainConfiguration(ValueType type, Object defaultValue, String node) {
        this(type, defaultValue, null, null, node);
    }

    public boolean isAvailable(ISorcererData cap) {
        if (this == OPEN_BARRIER) return false;
        if (this == SIZE) return !cap.isOpenDomain();
        if (this == OPEN_DOMAIN_SIZE) return cap.isOpenDomain();

        return this.node.isEmpty() || cap.hasNode(this.node);
    }

    public ValueType getType() {
        return type;
    }

    public <T> T 
    getDefault() {
        return (T) defaultValue;
    }

    public Object getMinimumValue() {
        return minimumValue;
    }

    public Object getMaximumValue() {
        return maximumValue;
    }

    public String getRawName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component getName() {
        return Component.translatable("domain_configuration.%s.%s".formatted(
                JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }

    public Component getDescription() {
        return Component.translatable("domain_configuration.%s.%s.description".formatted(
                JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }
}

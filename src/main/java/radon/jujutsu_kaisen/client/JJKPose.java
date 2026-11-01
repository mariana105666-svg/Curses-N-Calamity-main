package radon.jujutsu_kaisen.client;

import net.minecraft.resources.ResourceLocation;

public final class JJKPose {
    private final ResourceLocation id;
    private final PoseDefinition definition;
    private final int priority;
    private final int defaultDuration;
    private final boolean autocancel;

    public JJKPose(
        ResourceLocation id,
        PoseDefinition definition,
        int priority,
        int defaultDuration,
        boolean autocancel
    ) {
        this.id = id;
        this.definition = definition;
        this.priority = priority;
        this.defaultDuration = defaultDuration;
        this.autocancel = autocancel;
    }

    public ResourceLocation id() { return id; }
    public PoseDefinition definition() { return definition; }
    public int priority() { return priority; }
    public int defaultDuration() { return defaultDuration; }
    public boolean autocancel() { return autocancel; }
}

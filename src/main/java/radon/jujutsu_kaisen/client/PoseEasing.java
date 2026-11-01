package radon.jujutsu_kaisen.client;

import net.minecraft.util.Mth;

public enum PoseEasing {
    LINEAR {
        @Override public float apply(float t) { return t; }
    },
    EASE_IN {
        @Override public float apply(float t) { return t * t; }
    },
    EASE_OUT {
        @Override public float apply(float t) {
            return 1f - (1f - t) * (1f - t);
        }
    },
    EASE_IN_OUT {
        @Override public float apply(float t) {
            return t < 0.5f
                ? 2f * t * t
                : 1f - (float)Math.pow(-2f * t + 2f, 2f) / 2f;
        }
    };

    public abstract float apply(float t);
}

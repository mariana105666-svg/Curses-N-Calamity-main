package radon.jujutsu_kaisen.puppet;

public enum TunnelPattern {
    SINGLE(1, 2, 0, 0),
    DOUBLE(2, 2, 0, 0),
    TRIPLE(3, 3, -1, -1);

    public static final int LENGTH = 30;

    private final int width;
    private final int height;
    private final int sideOffset;
    private final int heightOffset;

    TunnelPattern(int width, int height, int sideOffset, int heightOffset) {
        this.width = width;
        this.height = height;
        this.sideOffset = sideOffset;
        this.heightOffset = heightOffset;
    }

    public int getSideOffset() {
        return this.sideOffset;
    }

    public int getHeightOffset() {
        return this.heightOffset;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public TunnelPattern next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}

package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class TunnelShape {
    private TunnelShape() {
    }

    public static List<BlockPos> slice(BlockPos origin, Direction facing, TunnelPattern pattern, int step) {
        List<BlockPos> positions = new ArrayList<>();

        Direction side = facing.getClockWise();

        int width = pattern.getWidth();
        int height = pattern.getHeight();

        int offset = pattern.getSideOffset();
        int base = pattern.getHeightOffset();

        BlockPos head = origin.relative(facing, step);

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                positions.add(head.relative(side, offset + w).above(base + h));
            }
        }
        return positions;
    }

    public static List<BlockPos> preview(BlockPos origin, Direction facing, TunnelPattern pattern) {
        return slice(origin, facing, pattern, 0);
    }
}

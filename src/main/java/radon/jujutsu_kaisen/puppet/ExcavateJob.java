package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExcavateJob {
    public static final int MAX_VOLUME = 8192;

    private final BlockPos min;
    private final BlockPos max;
    private final List<UUID> puppets;

    private final Map<BlockPos, Float> progress = new HashMap<>();
    private final Map<UUID, BlockPos> claims = new HashMap<>();

    private int layer;

    public ExcavateJob(BlockPos min, BlockPos max, List<UUID> puppets) {
        this.min = min;
        this.max = max;
        this.puppets = puppets;

        this.layer = max.getY();
    }

    public static int volume(BlockPos a, BlockPos b) {
        int x = Math.abs(a.getX() - b.getX()) + 1;
        int y = Math.abs(a.getY() - b.getY()) + 1;
        int z = Math.abs(a.getZ() - b.getZ()) + 1;

        return x * y * z;
    }

    public BlockPos getMin() {
        return this.min;
    }

    public BlockPos getMax() {
        return this.max;
    }

    public List<UUID> getPuppets() {
        return this.puppets;
    }

    public List<BlockPos> current() {
        List<BlockPos> positions = new ArrayList<>();

        for (int x = this.min.getX(); x <= this.max.getX(); x++) {
            for (int z = this.min.getZ(); z <= this.max.getZ(); z++) {
                positions.add(new BlockPos(x, this.layer, z));
            }
        }
        return positions;
    }

    public BlockPos approach(BlockPos target) {
        return target.above();
    }

    @Nullable
    public BlockPos getClaim(UUID id) {
        return this.claims.get(id);
    }

    public void claim(UUID id, BlockPos pos) {
        this.claims.put(id, pos.immutable());
    }

    public boolean isClaimed(BlockPos pos, UUID by) {
        for (Map.Entry<UUID, BlockPos> entry : this.claims.entrySet()) {
            if (entry.getKey().equals(by)) continue;
            if (pos.equals(entry.getValue())) return true;
        }
        return false;
    }

    public float addProgress(BlockPos pos, float amount) {
        return this.progress.merge(pos.immutable(), amount, Float::sum);
    }

    public void step() {
        this.layer--;
        this.progress.clear();
        this.claims.clear();
    }

    public boolean isDone() {
        return this.layer < this.min.getY();
    }
}

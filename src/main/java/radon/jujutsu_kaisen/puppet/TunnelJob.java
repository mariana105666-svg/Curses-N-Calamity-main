package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TunnelJob {
    private final BlockPos origin;
    private final Direction facing;
    private final TunnelPattern pattern;
    private final List<UUID> puppets;

    private final Map<BlockPos, Float> progress = new HashMap<>();
    private final Map<UUID, BlockPos> claims = new HashMap<>();

    private int step;

    public TunnelJob(BlockPos origin, Direction facing, TunnelPattern pattern, List<UUID> puppets) {
        this.origin = origin;
        this.facing = facing;
        this.pattern = pattern;
        this.puppets = puppets;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public List<UUID> getPuppets() {
        return this.puppets;
    }

    public List<BlockPos> current() {
        return TunnelShape.slice(this.origin, this.facing, this.pattern, this.step);
    }

    private int floor() {
        return this.origin.getY() + this.pattern.getHeightOffset();
    }

    public BlockPos stand() {
        BlockPos head = this.origin.relative(this.facing, this.step - 1);

        return new BlockPos(head.getX(), this.floor(), head.getZ());
    }

    public BlockPos approach(BlockPos target) {
        return new BlockPos(target.getX(), this.floor(), target.getZ()).relative(this.facing.getOpposite());
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
        this.step++;
        this.progress.clear();
        this.claims.clear();
    }

    public boolean isDone() {
        return this.step >= TunnelPattern.LENGTH;
    }
}

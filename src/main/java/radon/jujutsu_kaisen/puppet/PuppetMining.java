package radon.jujutsu_kaisen.puppet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.util.HelperMethods;

public final class PuppetMining {
    public static final float MINE_SPEED = 1.5F;

    private static final ItemStack PICK = Items.STONE_PICKAXE.getDefaultInstance();

    private PuppetMining() {
    }

    public static float speed(BlockState state, ServerLevel level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);

        if (hardness < 0.0F) return 0.0F;
        if (hardness == 0.0F) return 1.0F;

        boolean correct = !state.requiresCorrectToolForDrops() || PICK.isCorrectToolForDrops(state);

        return PICK.getDestroySpeed(state) / hardness / (correct ? 30.0F : 100.0F);
    }

    public static boolean canMine(ServerLevel level, @Nullable ServerPlayer owner, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        if (owner != null && !HelperMethods.isDestroyable(level, owner, pos)) return false;

        return !state.requiresCorrectToolForDrops() || PICK.isCorrectToolForDrops(state);
    }
}

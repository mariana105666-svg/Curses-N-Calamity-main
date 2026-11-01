package radon.jujutsu_kaisen.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.VeilHandler;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void jujutsu_kaisen$veil(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        if (jujutsu_kaisen$protected(pLevel, pPos)) {
            ci.cancel();
            return;
        }

        for (Direction direction : Direction.values()) {
            if (!jujutsu_kaisen$protected(pLevel, pPos.relative(direction))) continue;

            ci.cancel();
            return;
        }
    }

    private static boolean jujutsu_kaisen$protected(Level level, BlockPos pos) {
        Vec3 center = pos.getCenter();

        return !VeilHandler.canDestroy(null, level, center.x, center.y, center.z);
    }
}

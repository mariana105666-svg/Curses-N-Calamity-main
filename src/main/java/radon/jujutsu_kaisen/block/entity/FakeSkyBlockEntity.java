package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FakeSkyBlockEntity extends DomainBlockEntity {
    public FakeSkyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(JJKBlockEntities.FAKE_SKY.get(), pPos, pBlockState);
    }
}

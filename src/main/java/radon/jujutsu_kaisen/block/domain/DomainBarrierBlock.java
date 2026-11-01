package radon.jujutsu_kaisen.block.domain;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class DomainBarrierBlock extends DomainBlock {
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 5);

    public DomainBarrierBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(DAMAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DAMAGE);
    }

}

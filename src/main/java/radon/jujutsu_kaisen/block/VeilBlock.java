package radon.jujutsu_kaisen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.block.entity.JJKBlockEntities;
import radon.jujutsu_kaisen.block.entity.VeilBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;

public class VeilBlock extends Block implements EntityBlock {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    public static final BooleanProperty TRANSPARENT = BooleanProperty.create("transparent");

    public VeilBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COLOR, DyeColor.BLACK)
                .setValue(TRANSPARENT, false));
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        return true;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return pState.getValue(TRANSPARENT) ? RenderShape.INVISIBLE : super.getRenderShape(pState);
    }

    /*
     *     @Override
    public float getExplosionResistance(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        float resistance = super.getExplosionResistance(state, level, pos, explosion);

        if (!(level instanceof ServerLevel serverLevel)) return resistance;

        if (!(level.getBlockEntity(pos) instanceof VeilBlockEntity veil)) return resistance;

        UUID identifier = veil.getParentUUID();

        if (identifier == null) return resistance;

        if (!(serverLevel.getEntity(identifier) instanceof IBarrier barrier)) return resistance;

        return resistance * barrier.getStrength();
    }
     */
 
        @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        if (pContext instanceof EntityCollisionContext ctx) {
            if (!(pLevel.getBlockEntity(pPos) instanceof VeilBlockEntity be))
                return super.getCollisionShape(pState, pLevel, pPos, pContext);
            
            Entity entity = ctx.getEntity();
             if (entity != null) {
                if (entity instanceof LivingEntity living && (JJKAbilities.hasTrait(living, Trait.HEAVENLY_RESTRICTION) || JJKAbilities.hasToggled(living, JJKAbilities.BARRIER_TRAVEL.get()) ) && !pContext.isAbove(Shapes.block(), pPos, true)) {
                    return Shapes.empty();
                }
                if (entity instanceof Projectile projectile) 
                    entity = projectile.getOwner();
                return VeilHandler.isWhitelisted(pPos, entity) && !pContext.isAbove(Shapes.block(), pPos, true) ? Shapes.empty() : Shapes.block();
            }
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        if (!(level.getBlockEntity(pos) instanceof VeilBlockEntity veil)) return;

        Entity check = entity;
        
        if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
            check = projectile.getOwner();
        }
        if (check instanceof LivingEntity living && (JJKAbilities.hasTrait(living, Trait.HEAVENLY_RESTRICTION) || JJKAbilities.hasToggled(living, JJKAbilities.BARRIER_TRAVEL.get()) ) ) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                radon.jujutsu_kaisen.util.SorcererUtil.tryUnlockVeil(player);
            }
            return;
        }

        boolean allowed = VeilHandler.isWhitelisted(pos, check);
        
        if (allowed && entity instanceof net.minecraft.server.level.ServerPlayer player) {
            radon.jujutsu_kaisen.util.SorcererUtil.tryUnlockVeil(player);
        }

        if (!allowed) {
            entity.teleportTo(entity.xOld, entity.yOld, entity.zOld);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return JJKBlockEntities.VEIL.get().create(pPos, pState);
    }

    // @Nullable
    // @Override
    // public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
    //     return pLevel.isClientSide ? null : JJKBlocks.createTickerHelper(pBlockEntityType, JJKBlockEntities.VEIL.get(), VeilBlockEntity::tick);
    // }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(COLOR, TRANSPARENT);
    }
}

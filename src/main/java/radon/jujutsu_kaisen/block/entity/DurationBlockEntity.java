package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import radon.jujutsu_kaisen.block.base.TemporaryBlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DurationBlockEntity extends TemporaryBlockEntity {
    private int duration;

    @Nullable
    private BlockState original;

    private CompoundTag deferred;

    public DurationBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public DurationBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(JJKBlockEntities.DURATION.get(), pPos, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DurationBlockEntity pBlockEntity) {
        if (--pBlockEntity.duration <= 0) {
            pBlockEntity.destroy();
        }
    }

    // public @Nullable BlockState getOriginal() {
    //     if (this.original == null && this.deferred != null) {
    //         this.original = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), this.deferred);
    //         this.deferred = null;
    //         this.setChanged();
    //     }
    //     return this.original;
    // }

    public void setDuration(int duration) {
        this.duration = duration;
        this.setChanged();
    }

    public void create(int duration, BlockState state) {
        this.duration = duration;
        this.setOriginal(state);
        this.setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.duration = pTag.getInt("duration");
    }

 
    @Override
    public void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putInt("duration", this.duration);
    }

}

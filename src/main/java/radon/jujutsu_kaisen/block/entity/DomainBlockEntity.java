package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.base.TemporaryBlockEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

import java.util.UUID;

public class DomainBlockEntity extends TemporaryBlockEntity {
    private boolean initialized;
    protected UUID identifier;
    protected UUID pidentifier;
    //private int death;

    @Nullable
    private BlockState original;

    private CompoundTag deferred;

    @Nullable
    private CompoundTag saved;

    public DomainBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(JJKBlockEntities.DOMAIN.get(), pPos, pBlockState);
    }

    public DomainBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    // public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DomainBlockEntity pBlockEntity) {
    //     if (pBlockEntity.identifier == null || !(((ServerLevel) pLevel).getEntity(pBlockEntity.identifier) instanceof DomainExpansionEntity domain) || domain.isRemoved() || !domain.isAlive()) {
    //         pBlockEntity.death--;
    //     }

    //     if (pBlockEntity.death <= 0) {
    //         pBlockEntity.destroy();
    //     }
    // }

    // public void destroy() {
    //     if (this.level == null) return;

    //     BlockState original = this.getOriginal();
    //      BlockPos pos = this.getBlockPos();
         
    //     if (original != null) {
    //         if (original.isAir()) {
    //             if (this.level.getBlockEntity(pos) != null) {
    //                 this.level.removeBlockEntity(pos);
    //             }

    //             this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());

    //         } else {
    //             this.level.setBlockAndUpdate(this.getBlockPos(), original);

    //             if (this.saved != null) {
    //                 BlockEntity be = this.level.getBlockEntity(this.getBlockPos());

    //                 if (be != null) {
    //                     be.load(this.saved);
    //                 }
                 
    //             }
    //                if (this.level.getBlockEntity(pos) != null) {
    //                     this.level.removeBlockEntity(pos);
    //                 }
    //         }
    //     } else {
    //          if (this.level.getBlockEntity(pos) != null) {
    //                 this.level.removeBlockEntity(pos);
    //             }
    //         this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
    //     }
    // }
    // public void destroy() {
    //     if (this.level == null) return;

    //     BlockPos pos = this.getBlockPos();
    //     BlockState original = this.getOriginal();

    //     if (original == null || original.isAir() || original.getBlock() == JJKBlocks.DOMAIN_AIR.get()) {
    //         if (this.level.getBlockEntity(pos) != null) {
    //             this.level.removeBlockEntity(pos);
    //         }
    //         this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
           
    //         return;
    //     }

    //     this.level.setBlockAndUpdate(pos, original);

    //     if (this.saved != null) {
    //         BlockEntity be = this.level.getBlockEntity(pos);
    //         if (be != null) {
    //             be.load(this.saved);
    //         }
    //     }

    //     if (this.level.getBlockEntity(pos) == this) {
    //         this.level.removeBlockEntity(pos);
    //     }
    // }

    public void destroy() {
    if (this.level == null) return;

    BlockPos pos = this.getBlockPos();
    BlockState original = this.getOriginal();

    if (original == null || original.isAir() ) {
        // if (this.level.getBlockEntity(pos) == this) {
        //     this.level.removeBlockEntity(pos);
        // }
        this.level.setBlock(pos, Blocks.AIR.defaultBlockState(),
                                              Block.UPDATE_CLIENTS |  Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
        //this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        
        return;
    }


    // boolean createsBE = original.hasBlockEntity();


    // if (createsBE) {
    //     if (this.level.getBlockEntity(pos) == this) {
    //         this.level.removeBlockEntity(pos);
    //     }
    // }

    this.level.setBlock(pos, original,
                                              Block.UPDATE_CLIENTS |  Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
    //this.level.setBlockAndUpdate(pos, original);
    

    if (this.saved != null) {
        BlockEntity restored = this.level.getBlockEntity(pos);
        if (restored != null) {
            restored.load(this.saved);
        }
    }


    // if (!createsBE) {
    //     BlockEntity leftover = this.level.getBlockEntity(pos);
    //     if (leftover == this) {
    //         this.level.removeBlockEntity(pos);
    //     }
    // }
}


// @Override
// public CompoundTag getUpdateTag() {
//     CompoundTag tag = new CompoundTag();
//     this.saveAdditional(tag);
//     return tag;
// }

// @Override
// public void handleUpdateTag(CompoundTag tag) {
//     this.load(tag);
// }

// @Override
// public ClientboundBlockEntityDataPacket getUpdatePacket() {
//     return ClientboundBlockEntityDataPacket.create(this);
// }

    @Nullable
    public UUID getIdentifier() {
        return this.identifier;
    }

        @Nullable
    public UUID getPidentifier() {
        return this.pidentifier;
    }

            @Nullable
    public void setPidentifier(UUID here) {
        this.pidentifier = here;
    }

    @Nullable
    public BlockState getOriginal() {
        if (this.level == null) return this.original;

        if (this.original == null && this.deferred != null) {
            this.original = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), this.deferred);
            this.deferred = null;
            this.setChanged();
        }
        return this.original;
    }

    public @Nullable CompoundTag getSaved() {
        return this.saved;
    }

    public void create(UUID identifier, int delay, BlockState state, CompoundTag saved, UUID pidentifier) {
        this.initialized = true;
        this.identifier = identifier;
        //this.death = delay;
        this.original = state;
        this.saved = saved;
        this.pidentifier = pidentifier;
        this.setChanged();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putBoolean("initialized", this.initialized);

        if (this.initialized) {
            pTag.putUUID("identifier", this.identifier);
            //pTag.putInt("death", this.death);

            if (this.original != null) {
                pTag.put("original", NbtUtils.writeBlockState(this.original));
            } else if (this.deferred != null) {
                pTag.put("original", this.deferred);
            }

            if (this.saved != null) {
                pTag.put("saved", this.saved);
            }
            pTag.putUUID("pidentifier", this.pidentifier);
        }
    }


    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        this.initialized = pTag.getBoolean("initialized");

        if (this.initialized) {
            this.identifier = pTag.getUUID("identifier");
            //this.death = pTag.getInt("death");
            this.deferred = pTag.getCompound("original");

            if (pTag.contains("saved")) {
                this.saved = pTag.getCompound("saved");
            }
            this.pidentifier = pTag.getUUID("pidentifier");
        }
    }
}

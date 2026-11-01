package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.PlayerModifier;

import javax.annotation.Nullable;

public class VeilBlockEntityA extends BlockEntity {
    private int counter;

    @Nullable
    private BlockPos parent;

    private boolean initialized;

    @Nullable
    private BlockState original;

    private CompoundTag deferred;

    @Nullable
    private CompoundTag saved;

    private int size;

    public VeilBlockEntityA(BlockPos pPos, BlockState pBlockState) {
        super(JJKBlockEntities.VEIL.get(), pPos, pBlockState);
    }

    // public static void tick(Level pLevel, BlockPos pPos, BlockState pState, VeilBlockEntity pBlockEntity) {
    //     if (++pBlockEntity.counter != VeilRodBlockEntity.INTERVAL * 2) return;

    //     pBlockEntity.counter = 0;

    //     if (pBlockEntity.parent == null || !(pLevel.getBlockEntity(pBlockEntity.parent) instanceof VeilRodBlockEntity be) || be.getSize() != pBlockEntity.size) {
    //         pBlockEntity.destroy();
    //     }
    // }

    // public void destroy() {
    //     if (this.level == null) return;

    //     BlockState original = this.getOriginal();

    //     if (original != null) {
    //         if (original.isAir() || original.getBlock()  == JJKBlocks.DOMAIN_AIR.get() ) {
    //             //if (!this.getBlockState().getFluidState().isEmpty()) {
    //                 this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
    //             //} else {
    //            //     this.level.destroyBlock(this.getBlockPos(), false);
    //            // }
    //         } else {
    //             this.level.setBlockAndUpdate(this.getBlockPos(), original);
    //         }
    //     } else {
    //         if (!this.getBlockState().getFluidState().isEmpty()) {
    //             this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
    //         } else {
    //             this.level.destroyBlock(this.getBlockPos(), false);
    //         }
    //     }
    // }

    public void destroy() {
        if (this.level == null) return;

        BlockPos pos = this.getBlockPos();
        BlockState original = this.getOriginal();

        // if (this.level.getBlockEntity(pos) == this) {
        //     this.level.removeBlockEntity(pos);
        // }
        


        if (original == null || original.isAir() || original.getBlock() == JJKBlocks.DOMAIN_AIR.get()) {
            this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }

    

        // if (original.getBlock() instanceof EntityBlock) {
        //     this.level.removeBlockEntity(pos);
        // }

        this.level.setBlockAndUpdate(pos, original);
            if (this.saved != null) {
        BlockEntity restored = this.level.getBlockEntity(pos);
        if (restored != null && !(restored instanceof VeilBlockEntity) && this.saved != null) {
            restored.load(this.saved);
        }
    }
    }

    public @Nullable CompoundTag getSaved() {
        return this.saved;
    }


    public static boolean isWhitelisted(@Nullable BlockPos parent, Entity entity) {
        if (entity == null || parent == null || !(entity.level().getBlockEntity(parent) instanceof VeilRodBlockEntity be)) return false;
        if (be.modifiers == null) return false;
        if (entity instanceof Player player) {
            for (Modifier modifier : be.modifiers) {
                if (modifier.getAction() == Modifier.Action.ALLOW &&
                    modifier.getType() == Modifier.Type.OWNER_BYPASS && (entity.getUUID().equals(be.ownerUUID)) ) {
                        return true;
                }
                if (modifier.getAction() != Modifier.Action.ALLOW || modifier.getType() != Modifier.Type.PLAYER)
                    continue;
                if (((PlayerModifier) modifier).getName().equals(player.getDisplayName().getString())) {
                    return modifier.getAction() == Modifier.Action.ALLOW;
                }
            }
        }

        for (Modifier modifier : be.modifiers) {
            if (modifier.getAction() == Modifier.Action.ALLOW && (modifier.getType() == Modifier.Type.CURSE || modifier.getType() == Modifier.Type.SORCERER)) {
                if (!entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
                ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

                return cap.getType() == JujutsuType.CURSE && modifier.getType() == Modifier.Type.CURSE ||
                        cap.getType() != JujutsuType.CURSE && modifier.getType() == Modifier.Type.SORCERER;
            }
        }
        return false;
    }

    public @Nullable BlockState getOriginal() {
        if (this.level == null) return this.original;

        if (this.original == null && this.deferred != null) {
            this.original = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), this.deferred);
            this.deferred = null;
            //this.setChanged();
        }
        return this.original;
    }

    public void create(BlockPos parent, int size, BlockState original, CompoundTag saved) {
        this.parent = parent;
        this.size = size;
        if (original.getBlock() == JJKBlocks.DOMAIN_AIR.get()) {
            original = Blocks.AIR.defaultBlockState();
        }
        this.original = original;
        this.saved = saved;
        this.initialized = true;
        this.setChanged();
        //this.sendUpdates();
    }

    public @Nullable BlockPos getParent() {
        return this.parent;
    }

    public static boolean isAllowed(BlockPos pos, Entity entity) {
        return isWhitelisted(pos, entity);
    }

    public void sendUpdates() {
        if (this.level != null) {
            //this.level.setBlocksDirty(this.worldPosition, this.level.getBlockState(this.worldPosition), this.level.getBlockState(this.worldPosition));
           // this.level.sendBlockUpdated(this.worldPosition, this.level.getBlockState(this.worldPosition), this.level.getBlockState(this.worldPosition), 3);
           // this.level.updateNeighborsAt(this.worldPosition, this.level.getBlockState(this.worldPosition).getBlock());
            //this.setChanged();
        }
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // @Override
    // public @NotNull CompoundTag getUpdateTag() {
    //     CompoundTag tag = super.getUpdateTag();
    //     this.saveAdditional(tag);
    //     return tag;
    // }
@Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
                System.out.println("savingload");
        pTag.putBoolean("initialized", this.initialized);

        if (this.initialized) {
            if (this.original != null) {
                pTag.put("original", NbtUtils.writeBlockState(this.original));
            } else {
                pTag.put("original", this.deferred);
            }
            if (this.saved != null) {
                pTag.put("saved", this.saved);
            }
     
        
        System.out.println("okay1");
        if (this.parent != null) {
            pTag.put("parent", NbtUtils.writeBlockPos(this.parent));
        }
        pTag.putInt("size", this.size);
        }
    }

     private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            this.level.setBlock(this.worldPosition, this.getBlockState(),
                                              Block.UPDATE_CLIENTS |  Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            //this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }
    
    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        this.initialized = pTag.getBoolean("initialized");

        if (this.initialized) {
            this.deferred = pTag.getCompound("original");
            if (pTag.contains("saved")) {
                this.saved = pTag.getCompound("saved");
            }
       

        if (pTag.contains("parent")) {
            this.parent = NbtUtils.readBlockPos(pTag.getCompound("parent"));
        }
        this.size = pTag.getInt("size");
         }
                //System.out.println("ooload");
         if (this.level != null) {
            this.markUpdated();
        }
    }
}

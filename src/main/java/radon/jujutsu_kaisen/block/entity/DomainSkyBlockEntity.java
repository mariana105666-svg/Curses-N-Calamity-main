package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DomainSkyBlockEntity extends DomainBlockEntity {
    @Nullable
    private ResourceLocation domain;

    public DomainSkyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(JJKBlockEntities.DOMAIN_SKY.get(), pPos, pBlockState);
    }

    public void setDomain(@Nullable ResourceLocation domain) {
        this.domain = domain;
        this.markUpdated();
    }

    public @Nullable ResourceLocation getDomain() {
        return this.domain;
    }
    

    // @Override
    // public void onLoad() {
    //     super.onLoad();
    //     if (this.domain == null) {
    //         Level level = this.getLevel();
    //         if (level instanceof ServerLevel serverLevel) {
    //             if (serverLevel.getEntity(this.pidentifier) instanceof DomainExpansionEntity domain) {
    //                 this.setDomain(JJKAbilities.getKey(domain.getAbility()));
    //             }
    //         }
    //     }
    // }
        public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DomainSkyBlockEntity pBlockEntity) {
        if (pBlockEntity.domain == null) {
            if (!(((ServerLevel) pLevel).getEntity(pBlockEntity.pidentifier) instanceof DomainExpansionEntity domain))
                return;
            pBlockEntity.setDomain(JJKAbilities.getKey(domain.getAbility()));
        }
    }
    



    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    
    @Override
    public void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (this.domain != null) {
            pTag.putString("domain", this.domain.toString());
        }
    }


    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains("domain")) {
            this.domain = new ResourceLocation(pTag.getString("domain"));
        }

        if (this.level != null) {
            this.markUpdated();
        }
    }
}

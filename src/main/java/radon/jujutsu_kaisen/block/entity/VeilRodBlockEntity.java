package radon.jujutsu_kaisen.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.block.JJKBlocks;
import radon.jujutsu_kaisen.block.VeilBlock;
import radon.jujutsu_kaisen.block.domain.DomainAirBlock;
import radon.jujutsu_kaisen.block.domain.DomainBlock;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.item.veil.modifier.ColorModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.ModifierUtils;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VeilRodBlockEntity extends BlockEntity {
    public static final int RANGE = 128;
    public static final int INTERVAL = 2;
    private static final float COST = 0.1F;
    private int blockCursor;

    private int counter;
    private int size;
    private float experience;
    private float storedEnergy;
    public List<Modifier> modifiers;

    @Nullable
    public UUID ownerUUID;

    public VeilRodBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(JJKBlockEntities.VEIL_ROD.get(), pPos, pBlockState);

        this.size = ConfigHolder.SERVER.minimumVeilSize.get();
        this.modifiers = new ArrayList<>();
        this.blockCursor = 0;
        this.storedEnergy = 0.0F;
    }


       @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    // public boolean isValid() {
    //     if (!(this.level instanceof ServerLevel serverLevel)) return false;
    //     if (this.ownerUUID == null) return false;

    //     //if (!(serverLevel.getEntity(this.ownerUUID) instanceof LivingEntity owner) || !owner.getCapability(SorcererDataHandler.INSTANCE).isPresent())
    //      //   return false;
    //    // if ((serverLevel.getEntity(this.ownerUUID) instanceof LivingEntity owner)) {
    //        // if (!(owner instanceof Player player) || !player.getAbilities().instabuild) {
    //      //       ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //             //float cost = ConfigHolder.SERVER.veilCost.get() * ((float) this.getSize() / ConfigHolder.SERVER.maximumVeilSize.get());

    //            // return cap.getEnergy() >= cost;
    //   //      }
    // //    }
    //     return true;
    // }

//     public static void tick(Level level, BlockPos pos, BlockState state, VeilRodBlockEntity rod) {

   


//     if (++rod.counter < INTERVAL) return;
//     rod.counter = 0;
//      if (VeilHandler.checkIntersect(level, rod.getBlockPos(), rod.getSize())) {
//         return;
//     }
//     //if (!VeilHandler.isRegistered(rod)) {
//        VeilHandler.addVeil(rod);
//    // }
 



//     //if (!(level instanceof ServerLevel serverLevel)) return;
//     if (rod.ownerUUID == null) return;

//     //if (!(serverLevel.getEntity(rod.ownerUUID) instanceof LivingEntity owner)) return;
//     //ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
//    // if (ownerCap == null) return;

//    // if (!(owner instanceof Player player && player.getAbilities().instabuild)) {
//        // float cost = 0.1f * (rod.getSize() / (float) ConfigHolder.SERVER.maximumVeilSize.get());
//         //if (ownerCap.hasTrait(Trait.SIX_EYES)) cost *= 0.5f;
//         //if (ownerCap.getEnergy() < cost) return;

//         //ownerCap.useEnergy(cost);

//         //if (owner instanceof ServerPlayer serverPlayer) {
//         //    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(ownerCap.serializeNBT()), serverPlayer);
//         //}
//     //} replace with system where you seep energy in veils over time while near and they drain that energy passively. 1 hr recharge = 

//     BlockState replacement = JJKBlocks.VEIL.get().defaultBlockState();
//     for (Modifier mod : rod.modifiers) {
//         if (mod.getType() == Modifier.Type.COLOR) {
//             replacement = replacement.setValue(VeilBlock.COLOR, ((ColorModifier) mod).getColor());
//         } else if (mod.getType() == Modifier.Type.TRANSPARENT) {
//             replacement = replacement.setValue(VeilBlock.TRANSPARENT, true);
//         }
//     }


//     // AABB rodAABB = new AABB(pos.offset(-rod.getSize(), -rod.getSize(), -rod.getSize()),
//     //                         pos.offset(rod.getSize(), rod.getSize(), rod.getSize()));
//     // List<DomainExpansionEntity> nearbyDomains = new ArrayList<>();
//     // for (DomainExpansionEntity domain : VeilHandler.getDomains(serverLevel)) {
//     //     if (domain.getBounds().intersects(rodAABB)) nearbyDomains.add(domain);
//     // }

//     int size = rod.getSize();
//     //int blocksPerTick = 61152 * (1 + 2*(size / ConfigHolder.SERVER.maximumVeilSize.get()) ); 
//     int s = (size * 2 + 1);
//     int totalBlocks = s * s * s;

//     if (rod.blockCursor >= totalBlocks) rod.blockCursor = 0; // reset
//     int yLayer = size - (rod.blockCursor / (s * s ));
//     //for (int i = 0; i < blocksPerTick && rod.blockCursor < totalBlocks; i++, rod.blockCursor++) {
//     for (int xIndex = 0; xIndex < s; xIndex++) {
//     for (int zIndex = 0; zIndex < s; zIndex++) {

//         //int cursor = rod.blockCursor;
//         int x = xIndex - size;
//         int z = zIndex - size;
//         int y = yLayer;
//         // int x = cursor % (s) - size;
//         // int z = (cursor / (s)) % (s) - size;
//         // int y = size - (cursor / ((s) * (s)));

//         double distSqr = x * x + y * y + z * z;
//         double minSqr = (size - 0.5) * (size - 0.5);
//         double maxSqr = (size + 0.5) * (size + 0.5);
//         if (distSqr < minSqr || distSqr > maxSqr) continue;
//       //  double distance = Math.sqrt(x * x + y * y + z * z);
//         //if (distance >= size || distance < size - 1) continue; 

//         BlockPos targetPos = pos.offset(x, y, z);
//          if (!level.isInWorldBounds(pos)) continue;

//         // boolean blocked = false;
//         // for (DomainExpansionEntity domain : nearbyDomains) {
//         //     LivingEntity domainOwner = domain.getOwner();
//         //     if (domainOwner == null) continue;

//         //     ISorcererData domainCap = domainOwner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
//         //     if (domainCap == null) continue;

//         //     if (domainCap.getExperience() >= rod.experience && domain.isInsideBarrier(targetPos)) {
//         //         if (level.getBlockEntity(targetPos) instanceof VeilBlockEntity be) be.destroy();
//         //         blocked = true;
//         //         break;
//         //     }
//         // }
//         // if (blocked) continue;
//         BlockState targetState = level.getBlockState(targetPos);
//         BlockEntity existingBE = level.getBlockEntity(targetPos);
       
//         Block b = targetState.getBlock();
//         if (b instanceof DomainBlock || b instanceof DomainAirBlock || b instanceof VeilBlock ) {
//             continue;  
//         }
 
//         if (state.is(Blocks.BEDROCK)) continue;
//         BlockState currentState = (existingBE instanceof VeilBlockEntity ve) ? ve.getOriginal() : targetState;
//         CompoundTag saved = null;
//         // if (existingBE == null || (!(existingBE instanceof VeilBlockEntity) && !(existingBE instanceof DomainBlockEntity))  ) {
//         //    hadBarrier = false;
//             if (existingBE != null) {
//                 // if (existingBE instanceof VeilBlockEntity) {
//                 //     continue;
//                 // }
//                 saved = existingBE.saveWithoutMetadata();
//                 if (existingBE instanceof net.minecraft.world.Container container) {
//                     container.clearContent();
//                     level.removeBlockEntity(targetPos);
//                 }
//             }

//         level.setBlockAndUpdate(targetPos, replacement);
//             //level.setBlock(targetPos, replacement, Block.UPDATE_ALL);
//        // }

//        // if (hadBarrier == false) {
//         if (level.getBlockEntity(targetPos) instanceof VeilBlockEntity be) {
//             be.create(pos, size, currentState, saved);
//         }
//        // }
//         }
//     }
//     rod.blockCursor += s * s; 
// }



    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        // if (!this.level.isClientSide) {
        //     VeilHandler.removeVeil(this);
        // }
        this.size = size;
        this.setChanged();
    }

    public void setExperience(float experience) {
        this.experience = experience;
        this.setChanged();
    }
    
    public float getExperience() {
        return this.experience;
    }

    public void setOwner(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }

    
    // @Override
    // public void setRemoved() {
    //     if (!this.removed) {
    //         this.removed = true; // first call only
    //     if (!this.level.isClientSide) {
    //         BlockState state = this.level.getBlockState(this.getBlockPos());
    //         if (!state.getBlock().equals(JJKBlocks.VEIL_ROD.get())  && VeilHandler.isRegistered(this)) {
    //             VeilHandler.removeVeil(this);
    //         }
    //                   //System.out.println("storebreakms");
    //     }
    //     super.setRemoved();
    //     }
    // }

    //     @Override
    // public void onLoad() {
    //     super.onLoad();
    //     if (!this.level.isClientSide) {
    //         if (this.level != null && !this.isRemoved() && this.ownerUUID != null) {
    //                 if (!VeilHandler.isRegistered(this)) {
    //                     VeilHandler.addVeil(this);
    //                 }
    //         }
    //     }
    // }

    // @Override
    // public void onLoad() {
    //     super.onLoad();
    //     if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
    //         serverLevel.getServer().execute(() -> {
    //             if (!this.isRemoved() && this.ownerUUID != null) {
    //                 VeilHandler.addVeil(this);
    //             }
    //         });
    //     }
    // }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (this.ownerUUID != null) {
            pTag.putUUID("owner", this.ownerUUID);
        }
          //System.out.println("stored1ms");
        pTag.putInt("counter", this.counter);
                  //System.out.println("stored2ms");
        pTag.putInt("size", this.size);
        pTag.putFloat("experience", this.experience);
        pTag.putFloat("storedEnergy", this.storedEnergy);
        if (this.modifiers != null) {
            pTag.put("modifiers", ModifierUtils.serialize(this.modifiers));
        }
        //System.out.println("serialize took: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains("owner")) {
            this.ownerUUID = pTag.getUUID("owner");
        }
        this.counter = pTag.getInt("counter");
        this.size = pTag.getInt("size");
        this.experience = pTag.getFloat("experience");
        this.storedEnergy = pTag.getFloat("storedEnergy");
                //System.out.println("load");
        if (pTag.contains("modifiers")) {
            this.modifiers = ModifierUtils.deserialize(pTag.getList("modifiers", Tag.TAG_COMPOUND));
        }
    }
}

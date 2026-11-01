package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.ClientWrapper;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

//import com.mojang.authlib.minecraft.MinecraftProfileTexture;

//import com.mojang.authlib.GameProfile;
//import com.mojang.authlib.minecraft.MinecraftProfileTexture;

public class SyncSorcererDataS2CPacket {
    private final CompoundTag nbt;

    public SyncSorcererDataS2CPacket(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public SyncSorcererDataS2CPacket(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Player player = ClientWrapper.getPlayer();

            assert player != null;

            ISorcererData oldCap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            ISorcererData newCap = new SorcererData();
            newCap.deserializeNBT(this.nbt);
           // if (newCap.getStolenSkinProfile() != null) {
             //   radon.jujutsu_kaisen.client.ClientSkinHandler.handleSkinSync(oldCap, newCap);
           //     GameProfile profile = newCap.getStolenSkinProfile();
                //  SkinManager skinManager = Minecraft.getInstance().getSkinManager();
                //     skinManager.registerSkins(newCap.getStolenSkinProfile(), (type, location, texture) -> {
                //     if (type == MinecraftProfileTexture.Type.SKIN) {
                //         oldCap.setStolenSkinTexture(location);
                //     }
                // }, true);
                                
           //     skinManager.registerSkins(profile, new SkinManager.SkinTextureCallback() {
                //     @Override
                //     public void onSkinTextureAvailable(MinecraftProfileTexture.Type type, ResourceLocation location, MinecraftProfileTexture profileTexture) {
                //         if (type == MinecraftProfileTexture.Type.SKIN) {
                //             oldCap.setStolenSkinTexture(location);
                //         }
                //     }
                // }, true);


          //  }
            // if (newCap.getStolenSkinProfile() != null) {
      
            //     radon.jujutsu_kaisen.client.ClientSkinHandler.handleSkinSync(oldCap, newCap);
            // } else {
             
            //     newCap.setStolenSkinProfile(oldCap.getStolenSkinProfile());
            //     newCap.setStolenSkinTexture(oldCap.getStolenSkinTexture());
            // }
            Set<Ability> newToggled = new HashSet<>();
            for (Tag tag : this.nbt.getList("toggled", Tag.TAG_STRING)) {
                newToggled.add(JJKAbilities.getValue(new ResourceLocation(tag.getAsString())));
            }

            Set<Ability> stale = new HashSet<>(oldCap.getToggled());
            stale.removeAll(newToggled);

            for (Ability ability : stale) {
                oldCap.toggle(ability);
            }
            oldCap.deserializeNBT(this.nbt);
        }));
        ctx.setPacketHandled(true);
    }
}

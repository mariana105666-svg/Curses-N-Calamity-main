package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.ClientWrapper;

import java.util.function.Supplier;

public class SyncEnergyS2CPacket {
    private final float energy;

    public SyncEnergyS2CPacket(float energy) {
        this.energy = energy;
    }

    public SyncEnergyS2CPacket(FriendlyByteBuf buf) {
        this(buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.energy);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Player player = ClientWrapper.getPlayer();

            if (player == null) return;

            ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null) return;

            cap.setEnergy(this.energy);
        }));
        ctx.setPacketHandled(true);
    }
}

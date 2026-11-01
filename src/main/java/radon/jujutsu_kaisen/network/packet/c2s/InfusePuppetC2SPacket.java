package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.puppet.PuppetInfusionHandler;

import java.util.function.Supplier;

public class InfusePuppetC2SPacket {
    private final boolean infusing;

    public InfusePuppetC2SPacket(boolean infusing) {
        this.infusing = infusing;
    }

    public InfusePuppetC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.infusing);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) return;

            PuppetInfusionHandler.setInfusing(sender, this.infusing);
        });
        ctx.setPacketHandled(true);
    }
}

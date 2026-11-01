package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.puppet.ExcavateHandler;

import java.util.function.Supplier;

public class ExcavateCancelC2SPacket {
    public ExcavateCancelC2SPacket() {
    }

    public ExcavateCancelC2SPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender != null) ExcavateHandler.cancel(sender);
        });
        ctx.setPacketHandled(true);
    }
}

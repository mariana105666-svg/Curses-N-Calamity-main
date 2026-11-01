package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.client.domain.ClientDomainClashHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DomainClashS2CPacket {
    public record Entry(String name, float fraction, boolean self) {}

    private final List<Entry> entries;

    public DomainClashS2CPacket(List<Entry> entries) {
        this.entries = entries;
    }

    public DomainClashS2CPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();

        this.entries = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            this.entries.add(new Entry(buf.readUtf(), buf.readFloat(), buf.readBoolean()));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entries.size());

        for (Entry entry : this.entries) {
            buf.writeUtf(entry.name());
            buf.writeFloat(entry.fraction());
            buf.writeBoolean(entry.self());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDomainClashHandler.accept(this.entries)));

        ctx.setPacketHandled(true);
    }
}

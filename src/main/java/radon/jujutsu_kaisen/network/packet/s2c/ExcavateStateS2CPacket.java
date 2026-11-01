package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExcavateStateS2CPacket {
    private final boolean working;

    public ExcavateStateS2CPacket(boolean working) {
        this.working = working;
    }

    public ExcavateStateS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.working);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                radon.jujutsu_kaisen.client.puppet.ExcavateClient.setWorking(this.working)));
        ctx.setPacketHandled(true);
    }
}

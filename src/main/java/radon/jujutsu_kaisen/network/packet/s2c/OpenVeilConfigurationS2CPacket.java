package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.client.veil.ClientVeilHandler;

import java.util.function.Supplier;

public class OpenVeilConfigurationS2CPacket {
    public OpenVeilConfigurationS2CPacket() {

    }

    public OpenVeilConfigurationS2CPacket(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {

    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientVeilHandler::openConfiguration));

        ctx.setPacketHandled(true);
    }
}

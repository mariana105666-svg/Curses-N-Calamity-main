package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.client.JJKClientEventHandler;
import radon.jujutsu_kaisen.client.gui.overlay.BlinkOverlay;


import java.util.function.Supplier;

public class BlinkS2CPacket {
    private int duration;
    public BlinkS2CPacket() {
    }   
    

    public BlinkS2CPacket(int duration) {
        this.duration = duration;
    }
    

    public BlinkS2CPacket(FriendlyByteBuf ignored) {
        this();
        this.duration = ignored.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(duration);
    }

    public void encode(FriendlyByteBuf ignored) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlinkOverlay.flash(this.duration));
        });
        return true;
    }

    public int getDuration() {
        return duration;
    }
}
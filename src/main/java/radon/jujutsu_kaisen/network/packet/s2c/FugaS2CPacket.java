package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.client.ClientWrapper;

import java.util.function.Supplier;

public class FugaS2CPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float radius;

    public FugaS2CPacket(Vec3 center, float radius) {
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
        this.radius = radius;
    }

    public FugaS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.radius = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.radius);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientWrapper.startFuga(new Vec3(this.x, this.y, this.z), this.radius)));
        ctx.setPacketHandled(true);
    }
}

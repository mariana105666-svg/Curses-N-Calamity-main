package radon.jujutsu_kaisen.network.packet.s2c;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ResurrectionHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class ResurrectionS2CPacket {
    private final float health;
    private final int src;

    public ResurrectionS2CPacket(int src, float health) {
        this.src = src;
        this.health = health;
    }

    public ResurrectionS2CPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.src);
        buf.writeFloat(this.health);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ResurrectionHandler.handle(src, health)
            )
        );

        ctx.setPacketHandled(true);
    }

}
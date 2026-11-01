package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.puppet_manipulation.Tunnel;
import radon.jujutsu_kaisen.puppet.TunnelHandler;
import radon.jujutsu_kaisen.puppet.TunnelPattern;

import java.util.function.Supplier;

public class TunnelDigC2SPacket {
    private final BlockPos pos;
    private final Direction facing;
    private final TunnelPattern pattern;

    public TunnelDigC2SPacket(BlockPos pos, Direction facing, TunnelPattern pattern) {
        this.pos = pos;
        this.facing = facing;
        this.pattern = pattern;
    }

    public TunnelDigC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readEnum(TunnelPattern.class));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnum(this.facing);
        buf.writeEnum(this.pattern);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.hasToggled(JJKAbilities.TUNNEL.get())) return;

            if (sender.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D)
                    > (Tunnel.RANGE + 2.0D) * (Tunnel.RANGE + 2.0D)) return;

            TunnelHandler.start(sender, this.pos, this.facing, this.pattern);
        });
        ctx.setPacketHandled(true);
    }
}

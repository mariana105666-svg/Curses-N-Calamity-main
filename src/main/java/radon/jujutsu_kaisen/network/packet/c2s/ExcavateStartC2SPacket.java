package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.puppet_manipulation.Excavate;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.puppet.ExcavateHandler;

import java.util.function.Supplier;

public class ExcavateStartC2SPacket {
    private final BlockPos first;
    private final BlockPos second;

    public ExcavateStartC2SPacket(BlockPos first, BlockPos second) {
        this.first = first;
        this.second = second;
    }

    public ExcavateStartC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.first);
        buf.writeBlockPos(this.second);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.hasToggled(JJKAbilities.EXCAVATE.get())) return;

            double limit = (Excavate.RANGE + 2.0D) * (Excavate.RANGE + 2.0D);

            if (sender.distanceToSqr(this.first.getX() + 0.5D, this.first.getY() + 0.5D, this.first.getZ() + 0.5D) > limit) return;
            if (sender.distanceToSqr(this.second.getX() + 0.5D, this.second.getY() + 0.5D, this.second.getZ() + 0.5D) > limit) return;

            ExcavateHandler.start(sender, this.first, this.second);
        });
        ctx.setPacketHandled(true);
    }
}

package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.puppet.PuppetRecord;
import radon.jujutsu_kaisen.puppet.PuppetUtil;

import java.util.List;
import java.util.function.Supplier;

public class RenamePuppetC2SPacket {
    private final int slot;
    private final String name;

    public RenamePuppetC2SPacket(int slot, String name) {
        this.slot = slot;
        this.name = name;
    }

    public RenamePuppetC2SPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf(PuppetUtil.MAX_NAME_LENGTH));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.slot);
        buf.writeUtf(this.name, PuppetUtil.MAX_NAME_LENGTH);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null) return;

            List<PuppetRecord> puppets = cap.getPuppets();

            if (this.slot < 0 || this.slot >= puppets.size()) return;

            PuppetUtil.rename(sender, puppets.get(this.slot), this.name);
        });
        ctx.setPacketHandled(true);
    }
}

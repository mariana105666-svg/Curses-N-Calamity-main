package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

import java.util.function.Supplier;

public class SetQuickVeilC2SPacket {
    private final CompoundTag settings;

    public SetQuickVeilC2SPacket(CompoundTag settings) {
        this.settings = settings;
    }

    public SetQuickVeilC2SPacket(FriendlyByteBuf buf) {
        this.settings = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.settings);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null || this.settings == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null) return;

            cap.setQuickVeil(this.settings);
        });

        ctx.setPacketHandled(true);
    }
}

package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.AbsorbedCurse;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class CurseSummonC2SPacket {
    private final UUID curseId;

    public CurseSummonC2SPacket(UUID curseId) {
        this.curseId = curseId;
    }

    public CurseSummonC2SPacket(FriendlyByteBuf buf) {
        this.curseId = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.curseId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (cap == null) return;

            AbsorbedCurse curse = cap.getCurses().stream()
                    .filter(c -> c.getUUID().equals(this.curseId))
                    .findFirst()
                    .orElse(null);

            if (curse != null) {
                JJKAbilities.summonCurse(sender, curse, true);
            }
        });
        ctx.setPacketHandled(true);
    }
}

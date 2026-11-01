package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.misc.Veil;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.ModifierUtils;

import java.util.List;
import java.util.function.Supplier;

public class QuickVeilC2SPacket {
    private final CompoundTag data;
    private final int size;

    public QuickVeilC2SPacket(List<Modifier> modifiers, int size) {
        this.data = new CompoundTag();
        this.data.put("modifiers", ModifierUtils.serialize(modifiers));
        this.size = size;
    }

    public QuickVeilC2SPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
        this.size = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.data);
        buf.writeVarInt(this.size);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null || this.data == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null || !cap.isUnlocked(JJKAbilities.QUICK_VEIL_CONFIG.get())) return;

            Veil.place(sender, ModifierUtils.getModifiers(this.data), this.size);
        });

        ctx.setPacketHandled(true);
    }
}

package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

import java.util.function.Supplier;

public class SetAbilityOutputC2SPacket {
    private final ResourceLocation key;
    private final float output;
    private final boolean locked;
    private final boolean clear;

    public SetAbilityOutputC2SPacket(ResourceLocation key, float output, boolean locked, boolean clear) {
        this.key = key;
        this.output = output;
        this.locked = locked;
        this.clear = clear;
    }

    public SetAbilityOutputC2SPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.key);
        buf.writeFloat(this.output);
        buf.writeBoolean(this.locked);
        buf.writeBoolean(this.clear);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) return;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

            if (cap == null) return;

            Ability ability = JJKAbilities.getValue(this.key);

            if (ability == null) return;

            if (this.clear) {
                cap.clearAbilityOutput(ability);
                cap.setZoneLocked(ability, false);
                return;
            }

            cap.setAbilityOutput(ability, this.output);
            cap.setZoneLocked(ability, this.locked);
        });
        ctx.setPacketHandled(true);
    }
}

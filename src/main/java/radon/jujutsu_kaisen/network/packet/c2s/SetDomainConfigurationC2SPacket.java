package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.ValueType;
import radon.jujutsu_kaisen.capability.data.sorcerer.DomainConfiguration;

import java.util.function.Supplier;

public class SetDomainConfigurationC2SPacket {
    private final DomainConfiguration config;
    private final Object value;

    public SetDomainConfigurationC2SPacket(DomainConfiguration config, Object value) {
        this.config = config;
        this.value = value;
    }

    public SetDomainConfigurationC2SPacket(FriendlyByteBuf buf) {
        this.config = buf.readEnum(DomainConfiguration.class);
        if (this.config.getType() == ValueType.BOOLEAN) {
            this.value = buf.readBoolean();
        } else if (this.config.getType() == ValueType.FLOAT) {
            this.value = buf.readFloat();
        } else {
            this.value = null;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.config);
        if (this.config.getType() == ValueType.BOOLEAN) {
            buf.writeBoolean((boolean) value);
        } 
        else if (this.config.getType() == ValueType.FLOAT) {
            buf.writeFloat((float) value);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            assert sender != null;

            ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            cap.setDomainConfig(this.config, value);
        });
        ctx.setPacketHandled(true);
    }
}
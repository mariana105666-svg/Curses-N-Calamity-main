package radon.jujutsu_kaisen.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.client.ability.ClientAbilityHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;

import java.util.function.Supplier;

public class TriggerAbilityS2CPacket {
    private final ResourceLocation key;

    public TriggerAbilityS2CPacket(ResourceLocation key) {
        this.key = key;
    }

    public TriggerAbilityS2CPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.key);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Ability ability = JJKAbilities.getValue(this.key); 
            ClientAbilityHandler.trigger(ability);
        }));

        ctx.setPacketHandled(true);
    }
}
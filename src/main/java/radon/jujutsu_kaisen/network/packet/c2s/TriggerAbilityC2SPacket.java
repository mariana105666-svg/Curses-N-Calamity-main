package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SetOverlayMessageS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.TriggerAbilityS2CPacket;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;

import java.util.function.Supplier;

public class TriggerAbilityC2SPacket {
    private final ResourceLocation key;

    public TriggerAbilityC2SPacket(ResourceLocation key) {
        this.key = key;
    }

    public TriggerAbilityC2SPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.key);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            

            assert sender != null;


            Ability.Status status;
            Ability ability = JJKAbilities.getValue(this.key);

            if ((status = AbilityHandler.trigger(sender, ability)) == Ability.Status.SUCCESS) {
                PacketHandler.sendToClient(new TriggerAbilityS2CPacket(JJKAbilities.getKey(ability)), sender );
            } else {    
                ISorcererData cap = sender.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
                if (cap == null) return;    

            switch (status) {
            case ENERGY ->
                    PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.energy", JujutsuKaisen.MOD_ID)),
                    false), sender);
            case COOLDOWN ->
                    PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.cooldown", JujutsuKaisen.MOD_ID),
                            Math.max(1, cap.getRemainingCooldown(ability) / 20)), false), sender);
            case BURNOUT ->
                     PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.burnout", JujutsuKaisen.MOD_ID), Math.max(1, cap.getBurnout() / 20)),
                    false), sender);
            case DISABLE ->
                    PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.disable", JujutsuKaisen.MOD_ID)),
                    false), sender);
            case FAILURE ->
                     PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.failure", JujutsuKaisen.MOD_ID)),
                    false), sender);
            case CHANT ->
                     PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.chant", JujutsuKaisen.MOD_ID)),
                    false), sender);
            case THROAT ->
                    PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.throat", JujutsuKaisen.MOD_ID),
                            Math.max(1, cap.getThroatDamage() / 20)), false), sender);
            case EMPTYINV ->
                     PacketHandler.sendToClient(new SetOverlayMessageS2CPacket(Component.translatable(String.format("ability.%s.fail.emptyinv", JujutsuKaisen.MOD_ID)),
                    false), sender);
            case SILENCED ->
                {}//PacketHandler.sendToClient(Component.translatable(String.format("ability.%s.fail.silenced", JujutsuKaisen.MOD_ID)), sender);
             case DISARMED ->
                {}//PacketHandler.sendToClient(Component.translatable(String.format("ability.%s.fail.disarmed", JujutsuKaisen.MOD_ID)), sender);
             case UNUSABLE ->
                {}//PacketHandler.sendToClient(Component.translatable(String.format("ability.%s.fail.unusable", JujutsuKaisen.MOD_ID)), sender);
            case SUCCESS ->
                {}
            }
        }
        });
        ctx.setPacketHandled(true);
    }
}
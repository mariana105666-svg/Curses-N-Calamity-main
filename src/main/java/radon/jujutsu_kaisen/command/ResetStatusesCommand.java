package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class ResetStatusesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("jjkresetstatuses")
                .requires((player) -> player.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.entity()).executes((ctx) ->
                        resetstatuses(EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("jjkresetstatuses").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int resetstatuses(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.setDisable(0);
        cap.setDisarmed(0);
        cap.setSelfHit(0);
        cap.setExtraMeleeTaken(0);
        cap.setBurnout(0);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

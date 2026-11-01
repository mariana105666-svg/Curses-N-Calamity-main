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
import radon.jujutsu_kaisen.util.PlayerUtil;

public class LockAllCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
            Commands.literal("jjklockall")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("players", EntityArgument.players())
                            .executes(ctx -> {
                                for (ServerPlayer player :
                                        EntityArgument.getPlayers(ctx, "players")) {
                                    lockall(player);
                                }
                                return 1;
                            })
                    )
        );

        dispatcher.register(Commands.literal("jjklockall").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int lockall(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.lockAll();
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

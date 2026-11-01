package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AddPointsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
        Commands.literal("jjkaddpoints")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                        .then(Commands.argument("points", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int points = IntegerArgumentType.getInteger(ctx, "points");

                                    for (ServerPlayer player :
                                            EntityArgument.getPlayers(ctx, "players")) {
                                        addPoints(player, points);
                                    }

                                    return 1;
                                })
                        )
                )
        );


        dispatcher.register(Commands.literal("jjkaddpoints").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int addPoints(ServerPlayer player, int points) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.addPoints(points);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

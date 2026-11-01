package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

public class WipeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
            Commands.literal("jjkwipe")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("players", EntityArgument.players())
                            .executes(ctx -> {
                                for (ServerPlayer player :
                                        EntityArgument.getPlayers(ctx, "players")) {
                                    wipe(player);
                                }
                                return 1;
                            })
                    )
        );

        dispatcher.register(Commands.literal("jjkwipe").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int wipe(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.wipe(player);

        return 1;
    }
}

package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class SetGradeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
        Commands.literal("jjksetgrade")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                        .then(Commands.argument("grade", EnumArgument.enumArgument(SorcererGrade.class))
                                .executes(ctx -> {
                                    SorcererGrade grade = ctx.getArgument("grade", SorcererGrade.class);

                                    for (ServerPlayer player :
                                            EntityArgument.getPlayers(ctx, "players")) {
                                        setGrade(player, grade);
                                    }

                                    return 1;
                                })
                        )
                )
        );


        dispatcher.register(Commands.literal("jjksetgrade").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int setGrade(ServerPlayer player, SorcererGrade grade) {
        player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
            cap.setGrade(grade);
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        });
        return 1;
    }
}

package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AddExperienceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
       LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
            Commands.literal("jjkaddexperience")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("players", EntityArgument.players())
                            .then(Commands.argument("experience", FloatArgumentType.floatArg())
                                    .executes(ctx -> {
                                        float amount = FloatArgumentType.getFloat(ctx, "experience");

                                        for (ServerPlayer player :
                                                EntityArgument.getPlayers(ctx, "players")) {
                                            addExperience(player, amount);
                                        }

                                        return 1;
                                    })
                            )
                    )
    );

        dispatcher.register(Commands.literal("jjkaddexperience").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int addExperience(ServerPlayer player, float experience) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.addExperience(experience);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

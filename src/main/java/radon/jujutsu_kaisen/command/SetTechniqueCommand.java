package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class SetTechniqueCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
                Commands.literal("jjksettechnique")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("technique",
                                        EnumArgument.enumArgument(CursedTechnique.class))
                                        .executes(ctx -> {
                                            CursedTechnique tech =
                                                    ctx.getArgument("technique", CursedTechnique.class);

                                            for (ServerPlayer player :
                                                    EntityArgument.getPlayers(ctx, "players")) {
                                                setTechnique(player, tech);
                                            }

                                            return 1;
                                        })
                                ))
        );

        dispatcher.register(Commands.literal("jjksettechnique").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int setTechnique(ServerPlayer player, CursedTechnique technique) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.clearToggled();
        cap.setTechnique(technique);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.clearToggled();
        cap.setTechnique(technique);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

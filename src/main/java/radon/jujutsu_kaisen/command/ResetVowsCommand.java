package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncTenShadowsDataS2CPacket;

public class ResetVowsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
        Commands.literal("jjkresetvows")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                        .executes(ctx -> {
                            for (ServerPlayer player :
                                    EntityArgument.getPlayers(ctx, "players")) {
                                resetvows(player);
                            }
                            return 1;
                        })
                )
        );


        dispatcher.register(Commands.literal("jjkresetvows").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int resetvows(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.resetVows();
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

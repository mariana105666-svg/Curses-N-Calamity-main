package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.SorcererUtil;

public class UnlockAllCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
            Commands.literal("jjkunlockall")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("players", EntityArgument.players())
                            .executes(ctx -> {
                                for (ServerPlayer player :
                                        EntityArgument.getPlayers(ctx, "players")) {
                                    unlockall(player);
                                }
                                return 1;
                            })
                    )
                    .executes(ctx -> unlockall(ctx.getSource().getPlayerOrException()))
        );

        dispatcher.register(Commands.literal("jjkunlockall").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int unlockall(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return 0;

        cap.setBlackFlashed(true);

        cap.unlockNode(ISorcererData.CE2_NODE);
        cap.unlockNode(ISorcererData.BARRIER_NODE);
        cap.unlockNode(ISorcererData.CE3_NODE);

        cap.setEfficiency(ISorcererData.CE_MAX);
        cap.setEfficiencyModerate(ISorcererData.CE2_MAX);
        cap.setEfficiencyAdvanced(ISorcererData.CE3_MAX);

        cap.unlockNode(ISorcererData.RCT_NODE);
        cap.unlock(JJKAbilities.RCT1.get());
        cap.setReverseEfficiency(ISorcererData.RCT_MAX);

        cap.setTechniqueMastery(ISorcererData.TECHNIQUE_MASTERY_MAX);

        SorcererUtil.checkMastery(player);

        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);

        return 1;
    }
}

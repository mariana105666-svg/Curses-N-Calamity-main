package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AddProgressCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("jjkaddprogress")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .executes(ctx -> addProgress(ctx.getSource(),
                                        ctx.getSource().getPlayerOrException(),
                                        FloatArgumentType.getFloat(ctx, "amount")))
                        )
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> {
                                            float amount = FloatArgumentType.getFloat(ctx, "amount");

                                            for (ServerPlayer player : EntityArgument.getPlayers(ctx, "players")) {
                                                addProgress(ctx.getSource(), player, amount);
                                            }

                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static int addProgress(CommandSourceStack source, ServerPlayer player, float amount) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return 0;

        cap.addProgress(amount);

        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);

        source.sendSuccess(() -> Component.literal(String.format(
                "%s: CE %.2f/%.0f, CE2 %.2f/%.0f, CE3 %.2f/%.0f",
                player.getGameProfile().getName(),
                cap.getEfficiency(), ISorcererData.CE_MAX,
                cap.getEfficiencyModerate(), ISorcererData.CE2_MAX,
                cap.getEfficiencyAdvanced(), ISorcererData.CE3_MAX)), true);

        return 1;
    }
}

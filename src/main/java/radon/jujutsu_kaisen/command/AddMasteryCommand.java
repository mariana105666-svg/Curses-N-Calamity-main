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
import radon.jujutsu_kaisen.util.SorcererUtil;

public class AddMasteryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("jjkaddmastery")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .executes(ctx -> addMastery(ctx.getSource(),
                                        ctx.getSource().getPlayerOrException(),
                                        FloatArgumentType.getFloat(ctx, "amount")))
                        )
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> {
                                            float amount = FloatArgumentType.getFloat(ctx, "amount");

                                            for (ServerPlayer player : EntityArgument.getPlayers(ctx, "players")) {
                                                addMastery(ctx.getSource(), player, amount);
                                            }

                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static int addMastery(CommandSourceStack source, ServerPlayer player, float amount) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return 0;

        cap.addTechniqueMastery(amount);

        SorcererUtil.checkMastery(player);

        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);

        source.sendSuccess(() -> Component.literal(String.format(
                "%s: Mastery %.2f/%.0f",
                player.getGameProfile().getName(),
                cap.getTechniqueMastery(), ISorcererData.TECHNIQUE_MASTERY_MAX)), true);

        return 1;
    }
}

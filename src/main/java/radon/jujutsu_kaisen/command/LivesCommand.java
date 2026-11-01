package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class LivesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
                Commands.literal("jjklives")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("lives", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    int lives = IntegerArgumentType.getInteger(ctx, "lives");
                                                    return setLives(ctx.getSource(), target, lives);
                                                }))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                    return addLives(ctx.getSource(), target, amount);
                                                }))))

                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            return getLives(ctx.getSource(), target);
                                        }))));
        dispatcher.register(Commands.literal("jjklives")
                .requires(source -> source.hasPermission(2))
                .redirect(node));
    }

    private static int setLives(CommandSourceStack source, ServerPlayer player, int count) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE)
                .resolve().orElseThrow();
        cap.setLives(count);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
    private static int addLives(CommandSourceStack source, ServerPlayer player, int amount) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE)
                .resolve().orElseThrow();
        int current = cap.getLives();
        int newCount = current + amount;
        cap.setLives(newCount);
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
    private static int getLives(CommandSourceStack source, ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE)
                .resolve().orElseThrow();
        int lives = cap.getLives();
        source.sendSuccess(() -> Component.literal(player.getName().getString() + " has " + lives + " lives."), false);
        return lives;
    }
}

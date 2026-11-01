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
import radon.jujutsu_kaisen.util.PlayerUtil;

public class RerollCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(
            Commands.literal("jjkreroll")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("players", EntityArgument.players())
                            .executes(ctx -> {
                                for (ServerPlayer player :
                                        EntityArgument.getPlayers(ctx, "players")) {
                                    reroll(player);
                                }
                                return 1;
                            })
                    )
        );
        dispatcher.register(Commands.literal("jjkreroll").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int reroll(ServerPlayer player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        PlayerUtil.removeAdvancement(player, "six_eyes");
        PlayerUtil.removeAdvancement(player, "heavenly_restriction");
        PlayerUtil.removeAdvancement(player, "vessel");
        PlayerUtil.removeAdvancement(player, "perfect_body");
        cap.resetCopy();
        cap.resetAbsorbed();
        cap.resetCurses();
        cap.setCurrentCopied(null);
        cap.clearToggled();
        cap.resetSteal();
        cap.lockAll();
        cap.resetVows();
        ITenShadowsData shadowCap = player.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElse(null);
        if (shadowCap != null) {
            shadowCap.revive(true);
        }
        cap.generate(player);

        return 1;
    }
}

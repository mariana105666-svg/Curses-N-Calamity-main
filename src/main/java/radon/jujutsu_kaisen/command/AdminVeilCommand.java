package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AdminVeilCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jjkadminveil")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> set(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))));
    }

    private static int set(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.translatable(String.format("commands.%s.admin_veil.not_a_player", JujutsuKaisen.MOD_ID)));
            return 0;
        }

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return 0;

        cap.setAdminVeil(enabled);

        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);

        source.sendSuccess(() -> Component.translatable(String.format("commands.%s.admin_veil.%s",
                JujutsuKaisen.MOD_ID, enabled ? "enabled" : "disabled")), false);

        return 1;
    }
}

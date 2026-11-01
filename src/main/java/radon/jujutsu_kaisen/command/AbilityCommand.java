package radon.jujutsu_kaisen.command;

import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AbilityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("jjkability")
                .requires((player) -> player.hasPermission(2))
                                .then(Commands.literal("unlock")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("ability", ResourceLocationArgument.id())
                                                        .suggests((ctx, builder) -> {
                                                            JJKAbilities.ABILITIES.getEntries().forEach(entry ->
                                                                    builder.suggest(entry.getId().toString()));
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "ability");
                                                            return unlockAbility(player, id);
                                                        }))))
                                .then(Commands.literal("lock")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("ability", ResourceLocationArgument.id())
                                                        .suggests((ctx, builder) -> {
                                                            JJKAbilities.ABILITIES.getEntries().forEach(entry ->
                                                                    builder.suggest(entry.getId().toString()));
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "ability");
                                                            return lockAbility(player, id);
                                                        }))))
        );

        dispatcher.register(Commands.literal("jjkability").requires((player) -> player.hasPermission(2)).redirect(node));
    }

   public static int unlockAbility(ServerPlayer player, ResourceLocation id) {
        Optional<Ability> ability = JJKAbilities.ABILITIES.getEntries().stream()
                .filter(e -> e.getId().equals(id))
                .map(e -> e.get())
                .findFirst();

        if (ability.isEmpty()) {
            return 0;
        }

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.unlock(ability.get());
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }

    public static int lockAbility(ServerPlayer player, ResourceLocation id) {
        Optional<Ability> ability = JJKAbilities.ABILITIES.getEntries().stream()
                .filter(e -> e.getId().equals(id))
                .map(e -> e.get())
                .findFirst();

        if (ability.isEmpty()) {
            return 0;
        }

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        cap.lock(ability.get());
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
        return 1;
    }
}

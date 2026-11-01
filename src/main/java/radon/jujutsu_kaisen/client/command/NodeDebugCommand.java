package radon.jujutsu_kaisen.client.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.client.gui.screen.tab.AbilityTab;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NodeDebugCommand {
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("jjknodedebug")
                        .executes(ctx -> {
                            AbilityTab.DEBUG_NODES = !AbilityTab.DEBUG_NODES;

                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    AbilityTab.DEBUG_NODES
                                            ? "Node debug on: every node is shown"
                                            : "Node debug off: only unlocked nodes are shown"), false);

                            return 1;
                        })
        );
    }
}

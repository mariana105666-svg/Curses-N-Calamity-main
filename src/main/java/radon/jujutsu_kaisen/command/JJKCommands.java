package radon.jujutsu_kaisen.command;


import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JJKCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SetGradeCommand.register(event.getDispatcher());
        SetTechniqueCommand.register(event.getDispatcher());
        TraitCommand.register(event.getDispatcher());
        SetTypeCommand.register(event.getDispatcher());
        RerollCommand.register(event.getDispatcher());
        WipeCommand.register(event.getDispatcher());
        ResetSummonsCommand.register(event.getDispatcher());
        SetNatureCommand.register(event.getDispatcher());
        SetExperienceCommand.register(event.getDispatcher());
        AddExperienceCommand.register(event.getDispatcher());
        PactCreationAcceptCommand.register(event.getDispatcher());
        PactCreationDeclineCommand.register(event.getDispatcher());
        PactRemovalAcceptCommand.register(event.getDispatcher());
        PactRemovalDeclineCommand.register(event.getDispatcher());
        AddPointsCommand.register(event.getDispatcher());
        AddProgressCommand.register(event.getDispatcher());
        AddMasteryCommand.register(event.getDispatcher());
        RefillCommand.register(event.getDispatcher());
        AbilityCommand.register(event.getDispatcher());
        UnlockAllCommand.register(event.getDispatcher());
        LockAllCommand.register(event.getDispatcher());
        SetExtraEnergyCommand.register(event.getDispatcher());
        SetAdditionalTechniqueCommand.register(event.getDispatcher());
        LivesCommand.register(event.getDispatcher());
        ResetVowsCommand.register(event.getDispatcher());
        ResetStatusesCommand.register(event.getDispatcher());
        AdminVeilCommand.register(event.getDispatcher());
        GuaranteedBlackFlashCommand.register(event.getDispatcher());
    }
}
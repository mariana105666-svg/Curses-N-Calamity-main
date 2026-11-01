// package radon.jujutsu_kaisen.client;

// import net.minecraftforge.event.entity.player.PlayerEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;
// import net.minecraftforge.api.distmarker.Dist;
// import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
// import radon.jujutsu_kaisen.JujutsuKaisen;

// @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = JujutsuKaisen.MOD_ID)
// public class ClientJoinHandler {

//     @SubscribeEvent
//     public static void onClientJoin(PlayerEvent.PlayerLoggedInEvent event) {
//         if (!(event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player)) return;

//         player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
//             if (cap.getStolenSkinProfile() != null && cap.getStolenSkinTexture() == null) {
//                 ClientSkinHandler.handleSkinSync(cap, cap);
//             }
//         });
//     }
// }
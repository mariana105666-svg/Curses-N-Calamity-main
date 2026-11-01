package radon.jujutsu_kaisen.client.ability;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Ability.IPosedMove;
import radon.jujutsu_kaisen.ability.AbilityTriggerEvent;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.ITransformation;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.client.JJKKeys;
import radon.jujutsu_kaisen.client.gui.MeleeMenuType;
import radon.jujutsu_kaisen.client.gui.overlay.AbilityOverlay;
import radon.jujutsu_kaisen.client.gui.screen.AbilityScreen;
import radon.jujutsu_kaisen.client.gui.screen.DomainScreen;
import radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen;
import radon.jujutsu_kaisen.client.gui.screen.JutwotsuScreen;
import radon.jujutsu_kaisen.client.gui.screen.MeleeScreen;
import radon.jujutsu_kaisen.client.gui.screen.ShadowInventoryScreen;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.base.IJumpInputListener;
import radon.jujutsu_kaisen.entity.base.IRightClickInputListener;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.ChangeOutputC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.JumpInputListenerC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.OpenInventoryCurseC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.RightClickInputListenerC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.TriggerAbilityC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.UntriggerAbilityC2SPacket;
import radon.jujutsu_kaisen.util.CuriosUtil;
import radon.jujutsu_kaisen.util.EntityUtil;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ClientAbilityHandler {
    private static @Nullable Ability channeled;
    private static @Nullable KeyMapping current;
    private static boolean isChanneling;
    private static boolean isRightDown;
    private static final Map<KeyMapping, Ability> activeChannels = new HashMap<>();
    private static final Map<KeyMapping, Boolean> isChannelingMap = new HashMap<>();
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientAbilityHandlerForgeEvents {
        @SubscribeEvent
        public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
            
            LivingEntity entity = event.getEntity();

            ClientVisualHandler.ClientData data = ClientVisualHandler.get(entity);

            if (data == null) return;

            if (!(event.getRenderer().getModel() instanceof HumanoidModel<?> humanoid))
                return;

            PlayerModel<?> player = null;
            if (humanoid instanceof PlayerModel<?> pm) {
                player = pm;
            }

            for (Ability ability : data.toggled) {
                if (!(ability instanceof ITransformation transformation)) continue;
                if (!transformation.isReplacement()) continue;
                    switch (transformation.getBodyPart()) {
                        case HEAD -> {
                            humanoid.head.visible = false;
                            if (player != null) {
                                player.hat.visible = false;
                            }
                        }
                        case BODY -> {
                            humanoid.body.visible = false;
                            if (player != null) {
                                player.jacket.visible = false;
                            }
                        }
                        case RIGHT_ARM -> {
                            humanoid.rightArm.visible = false;
                            if (player != null) {
                                player.rightSleeve.visible = false;
                            }
                        }
                        case LEFT_ARM -> {
                            humanoid.leftArm.visible = false;
                            if (player != null) {
                                player.leftSleeve.visible = false;
                            }
                        }
                        case LEGS -> {
                            humanoid.rightLeg.visible = false;
                            humanoid.leftLeg.visible = false;
                            if (player != null) {
                                player.rightPants.visible = false;
                                player.leftPants.visible = false;
                            }
                        }
                    }

                HumanoidModel.ArmPose pose = IClientItemExtensions.of(transformation.getItem()).getArmPose(event.getEntity(), InteractionHand.MAIN_HAND, transformation.getItem().getDefaultInstance());

                if (pose != null) {
                    if (transformation.getBodyPart() == ITransformation.Part.RIGHT_ARM) {
                        humanoid.rightArmPose = pose;
                    } else if (transformation.getBodyPart() == ITransformation.Part.LEFT_ARM) {
                        humanoid.leftArmPose = pose;
                    }
                }
            }
            // HumanoidModel.ArmPose right = PoseHandler.resolve(entity, HumanoidArm.RIGHT);
            // HumanoidModel.ArmPose left = PoseHandler.resolve(entity, HumanoidArm.LEFT);

            // if (right != null) humanoid.rightArmPose = right;
            // if (left  != null) humanoid.leftArmPose  = left;
        }
            


    private static void channel(@Nullable Ability ability, @Nullable KeyMapping key) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        channeled = ability;
        current = key;
        isChanneling = false;
    }
        

        @SubscribeEvent
public static void onClientTick(TickEvent.ClientTickEvent event) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;

   if (current != null && channeled != null) {
                boolean isHeld = current.isDown();

                if (isHeld) {
                    if (!isChanneling) {
                        PacketHandler.sendToServer(new TriggerAbilityC2SPacket(JJKAbilities.getKey(channeled)));
                    }
                    isChanneling = true;
                } else if (isChanneling) {
                    AbilityHandler.untrigger(mc.player, channeled);
                    PacketHandler.sendToServer(new UntriggerAbilityC2SPacket(JJKAbilities.getKey(channeled)));

                    channel(null, null);
                }
            }
    if (mc.player.getVehicle() instanceof IRightClickInputListener listener) {
        if (!isRightDown && mc.mouseHandler.isRightPressed()) {
            listener.setDown(true);
            PacketHandler.sendToServer(new RightClickInputListenerC2SPacket(true));
            isRightDown = true;
        } else if (isRightDown && !mc.mouseHandler.isRightPressed()) {
            listener.setDown(false);
            PacketHandler.sendToServer(new RightClickInputListenerC2SPacket(false));
            isRightDown = false;
        }
    }

    // if (JJKKeys.OPEN_JUJUTSU_MENU.consumeClick()) {
    //     mc.setScreen(new JujutsuScreen());
    // }
    // if (JJKKeys.SHOW_ABILITY_MENU.consumeClick()) {
    //     mc.setScreen(new AbilityScreen());
    // }
    // if (JJKKeys.SHOW_DOMAIN_MENU.consumeClick()) {
    //     mc.setScreen(new DomainScreen());
    // }
    // if (ConfigHolder.CLIENT.meleeMenuType.get() == MeleeMenuType.TOGGLE && JJKKeys.ACTIVATE_MELEE_MENU.consumeClick()) {
    //     mc.setScreen(new MeleeScreen());
    // }
    // if (JJKKeys.ACTIVATE_J2TSU_MENU.consumeClick()) {
    //     mc.setScreen(new JutwotsuScreen());
    // }
    
}
      
          private static void handleInput(int inputObj, int action) {
 Minecraft mc = Minecraft.getInstance();

            if (mc.player == null) return;

            if (inputObj == KeyEvent.VK_SPACE || inputObj == GLFW.GLFW_KEY_SPACE) {
                if (action == InputConstants.PRESS || action == InputConstants.RELEASE) {
                    boolean down = action == InputConstants.PRESS;

                    if (mc.player.getVehicle() instanceof IJumpInputListener listener) {
                        listener.setJump(down);
                        PacketHandler.sendToServer(new JumpInputListenerC2SPacket(down));
                    } else if (mc.player.getFirstPassenger() instanceof IJumpInputListener listener) {
                        listener.setJump(down);
                        PacketHandler.sendToServer(new JumpInputListenerC2SPacket(down));
                    }
                }
            }

            if (action == InputConstants.PRESS) {
                if (JJKKeys.ACTIVATE_ABILITY.isDown()) {
                    Ability ability = AbilityOverlay.getSelected();

                    if (ability != null) {
                        if (ability.getActivationType(mc.player) == Ability.ActivationType.CHANNELED) {
                             if (channeled == null) {
                                channel(ability, JJKKeys.ACTIVATE_ABILITY);
                            }
                        } else if (JJKKeys.ACTIVATE_ABILITY.consumeClick()){
                            //if (ClientAbilityHandler.trigger(ability) == Ability.Status.SUCCESS) {
                                PacketHandler.sendToServer(new TriggerAbilityC2SPacket(JJKAbilities.getKey(ability)));
                        }
                    }
                }

                if (JJKKeys.ACTIVATE_J2TSU.isDown()) {
                    Ability ability = AbilityOverlay.getSelected2();

                    if (ability != null) {
                        if (ability.getActivationType(mc.player) == Ability.ActivationType.CHANNELED) {
                            if (channeled == null) {
                                channel(ability, JJKKeys.ACTIVATE_J2TSU);
                            }
                      
                         } else if (JJKKeys.ACTIVATE_J2TSU.consumeClick()){
                            //if (ClientAbilityHandler.trigger(ability) == Ability.Status.SUCCESS) {
                                PacketHandler.sendToServer(new TriggerAbilityC2SPacket(JJKAbilities.getKey(ability)));
                            //}
                        }
                    }
                }

                if (JJKKeys.OPEN_INVENTORY_CURSE.consumeClick() && (mc.player.getItemBySlot(EquipmentSlot.CHEST).is(JJKItems.INVENTORY_CURSE.get()) ||
                        CuriosUtil.findSlot(mc.player, "body").is(JJKItems.INVENTORY_CURSE.get()))) {
                    PacketHandler.sendToServer(new OpenInventoryCurseC2SPacket());
                }
                if (JJKKeys.OPEN_JUJUTSU_MENU.consumeClick()) {
                    mc.setScreen(new JujutsuScreen());
                }
                if (JJKKeys.SHOW_ABILITY_MENU.consumeClick()) {
                    mc.setScreen(new AbilityScreen());
                }
                if (JJKKeys.SHOW_DOMAIN_MENU.consumeClick()) {
                    mc.setScreen(new DomainScreen());
                }
                  if (ConfigHolder.CLIENT.meleeMenuType.get() == MeleeMenuType.TOGGLE && JJKKeys.ACTIVATE_MELEE_MENU.consumeClick()) {
                    mc.setScreen(new MeleeScreen());
                }
                if (JJKKeys.ACTIVATE_J2TSU_MENU.consumeClick()) {
                    mc.setScreen(new JutwotsuScreen());
                }
                if (JJKKeys.INCREASE_OUTPUT.consumeClick()) {
                    ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    PacketHandler.sendToServer(new ChangeOutputC2SPacket(ChangeOutputC2SPacket.INCREASE));
                    cap.increaseOutput();
                }
                if (JJKKeys.DECREASE_OUTPUT.consumeClick()) {
                    ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    PacketHandler.sendToServer(new ChangeOutputC2SPacket(ChangeOutputC2SPacket.DECREASE));
                    cap.decreaseOutput();
                }   

                if (JJKKeys.ACTIVATE_RCT_OR_HEAL.isDown()) {
                    Ability rct = EntityUtil.getRCTTier(mc.player);
                    Ability ability = JJKAbilities.getType(mc.player) == JujutsuType.CURSE ? JJKAbilities.HEAL.get() : rct;
                    if (ability != null) {
                        if (channeled == null) {
                            channel(ability, JJKKeys.ACTIVATE_RCT_OR_HEAL);
                        }
                    }
                }

                if (JJKKeys.ACTIVATE_CURSED_ENERGY_SHIELD.isDown()) {
                    if (channeled == null) {
                        channel(JJKAbilities.CURSED_ENERGY_SHIELD.get(),JJKKeys.ACTIVATE_CURSED_ENERGY_SHIELD );
                    }
                }

                if (JJKKeys.DASH.consumeClick()) {
                    //if (ClientAbilityHandler.trigger(JJKAbilities.DASH.get()) == Ability.Status.SUCCESS) {
                        PacketHandler.sendToServer(new TriggerAbilityC2SPacket(JJKAbilities.getKey(JJKAbilities.DASH.get())));
                    //}
                }
                if (JJKKeys.QUICK_VEIL.consumeClick()) {
                    if (radon.jujutsu_kaisen.client.gui.screen.JujutsuScreen.hasQuickVeil()) {
                        PacketHandler.sendToServer(new radon.jujutsu_kaisen.network.packet.c2s.QuickVeilC2SPacket(
                                radon.jujutsu_kaisen.client.veil.QuickVeilSettings.toModifiers(),
                                radon.jujutsu_kaisen.client.veil.QuickVeilSettings.getSize()));
                    }
                }
                if (JJKKeys.QUICKDASH.consumeClick()) {
                    //if (ClientAbilityHandler.trigger(JJKAbilities.QUICKDASH.get()) == Ability.Status.SUCCESS) {
                        PacketHandler.sendToServer(new TriggerAbilityC2SPacket(JJKAbilities.getKey(JJKAbilities.QUICKDASH.get())));
                    //}
                }
            } else if (action == InputConstants.RELEASE) {
                // if (current != null) {
                //     boolean possiblyChanneling = channeled != null;

                //     if (possiblyChanneling) {
                //         if (inputObj == current.getKey().getValue()) {
                //             AbilityHandler.untrigger(mc.player, channeled);
                //             PacketHandler.sendToServer(new UntriggerAbilityC2SPacket(JJKAbilities.getKey(channeled)));

                //             channeled = null;
                //             current = null;
                //             isChanneling = false;
                //         }
                //     }
                // }
                if ((inputObj == JJKKeys.SHOW_ABILITY_MENU.getKey().getValue() && mc.screen instanceof AbilityScreen) ||
                        (inputObj == JJKKeys.SHOW_DOMAIN_MENU.getKey().getValue() && mc.screen instanceof DomainScreen) ||
                        (inputObj == JJKKeys.ACTIVATE_J2TSU.getKey().getValue() && mc.screen instanceof ShadowInventoryScreen)) {
                    mc.screen.onClose();
                }
            }
        
        
        
        }
         
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInput(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        handleInput(event.getButton(), event.getAction());
    }
    }

      

    // public static boolean isSuccess(Ability ability, Ability.Status status) {
    //     Minecraft mc = Minecraft.getInstance();
    //     LocalPlayer owner = mc.player;

    //     if (owner == null) return false;

    //     ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //     switch (status) {
    //         case ENERGY ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.energy", JujutsuKaisen.MOD_ID)), false);
    //         case COOLDOWN ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.cooldown", JujutsuKaisen.MOD_ID),
    //                         Math.max(1, cap.getRemainingCooldown(ability) / 20)), false);
    //         case BURNOUT ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.burnout", JujutsuKaisen.MOD_ID),
    //                     cap.getBurnout() / 20), false);
    //         case DISABLE ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.disable", JujutsuKaisen.MOD_ID)), false);     
    //         case FAILURE ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.failure", JujutsuKaisen.MOD_ID)), false);
    //         case CHANT ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.chant", JujutsuKaisen.MOD_ID)), false);
    //         case THROAT ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.throat", JujutsuKaisen.MOD_ID), 
    //                 Math.max(1, cap.getThroatDamage() / 20)), false);
    //         case EMPTYINV ->
    //                 mc.gui.setOverlayMessage(Component.translatable(String.format("ability.%s.fail.emptyinv", JujutsuKaisen.MOD_ID)), false);
    //         }
    //     return status == Ability.Status.SUCCESS;
    // }

    public static Ability.Status trigger(Ability ability) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer owner = mc.player;

        if (owner == null) return Ability.Status.FAILURE;

        if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return Ability.Status.FAILURE;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (ability.getActivationType(owner) == Ability.ActivationType.INSTANT) {
            ability.charge(owner);
            ability.addDuration(owner);
            //if (isSuccess(ability, (status = ability.isTriggerable(owner)))) {
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                ability.run(owner);
          
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));
            //}
           // return status;
        } else if (ability.getActivationType(owner) == Ability.ActivationType.TOGGLED  ) {
            ability.addDuration(owner);
            //Ability.Status status;

            //if (isSuccess(ability, (status = ability.isTriggerable(owner))) | (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.toggle(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));
            //}
            //return status;
        } else if (ability.getActivationType(owner) == Ability.ActivationType.DOMAIN) {
            // if (!cap.hasToggled(ability)) {
            //     ability.charge(owner);
            // }
            ability.addDuration(owner);
            //Ability.Status status;

            //if (isSuccess(ability, (status = ability.isTriggerable(owner))) | (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.toggle(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));
            //}
            //return status;
        
        } else if (ability.getActivationType(owner) == Ability.ActivationType.CHANNELED) {
            ability.addDuration(owner);
            //Ability.Status status;

            //if (isSuccess(ability, (status = ability.isTriggerable(owner))) || (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Pre(owner, ability));
                cap.channel(ability);
                MinecraftForge.EVENT_BUS.post(new AbilityTriggerEvent.Post(owner, ability));
            //}
            //return status;
        }
        return Ability.Status.SUCCESS;
    }
}

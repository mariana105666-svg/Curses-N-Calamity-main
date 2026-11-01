package radon.jujutsu_kaisen.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import radon.jujutsu_kaisen.JujutsuKaisen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import radon.jujutsu_kaisen.client.particle.SlicedEntityParticle;
import radon.jujutsu_kaisen.client.slice.CutModelUtil;
import radon.jujutsu_kaisen.client.slice.GJK;
import radon.jujutsu_kaisen.client.slice.RigidBody;

import radon.jujutsu_kaisen.util.HelperMethods;

import java.util.*;


import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public class SlicedEntityRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
            for (SlicedEntityParticle sliced : SlicedEntityParticle.getAll()) {
                sliced.actuallyRender(event.getPoseStack(), event.getPartialTick());
            }
    }
}
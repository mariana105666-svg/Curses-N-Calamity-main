package radon.jujutsu_kaisen.client.puppet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.puppet_manipulation.Excavate;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.ExcavateCancelC2SPacket;
import radon.jujutsu_kaisen.network.packet.c2s.ExcavateStartC2SPacket;
import radon.jujutsu_kaisen.puppet.ExcavateJob;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExcavateClient {
    @Nullable
    private static BlockPos first;

    @Nullable
    private static BlockPos second;

    private static boolean working;

    private ExcavateClient() {
    }

    public static boolean isWorking() {
        return working;
    }

    public static void setWorking(boolean value) {
        working = value;

        if (!value) reset();
    }

    public static void reset() {
        first = null;
        second = null;
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return false;

        ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasToggled(JJKAbilities.EXCAVATE.get());
    }

    @Nullable
    public static BlockHitResult target() {
        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;

        if (player == null) return null;

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(Excavate.RANGE));

        BlockHitResult hit = player.level().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        return hit.getType() == HitResult.Type.MISS ? null : hit;
    }

    public static void select() {
        if (working) return;

        BlockHitResult hit = target();

        if (hit == null) return;

        if (first == null) {
            first = hit.getBlockPos().immutable();
            return;
        }

        second = hit.getBlockPos().immutable();

        if (ExcavateJob.volume(first, second) > ExcavateJob.MAX_VOLUME) {
            second = null;
            return;
        }

        PacketHandler.sendToServer(new ExcavateStartC2SPacket(first, second));
    }

    public static void stop() {
        PacketHandler.sendToServer(new ExcavateCancelC2SPacket());

        working = false;

        reset();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!isActive()) return;

        List<Highlight.Region> regions = new ArrayList<>();

        if (first != null && second != null) {
            regions.add(Highlight.Region.of(first, second, 0.35F, 0.55F, 1.0F));
        } else {
            if (first != null) regions.add(Highlight.Region.of(first, 1.0F, 1.0F, 1.0F));

            BlockHitResult hit = working ? null : target();

            if (hit != null) {
                BlockPos pos = hit.getBlockPos();

                if (first == null) {
                    regions.add(Highlight.Region.of(pos, 1.0F, 1.0F, 1.0F));
                } else {
                    regions.add(Highlight.Region.of(pos, 0.35F, 0.55F, 1.0F));
                }
            }
        }

        Highlight.render(event.getPoseStack(), event.getCamera().getPosition(), regions);
    }
}

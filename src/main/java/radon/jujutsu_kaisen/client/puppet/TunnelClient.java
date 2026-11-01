package radon.jujutsu_kaisen.client.puppet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import radon.jujutsu_kaisen.ability.puppet_manipulation.Tunnel;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.puppet.TunnelPattern;
import radon.jujutsu_kaisen.puppet.TunnelShape;

import java.util.List;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TunnelClient {
    private static TunnelPattern pattern = TunnelPattern.SINGLE;

    private static boolean working;

    private TunnelClient() {
    }

    public static TunnelPattern getPattern() {
        return pattern;
    }

    public static void cyclePattern() {
        if (working) return;

        pattern = pattern.next();
    }

    public static boolean isWorking() {
        return working;
    }

    public static void setWorking(boolean value) {
        working = value;
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return false;

        ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap != null && cap.hasToggled(JJKAbilities.TUNNEL.get());
    }

    @Nullable
    public static BlockHitResult target() {
        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;

        if (player == null) return null;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(Tunnel.RANGE));

        BlockHitResult hit = player.level().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        return hit.getType() == HitResult.Type.MISS ? null : hit;
    }

    public static Direction facing(BlockHitResult hit) {
        Minecraft mc = Minecraft.getInstance();

        Direction opposite = hit.getDirection().getOpposite();

        if (opposite.getAxis().isHorizontal()) return opposite;

        return mc.player == null ? Direction.NORTH : mc.player.getDirection();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (working || !isActive()) return;

        BlockHitResult hit = target();

        if (hit == null) return;

        List<BlockPos> positions = TunnelShape.preview(hit.getBlockPos(), facing(hit), pattern);

        if (positions.isEmpty()) return;

        BlockPos min = positions.get(0);
        BlockPos max = min;

        for (BlockPos pos : positions) {
            min = new BlockPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()),
                    Math.min(min.getZ(), pos.getZ()));
            max = new BlockPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()),
                    Math.max(max.getZ(), pos.getZ()));
        }

        Highlight.render(event.getPoseStack(), event.getCamera().getPosition(),
                List.of(Highlight.Region.of(min, max, 1.0F, 1.0F, 1.0F)));
    }
}

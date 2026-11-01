package radon.jujutsu_kaisen.client.render.domain;


import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.joml.Matrix4f;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.DomainExpansion;
import radon.jujutsu_kaisen.client.JJKShaders;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public class DomainRenderDispatcher {
    private static final Map<ResourceLocation, DomainRenderer> renderers = new HashMap<>();
    private static final Map<ResourceLocation, TextureTarget> cached = new HashMap<>();

    static {
        renderers.put(JJKAbilities.UNLIMITED_VOID.getId(), new UnlimitedVoidRenderer());
        renderers.put(JJKAbilities.TIME_CELL_MOON_PALACE.getId(), new TimeCellMoonPalaceDomainRenderer());
        renderers.put(JJKAbilities.SELF_EMBODIMENT_OF_PERFECTION.getId(), new SelfEmbodimentOfPerfectionDomainRenderer());
        renderers.put(JJKAbilities.MALEVOLENT_SHRINE.getId(), new MalevolentShrineRenderer());
        renderers.put(JJKAbilities.SWEET_BURNING_MATSURI.getId(), new SweetBurningMatsuriRenderer());
//        renderers.put(JJKAbilities.COFFIN_OF_THE_IRON_MOUNTAIN.getId(), new CoffinOfTheIronMountainRenderer());
//        renderers.put(JJKAbilities.AUTHENTIC_MUTUAL_LOVE.getId(), new AuthenticMutualLoveRenderer());
    }

    private static final DomainRenderer DEFAULT_RENDERER = new DefaultDomainRenderer();
    @Nullable
    private static TextureTarget fallback;

    private static int skyWidth;
    private static int skyHeight;

    public static TextureTarget get(ResourceLocation domain) {
        if (!cached.containsKey(domain)) {
            return fallback;
        }
        return cached.get(domain);
    }

    private static TextureTarget update(@Nullable TextureTarget target, DomainRenderer renderer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, boolean update) {
        Minecraft mc = Minecraft.getInstance();

        if (target == null || update) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new TextureTarget(skyWidth, skyHeight, true, Minecraft.ON_OSX);
        }

        target.clear(Minecraft.ON_OSX);

        target.bindWrite(true);

        render(renderer, modelViewMatrix, projectionMatrix);

        target.unbindRead();
        target.unbindWrite();

        mc.getMainRenderTarget().bindWrite(true);

        return target;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();

        Window window = mc.getWindow();
        int ww = window.getWidth();
        int wh = window.getHeight();

        if (ww <= 0 || wh <= 0) return;

        boolean update = false;

        if (skyWidth != ww || skyHeight != wh) {
            update = true;
            skyWidth = ww;
            skyHeight = wh;
        }

        Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getInverseViewRotationMatrix()).invert();
        Matrix4f projectionMatrix = event.getProjectionMatrix();

        for (Map.Entry<ResourceLocation, DomainRenderer> entry : renderers.entrySet()) {
            ResourceLocation key = entry.getKey();
            DomainRenderer renderer = entry.getValue();
            TextureTarget target = cached.get(key);
            TextureTarget updated = update(target, renderer, modelViewMatrix, projectionMatrix, update);
            cached.put(key, updated);
        }

        fallback = update(fallback, DEFAULT_RENDERER, modelViewMatrix, projectionMatrix, update);
    }

    public static void render(DomainRenderer renderer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        RenderSystem.enableBlend();

        RenderSystem.setShaderTexture(0, renderer.getTexture());

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        renderer.render(modelViewMatrix, projectionMatrix);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        for (DomainRenderLayer layer : renderer.getLayers()) {
            //if (layer.shouldRender(currentTick)) {
            RenderSystem.setShaderTexture(0, layer.getTexture());

            renderer.render(modelViewMatrix, projectionMatrix);
            //}
        }

        RenderSystem.defaultBlendFunc();

        RenderSystem.disableBlend();
    }
}

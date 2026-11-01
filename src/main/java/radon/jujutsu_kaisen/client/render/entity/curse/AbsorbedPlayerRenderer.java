package radon.jujutsu_kaisen.client.render.entity.curse;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.client.layer.JJKOverlayLayer;

public class AbsorbedPlayerRenderer extends HumanoidMobRenderer<radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity, PlayerModel<radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity>> {
    private final PlayerModel<radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity> normal;
    private final PlayerModel<radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity> slim;

    private final UUID randuuid = UUID.randomUUID();

    public AbsorbedPlayerRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, null, 0.5F);

        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), pContext.getModelManager()));
        this.addLayer(new JJKOverlayLayer<>(this));

        this.normal = new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(@NotNull radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        Minecraft mc = Minecraft.getInstance();

        assert mc.level != null;

        GameProfile profile = pEntity.getPlayer();
        if (profile != null) {
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        PlayerInfo info = conn == null ? null : conn.getPlayerInfo(profile.getId());
        this.model = (info == null ? DefaultPlayerSkin.getSkinModelName(profile.getId()) : info.getModelName()).equals("default") ? this.normal : this.slim;
        }
        else {
            this.model = this.normal;
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull radon.jujutsu_kaisen.entity.curse.AbsorbedPlayerEntity pEntity) {
        GameProfile profile = pEntity.getPlayer();
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        PlayerInfo info = null;
        if (profile != null) {
             info = conn == null ? null : conn.getPlayerInfo(profile.getId());
             this.model = (info == null ? DefaultPlayerSkin.getSkinModelName(profile.getId()) : info.getModelName()).equals("default") ? this.normal : this.slim;
        }
        else {
            this.model = this.normal;
            return DefaultPlayerSkin.getDefaultSkin(randuuid);

        }
        return info == null ? DefaultPlayerSkin.getDefaultSkin(profile.getId()) : info.getSkinLocation();
    }
}

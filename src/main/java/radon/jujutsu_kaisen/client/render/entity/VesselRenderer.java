package radon.jujutsu_kaisen.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.client.layer.JJKOverlayLayer;
import radon.jujutsu_kaisen.entity.VesselEntity;

import java.util.UUID;

public class VesselRenderer extends HumanoidMobRenderer<VesselEntity, PlayerModel<VesselEntity>> {
    private final PlayerModel<VesselEntity> normal;
    private final PlayerModel<VesselEntity> slim;

    public VesselRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager()));
        this.addLayer(new JJKOverlayLayer<>(this));

        this.normal = new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    private static PlayerInfo info(VesselEntity vessel) {
        UUID owner = vessel.getOwnerUUID();

        if (owner == null) return null;

        ClientPacketListener connection = Minecraft.getInstance().getConnection();

        return connection == null ? null : connection.getPlayerInfo(owner);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull VesselEntity pEntity) {
        UUID owner = pEntity.getOwnerUUID();

        if (owner == null) return DefaultPlayerSkin.getDefaultSkin();

        PlayerInfo info = info(pEntity);

        this.model = (info == null ? DefaultPlayerSkin.getSkinModelName(owner) : info.getModelName())
                .equals("default") ? this.normal : this.slim;

        return info == null ? DefaultPlayerSkin.getDefaultSkin(owner) : info.getSkinLocation();
    }
}

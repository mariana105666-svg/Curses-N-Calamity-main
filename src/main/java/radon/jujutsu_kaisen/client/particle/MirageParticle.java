package radon.jujutsu_kaisen.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.client.FakeEntityRenderer;
import radon.jujutsu_kaisen.client.MixinData;

import java.util.Locale;

public class MirageParticle<T extends MirageParticle.MirageParticleOptions> extends TextureSheetParticle {
    private final int entityId;

    @Nullable
    private Entity entity;

    @Nullable
    private FakeEntityRenderer renderer;

    protected MirageParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, T options) {
        super(pLevel, pX, pY, pZ);

        this.lifetime = 10;

        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;

        this.entityId = options.entityId();
    }

    @Override
    public void tick() {
        super.tick();

        this.alpha = 1.0F - ((float) this.age / this.lifetime);

        if (this.entity == null) {
            this.entity = this.level.getEntity(this.entityId);

            if (this.entity == null) return;

            this.renderer = new FakeEntityRenderer(this.entity);
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

     @Override
    public void render(@NotNull VertexConsumer pBuffer, @NotNull Camera pRenderInfo, float pPartialTicks) {
        if (this.renderer == null) return;

        this.renderer.setAlpha(this.alpha);

        this.renderer.render(this.getPos(), pPartialTicks);
    }

    public record MirageParticleOptions(int entityId) implements ParticleOptions {
        public static Deserializer<MirageParticleOptions> DESERIALIZER = new Deserializer<>() {
            public @NotNull MirageParticle.MirageParticleOptions fromCommand(@NotNull ParticleType<MirageParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
                return new MirageParticleOptions(reader.readInt());
            }

            public @NotNull MirageParticle.MirageParticleOptions fromNetwork(@NotNull ParticleType<MirageParticleOptions> type, @NotNull FriendlyByteBuf buf) {
                return new MirageParticleOptions(buf.readInt());
            }
        };

        @Override
        public @NotNull ParticleType<?> getType() {
            return JJKParticles.MIRAGE.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeInt(this.entityId);
        }

        @Override
        public @NotNull String writeToString() {
            return String.format(Locale.ROOT, "%s %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.entityId);
        }
    }

    public static class Provider implements ParticleProvider<MirageParticleOptions> {
        public Provider(SpriteSet ignored) {}

        public Particle createParticle(@NotNull MirageParticle.MirageParticleOptions pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            return new MirageParticle<>(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pType);
        }
    }
}

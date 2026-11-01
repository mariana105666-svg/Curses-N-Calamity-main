package radon.jujutsu_kaisen.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;
import radon.jujutsu_kaisen.client.render.entity.effect.BoltEffect;
import radon.jujutsu_kaisen.client.render.entity.effect.BoltRenderer;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.Locale;

public class EmittingLightningParticle extends TextureSheetParticle {
    private final Vector3f color;

    private final BoltRenderer renderer;
    private final float scale;
    private final Vec3 direction;
    private final boolean multi;

    protected EmittingLightningParticle(ClientLevel pLevel, double pX, double pY, double pZ, EmittingLightningParticleOptions options) {
        super(pLevel, pX, pY, pZ);

        this.color = options.color();
        this.direction = options.direction();
        this.scale = Math.max(options.scalar(), (this.random.nextFloat() - 0.5F) * options.scalar());
        this.setSize(this.scale, this.scale);
        //this.quadSize = Math.max(options.scalar(), (this.random.nextFloat() - 0.5F) * options.scalar());
        this.lifetime = options.lifetime();
        this.multi = options.multi();

        this.renderer = new BoltRenderer();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public void render(@NotNull VertexConsumer pBuffer, @NotNull Camera pRenderInfo, float pPartialTicks) {
        PoseStack pose = new PoseStack();

        Vec3 offset = this.getPos()
            .add(this.direction.scale(this.random.nextFloat() * this.scale));
        // Vec3 offset = this.getPos()
        //         .add(RotationUtil.calculateViewVector((this.random.nextFloat() - 0.5F) * 360.0F, (this.random.nextFloat() - 0.5F) * 360.0F)
        //         .scale(this.random.nextFloat() * this.quadSize));

        double d0 = Mth.lerp(pPartialTicks, this.xo, this.x);
        double d1 = Mth.lerp(pPartialTicks, this.yo, this.y);
        double d2 = Mth.lerp(pPartialTicks, this.zo, this.z);

        Vec3 cam = pRenderInfo.getPosition();

        pose.pushPose();
        pose.translate(d0 - cam.x, d1 - cam.y, d2 - cam.z);

        Vec3 start = new Vec3(this.x, this.y, this.z);
        Vec3 end = new Vec3(offset.x, offset.y, offset.z);
        BoltEffect.BoltRenderInfo info = new BoltEffect.BoltRenderInfo(0.0F, 0.075F, 0.0F, 0.0F,
                new Vector4f(this.color.x, this.color.y, this.color.z, 0.8F), 1.8F);
                BoltEffect.SpawnFunction spawnFunction = null;
                if (this.multi) { 
                    spawnFunction = BoltEffect.SpawnFunction.NO_DELAY;
                }
                else {
                    spawnFunction = BoltEffect.SpawnFunction.CONSECUTIVE;
                }
        BoltEffect bolt = new BoltEffect(info, start, end, (int) (Math.sqrt(start.distanceTo(end) * 100)))
                .size(0.05F)
                .lifespan(1)
                .fade(BoltEffect.FadeFunction.fade(0.5F))
                .spawn(spawnFunction);
                //
                //.fade(BoltEffect.FadeFunction.NONE)
                
                
        this.renderer.update(null, bolt, pPartialTicks);
        pose.translate(-this.x, -this.y, -this.z);
        this.renderer.render(pPartialTicks, pose, Minecraft.getInstance().renderBuffers().bufferSource());
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        pose.popPose();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public record EmittingLightningParticleOptions(Vector3f color,  Vec3 direction, float scalar, int lifetime, boolean multi) implements ParticleOptions {
        public static Deserializer<EmittingLightningParticleOptions> DESERIALIZER = new Deserializer<>() {
            public @NotNull EmittingLightningParticleOptions fromCommand(@NotNull ParticleType<EmittingLightningParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
                Vector3f color = EmittingLightningParticleOptions.readColorVector3f(reader);
                reader.expect(' ');
                Vec3 direction = EmittingLightningParticleOptions.readVec3(reader);
                reader.expect(' ');
                float scalar = reader.readFloat();
                reader.expect(' ');
                int lifetime = reader.readInt();
                reader.expect(' ');
                boolean multi = reader.readBoolean();
                return new EmittingLightningParticleOptions(color, direction, scalar, lifetime, multi);
            }

            public @NotNull EmittingLightningParticleOptions fromNetwork(@NotNull ParticleType<EmittingLightningParticleOptions> type, @NotNull FriendlyByteBuf buf) {
                return new EmittingLightningParticleOptions(EmittingLightningParticleOptions.readColorFromNetwork(buf), readVec3FromNetwork(buf), buf.readFloat(), buf.readInt(), buf.readBoolean());
            }
        };  

        @Override
        public @NotNull ParticleType<?> getType() {
            return JJKParticles.EMITTING_LIGHTNING.get();
        }

        public static Vector3f readColorVector3f(StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float f0 = reader.readFloat();
            reader.expect(' ');
            float f1 = reader.readFloat();
            reader.expect(' ');
            float f2 = reader.readFloat();
            return new Vector3f(f0, f1, f2);
        }

        public static Vec3 readVec3(StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            return new Vec3(x, y, z);
        }

        public static Vec3 readVec3FromNetwork(FriendlyByteBuf buf) {
            return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }


        public static Vector3f readColorFromNetwork(FriendlyByteBuf buf) {
            return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeFloat(this.color.x);
            buf.writeFloat(this.color.y);
            buf.writeFloat(this.color.z);

            buf.writeDouble(this.direction.x);
            buf.writeDouble(this.direction.y);
            buf.writeDouble(this.direction.z);

            buf.writeFloat(this.scalar);
            buf.writeInt(this.lifetime);
            buf.writeBoolean(this.multi);
        }


        @Override
        public @NotNull String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()),
                    this.color.x, this.color.y, this.color.z, this.scalar, this.lifetime);
        }
    }

    public static class Provider implements ParticleProvider<EmittingLightningParticleOptions> {
        public Provider(SpriteSet ignored) {
        }

        @Override
        public EmittingLightningParticle createParticle(@NotNull EmittingLightningParticleOptions options, @NotNull ClientLevel level, double x, double y, double z,
                                                double xSpeed, double ySpeed, double zSpeed) {
            return new EmittingLightningParticle(level, x, y, z, options);
        }
    }
}

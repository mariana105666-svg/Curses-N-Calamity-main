package radon.jujutsu_kaisen.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Locale;

public class FlameParticle extends TextureSheetParticle {
    private static final float[][] HEAT = {
            {1.00F, 0.92F, 0.60F},
            {1.00F, 0.56F, 0.15F},
            {1.00F, 0.28F, 0.05F},
            {1.00F, 0.11F, 0.02F},
            {0.95F, 0.05F, 0.02F}
    };

    private final SpriteSet sprites;

    private final float peak;

    private final boolean smoke;

    private final float phase;

    private final float spin;

    private final float bulk;

    private final float intensity;

    private final float heat;

    private float draw = 1.0F;

    private float lean;

    protected FlameParticle(ClientLevel level, double x, double y, double z,
                            double xSpeed, double ySpeed, double zSpeed,
                            FlameParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z);

        this.sprites = sprites;
        this.smoke = options.smoke();

        this.lifetime = Math.max(2, (int) (options.lifetime() * (0.7F + this.random.nextFloat() * 0.6F)));

        this.bulk = this.random.nextFloat();
        this.peak = options.scalar() * (0.45F + this.bulk * 1.15F);
        this.quadSize = this.peak * 0.35F;

        this.intensity = options.smoke() ? 1.0F
                : Mth.clamp((0.85F - this.bulk * 0.35F) * options.glow(), 0.0F, 1.6F);

        this.heat = (this.random.nextFloat() - 0.5F) * 0.24F + (1.0F - options.glow()) * 0.55F;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.phase = this.random.nextFloat() * Mth.TWO_PI;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.08F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;

        this.hasPhysics = false;

        this.setSpriteFromAge(sprites);
        this.tint(0.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float life = (float) this.age / this.lifetime;

        this.yd += this.smoke ? 0.006D : 0.010D * (1.0F - life);

        double drag = this.smoke ? 0.96D : 0.97D - this.bulk * 0.10D;

        this.xd *= drag;
        this.zd *= drag;
        this.yd *= this.smoke ? 0.97D : 0.95D;

        double curl = 0.0022D * (1.0F - life);
        this.xd += Mth.cos(this.phase + this.age * 0.16F) * curl;
        this.zd += Mth.sin(this.phase + this.age * 0.16F) * curl;

        this.move(this.xd, this.yd, this.zd);

        this.oRoll = this.roll;
        this.roll += this.spin;

        double speed = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);

        this.draw = this.smoke ? 1.0F : (float) Mth.clamp(1.0D + speed * 1.4D, 1.0D, 2.1D);

        float swell = life < 0.18F ? Mth.lerp(life / 0.18F, 0.35F, 1.0F) : 1.0F + (life - 0.18F) * 0.55F;
        this.quadSize = this.peak * swell;

        this.tint(life);
        this.setSpriteFromAge(this.sprites);
    }

    private void tint(float life) {
        if (this.smoke) {
            float grey = 0.06F + life * 0.16F;

            this.rCol = grey;
            this.gCol = grey * 0.97F;
            this.bCol = grey * 0.95F;

            float in = Mth.clamp((life - 0.30F) / 0.25F, 0.0F, 1.0F);
            float out = 1.0F - Mth.clamp((life - 0.65F) / 0.35F, 0.0F, 1.0F);

            this.alpha = in * out * 0.40F;
            return;
        }

        float scaled = Mth.clamp(life + this.heat, 0.0F, 1.0F) * (HEAT.length - 1);
        int step = Math.min((int) scaled, HEAT.length - 2);
        float within = scaled - step;

        this.rCol = Mth.lerp(within, HEAT[step][0], HEAT[step + 1][0]);
        this.gCol = Mth.lerp(within, HEAT[step][1], HEAT[step + 1][1]);
        this.bCol = Mth.lerp(within, HEAT[step][2], HEAT[step + 1][2]);

        float rise = Mth.clamp(life / 0.08F, 0.0F, 1.0F);
        float fall = 1.0F - Mth.clamp((life - 0.35F) / 0.65F, 0.0F, 1.0F);

        this.alpha = rise * fall * fall * this.intensity;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        Vec3 eye = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - eye.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - eye.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - eye.z());

        Quaternionf facing = new Quaternionf(camera.rotation());

        if (this.draw > 1.01F) {
            Vector3f travel = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);
            travel.rotate(new Quaternionf(camera.rotation()).conjugate());

            if (travel.x * travel.x + travel.y * travel.y > 1.0E-6F) {
                this.lean = (float) Math.atan2(travel.y, travel.x) - Mth.HALF_PI;
            }

            facing.rotateZ(this.lean);
        } else {
            facing.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        float size = this.getQuadSize(partialTicks);

        float along = size * this.draw;
        float across = size / Mth.sqrt(this.draw);

        Vector3f[] corners = {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (Vector3f corner : corners) {
            corner.mul(across, along, 1.0F);
            corner.rotate(facing);
            corner.add(x, y, z);
        }

        int light = this.getLightColor(partialTicks);

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        this.corner(buffer, corners[0], u1, v1, light);
        this.corner(buffer, corners[1], u1, v0, light);
        this.corner(buffer, corners[2], u0, v0, light);
        this.corner(buffer, corners[3], u0, v1, light);
    }

    private void corner(VertexConsumer buffer, Vector3f at, float u, float v, int light) {
        buffer.vertex(at.x(), at.y(), at.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return this.smoke ? JJKParticleRenderTypes.SOFT : JJKParticleRenderTypes.GLOW;
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.smoke ? super.getLightColor(partialTick) : 0xF000F0;
    }

    public record FlameParticleOptions(float scalar, boolean smoke, float glow, int lifetime) implements ParticleOptions {
        public static Deserializer<FlameParticleOptions> DESERIALIZER = new Deserializer<>() {
            public @NotNull FlameParticleOptions fromCommand(@NotNull ParticleType<FlameParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float scalar = reader.readFloat();
                reader.expect(' ');
                boolean smoke = reader.readBoolean();
                reader.expect(' ');
                float glow = reader.readFloat();
                reader.expect(' ');
                return new FlameParticleOptions(scalar, smoke, glow, reader.readInt());
            }

            public @NotNull FlameParticleOptions fromNetwork(@NotNull ParticleType<FlameParticleOptions> type, @NotNull FriendlyByteBuf buf) {
                return new FlameParticleOptions(buf.readFloat(), buf.readBoolean(), buf.readFloat(), buf.readInt());
            }
        };

        @Override
        public @NotNull ParticleType<?> getType() {
            return JJKParticles.FLAME.get();
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeFloat(this.scalar);
            buf.writeBoolean(this.smoke);
            buf.writeFloat(this.glow);
            buf.writeInt(this.lifetime);
        }

        @Override
        public @NotNull String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %s %.2f %d",
                    BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.scalar, this.smoke, this.glow, this.lifetime);
        }
    }

    public static class Provider implements ParticleProvider<FlameParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull FlameParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, this.sprites);
        }
    }
}

package radon.jujutsu_kaisen.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
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

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class CursedSpeechParticle extends HugeExplosionParticle {
    protected CursedSpeechParticle(ClientLevel level, double x, double y, double z, CursedSpeechParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, 1.0D, sprites);

        this.lifetime = 16; 
        this.quadSize = options.scalar();

        Vector3f color = options.color();
        this.rCol = color.x;
        this.gCol = color.y;
        this.bCol = color.z;

        this.setSpriteFromAge(sprites);
    }


    public static class Provider implements ParticleProvider<CursedSpeechParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }


    @Override
        public CursedSpeechParticle createParticle(@NotNull CursedSpeechParticleOptions options, @NotNull ClientLevel level,
                                                   double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new CursedSpeechParticle(level, x, y, z, options, this.sprites);
        }
    }


    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return JJKParticleRenderTypes.ADDITIVE;
    }

    @Override
    public int getLightColor(float pPartialTick) {
        float f = ((float) this.age + pPartialTick) / (float) this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15.0F * 16.0F);

        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }

   public record CursedSpeechParticleOptions(Vector3f color, float scalar) implements ParticleOptions {
    public static final Deserializer<CursedSpeechParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public @NotNull CursedSpeechParticleOptions fromCommand(@NotNull ParticleType<CursedSpeechParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            Vector3f color = CursedSpeechParticleOptions.readColorVector3f(reader);
            reader.expect(' ');
            return new CursedSpeechParticleOptions(color, reader.readFloat());
        }

        @Override
        public @NotNull CursedSpeechParticleOptions fromNetwork(@NotNull ParticleType<CursedSpeechParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new CursedSpeechParticleOptions(CursedSpeechParticleOptions.readColorFromNetwork(buf), buf.readFloat());
        }
    };

        @Override
        public @NotNull ParticleType<?> getType() {
            return JJKParticles.CURSED_SPEECH.get();
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

        public static Vector3f readColorFromNetwork(FriendlyByteBuf buf) {
            return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeFloat(this.color.x);
            buf.writeFloat(this.color.y);
            buf.writeFloat(this.color.z);
            buf.writeFloat(this.scalar);
        }

          @Override
        public @NotNull String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()),
                    this.color.x, this.color.y, this.color.z, this.scalar);
        }
    }
}




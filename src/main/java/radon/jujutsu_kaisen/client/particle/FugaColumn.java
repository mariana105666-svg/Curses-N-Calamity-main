package radon.jujutsu_kaisen.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, value = Dist.CLIENT)
public class FugaColumn {
    private static final int DURATION = 55;

    private static final float HEIGHT = 4.0F;
    private static final float WAIST = 0.32F;

    private static final double VIEW = 160.0D;

    private static final float LEAST_FLAME = 1.2F;
    private static final float LEAST_WAIST = 1.2F;

    private static final List<FugaColumn> BURNING = new ArrayList<>();

    private final Vec3 center;
    private final float radius;

    private int age;

    private FugaColumn(Vec3 center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public static void start(Vec3 center, float radius) {
        BURNING.add(new FugaColumn(center, radius));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null || mc.isPaused()) {
            if (mc.level == null) BURNING.clear();
            return;
        }

        Iterator<FugaColumn> iter = BURNING.iterator();

        while (iter.hasNext()) {
            FugaColumn column = iter.next();

            if (column.age++ >= DURATION) {
                iter.remove();
                continue;
            }

            if (mc.player.position().distanceToSqr(column.center) > VIEW * VIEW) continue;

            column.emit(mc.level);
        }
    }

    private void emit(ClientLevel level) {
        RandomSource random = level.random;

        float life = (float) this.age / DURATION;

        float force = this.age < 6 ? 1.0F + (6 - this.age) * 0.5F : Math.max(0.0F, 1.0F - (life - 0.35F) / 0.65F);

        if (force <= 0.0F) return;

        this.shaft(level, random, force);

        if (life < 0.45F) this.bloom(level, random, force);

        if (this.age < 4) this.ground(level, random);
        if (this.age < 3) this.thrown(level, random);

        if (life > 0.3F) this.smoke(level, random, life);
    }

    private void shaft(ClientLevel level, RandomSource random, float force) {
        int count = (int) (Math.max(6.0F, this.radius) * 5.0F * force);

        for (int i = 0; i < count; i++) {
            double up = Math.pow(random.nextDouble(), 0.6D);

            double height = up * this.radius * HEIGHT;
            double narrow = 1.0D - up * 0.3D;

            double theta = random.nextDouble() * Math.PI * 2.0D;

            double out = Math.sqrt(random.nextDouble());
            double r = Math.max(LEAST_WAIST, this.radius * WAIST) * narrow * out;

            double x = Math.cos(theta) * r;
            double z = Math.sin(theta) * r;

            float glow = (float) (0.22D + (1.0D - out) * (1.0D - out) * 1.75D);

            double rise = 0.35D + up * 0.45D + random.nextDouble() * 0.25D;

            level.addParticle(
                    new FlameParticle.FlameParticleOptions(
                            Math.max(LEAST_FLAME, this.radius * 0.24F) * (1.0F - (float) up * 0.25F), false, glow, 32),
                    this.center.x + x, this.center.y + height, this.center.z + z,
                    x * 0.010D, rise, z * 0.010D);
        }
    }

    private void bloom(ClientLevel level, RandomSource random, float force) {
        int count = (int) (Math.max(6.0F, this.radius) * 4.0F * force);

        for (int i = 0; i < count; i++) {
            Vec3 at = inSphere(random, this.radius * 0.75F);

            Vec3 drift = at.normalize().scale(0.05D + random.nextDouble() * 0.07D);

            level.addParticle(new FlameParticle.FlameParticleOptions(Math.max(LEAST_FLAME, this.radius * 0.20F), false, 0.40F, 30),
                    this.center.x + at.x, this.center.y + Math.abs(at.y) * 0.5D, this.center.z + at.z,
                    drift.x, 0.09D, drift.z);
        }
    }

    private void ground(ClientLevel level, RandomSource random) {
        int count = (int) (Math.max(6.0F, this.radius) * 12.0F);

        for (int i = 0; i < count; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0D;
            double out = Math.sqrt(random.nextDouble());

            double distance = this.radius * (0.3D + out * 0.8D);

            double x = Math.cos(theta) * distance;
            double z = Math.sin(theta) * distance;

            double speed = 0.34D * (1.0D - out * 0.5D);

            level.addParticle(new FlameParticle.FlameParticleOptions(Math.max(LEAST_FLAME, this.radius * 0.18F) * (1.0F - (float) out * 0.25F), false, 0.35F, 32),
                    this.center.x + x, this.center.y + random.nextDouble() * this.radius * 0.2D, this.center.z + z,
                    Math.cos(theta) * speed, 0.05D + out * 0.10D, Math.sin(theta) * speed);
        }
    }

    private void thrown(ClientLevel level, RandomSource random) {
        int count = (int) (Math.max(5.0F, this.radius) * 5.0F);

        for (int i = 0; i < count; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0D;
            double phi = Math.acos(1.0D - random.nextDouble() * 1.5D);

            double speed = 0.40D + random.nextDouble() * 0.70D;

            Vec3 at = inSphere(random, this.radius * 0.3F);

            level.addParticle(new FlameParticle.FlameParticleOptions(Math.max(LEAST_FLAME * 0.6F, this.radius * 0.10F), false, 0.55F, 45),
                    this.center.x + at.x, this.center.y + at.y, this.center.z + at.z,
                    Math.sin(phi) * Math.cos(theta) * speed,
                    Math.abs(Math.cos(phi)) * speed * 1.2D,
                    Math.sin(phi) * Math.sin(theta) * speed);
        }
    }

    private void smoke(ClientLevel level, RandomSource random, float life) {
        if (this.age % 2 != 0) return;

        int count = Math.max(1, (int) (this.radius * 0.4F));

        for (int i = 0; i < count; i++) {
            Vec3 at = inSphere(random, this.radius * 0.8F);

            double height = this.radius * HEIGHT * (0.75D + random.nextDouble() * 0.5D);

            level.addParticle(new FlameParticle.FlameParticleOptions(Math.max(2.0F, this.radius * 0.30F), true, 1.0F, 120),
                    this.center.x + at.x, this.center.y + height, this.center.z + at.z,
                    at.x * 0.014D, 0.07D + random.nextDouble() * 0.05D, at.z * 0.014D);
        }
    }

    private static Vec3 inSphere(RandomSource random, float radius) {
        double theta = random.nextDouble() * Math.PI * 2.0D;
        double phi = Math.acos(2.0D * random.nextDouble() - 1.0D);

        double r = radius * Math.pow(random.nextDouble(), 0.7D);

        return new Vec3(r * Math.sin(phi) * Math.cos(theta), r * Math.cos(phi), r * Math.sin(phi) * Math.sin(theta));
    }
}

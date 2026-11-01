package radon.jujutsu_kaisen.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.ExplosionHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.particle.FlameParticle;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.EntityUtil;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.FugaS2CPacket;
import radon.jujutsu_kaisen.util.RotationUtil;

public class FireArrowProjectile extends JujutsuProjectile {
    private static final float DAMAGE = 32.5F;
    private static final float SPEED = 5.0F;
    private static final float EXPLOSIVE_POWER = 3.0F;
    private static final float MAX_EXPLOSION = 15.0F;
    public static final int DELAY = 20;
    public static final int STILL_FRAMES = 2;
    public static final int STARTUP_FRAMES = 4;
    private static final double OFFSET = 2.0D;

    public int animation;

    public FireArrowProjectile(EntityType<? extends Projectile> pType, Level pLevel) {
        super(pType, pLevel);
    }

    public FireArrowProjectile(LivingEntity owner, float power) {
        super(JJKEntities.FIRE_ARROW.get(), owner.level(), owner, power);

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        EntityUtil.offset(this, look, new Vec3(owner.getX(), owner.getEyeY() - (this.getBbHeight() / 2.0F), owner.getZ()).add(look.scale(OFFSET)));
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);

        Entity entity = pResult.getEntity();

        if (!(this.getOwner() instanceof LivingEntity owner)) return;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if ( entity == owner && !cap.hasSelfHit() ) return;

        entity.hurt(JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.FIRE_ARROW.get()), DAMAGE * this.getPower());
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);

        if (this.level().isClientSide) return;

        this.playSound(JJKSounds.FLAME_EXPLOSION.get(), 3.0F, 1.0F);

        Vec3 location = result.getLocation();

        Vec3 center = new Vec3(location.x, location.y + (this.getBbHeight() / 2.0F), location.z);

        this.burst(center);

        if (this.getOwner() instanceof LivingEntity owner) {
            ExplosionHandler.spawn(this.level().dimension(), location, this.getExplosionRadius(), 2 * 20, this.getPower() * 0.45F,
                    owner, JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.FIRE_ARROW.get()), true);
        }
        this.discard();
    }

    private float getFlamePillarRadius() {
        return this.getExplosionRadius() * 0.25F;
    }

    private float getExplosionRadius() {
        return Math.min(MAX_EXPLOSION, EXPLOSIVE_POWER * this.getPower());
    }

    @Override
    public void tick() {

        if (this.getTime() == 0) {
            this.playSound(JJKSounds.FIRE_ARROW.get(), 1.0F, 1.0F);
        }

        super.tick();

        if (this.getTime() >= DELAY) {
            if (this.animation < STILL_FRAMES) {
                this.animation++;
            } else {
                this.animation = 0;
            }
        } else {
            if (this.getTime() > 0 && this.getTime() % (DELAY / STARTUP_FRAMES) == 0) {
                if (this.animation < STARTUP_FRAMES) {
                    this.animation++;
                }
            }
        }

        if (this.getOwner() instanceof LivingEntity owner) {
            if (this.level().isClientSide) {
                if (this.getTime() < DELAY) {
                    this.drawCharge(owner);
                } else {
                    this.drawTail(owner);
                }
            }

            if (this.getTime() < DELAY) {
                if (this.getTime() % 5 == 0) {
                    owner.swing(InteractionHand.MAIN_HAND);
                }
                Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
                EntityUtil.offset(this, look, new Vec3(owner.getX(), owner.getEyeY() - (this.getBbHeight() / 2.0F), owner.getZ()).add(look.scale(OFFSET)));
            } else if (this.getTime() == DELAY) {
                this.setDeltaMovement(RotationUtil.getTargetAdjustedLookAngle(owner).scale(SPEED));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.MASTER, 1.0F, 1.0F);
            }
        }
    }

    private void drawCharge(LivingEntity owner) {
        float progress = Mth.clamp((float) this.getTime() / DELAY, 0.0F, 1.0F);

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        double x = this.getX();
        double y = this.getY() + (this.getBbHeight() / 2.0F);
        double z = this.getZ();

        float drawn = Mth.clamp((progress - 0.55F) / 0.45F, 0.0F, 1.0F);

        double ball = 0.42D - progress * 0.12D;
        double length = drawn * 2.4D;

        if (drawn < 1.0F) {
            int pulled = (int) (20.0F * (1.0F - progress * 0.8F));

            for (int i = 0; i < pulled; i++) {
                double theta = this.random.nextDouble() * Math.PI * 2.0D;
                double phi = Math.acos(2.0D * this.random.nextDouble() - 1.0D);

                double shell = 2.6D - progress * 1.7D + this.random.nextDouble() * 0.7D;

                double ox = Math.sin(phi) * Math.cos(theta);
                double oy = Math.cos(phi);
                double oz = Math.sin(phi) * Math.sin(theta);

                double pull = 0.16D + progress * 0.10D;
                double swirl = 0.07D + progress * 0.06D;

                float heat = 0.24F + this.random.nextFloat() * this.random.nextFloat() * 1.30F;

                this.level().addParticle(new FlameParticle.FlameParticleOptions(0.34F, false, heat, 12),
                        x + ox * shell, y + oy * shell, z + oz * shell,
                        -ox * pull - oz * swirl, -oy * pull, -oz * pull + ox * swirl);
            }
        }

        int body = 6 + (int) (progress * 22.0F);

        for (int i = 0; i < body; i++) {
            double along = (this.random.nextDouble() - 0.28D) * length;

            double taper = length <= 0.0D ? 1.0D
                    : 1.0D - Math.abs(along) / Math.max(0.5D, length) * 0.65D;

            double spread = ball * taper;

            double theta = this.random.nextDouble() * Math.PI * 2.0D;
            double phi = Math.acos(2.0D * this.random.nextDouble() - 1.0D);
            double out = Math.cbrt(this.random.nextDouble());

            double ox = Math.sin(phi) * Math.cos(theta) * out;
            double oy = Math.cos(phi) * out;
            double oz = Math.sin(phi) * Math.sin(theta) * out;

            float glow = (float) (0.24D + (1.0D - out) * (1.0D - out) * 1.55D);

            this.level().addParticle(
                    new FlameParticle.FlameParticleOptions(0.34F + progress * 0.18F, false, glow, 9),
                    x + look.x * along + ox * spread,
                    y + look.y * along + oy * spread,
                    z + look.z * along + oz * spread,
                    look.x * drawn * 0.04D, look.y * drawn * 0.04D, look.z * drawn * 0.04D);
        }

        if (this.random.nextFloat() < progress * 0.8F) {
            double theta = this.random.nextDouble() * Math.PI * 2.0D;
            double speed = 0.12D + this.random.nextDouble() * 0.18D;

            this.level().addParticle(new FlameParticle.FlameParticleOptions(0.13F, false, 0.85F, 16),
                    x, y, z,
                    Math.cos(theta) * speed, this.random.nextDouble() * 0.12D, Math.sin(theta) * speed);
        }
    }

    private void drawTail(LivingEntity owner) {
        Vec3 back = RotationUtil.getTargetAdjustedLookAngle(owner).reverse().scale(0.16D);

        double x = this.getX();
        double y = this.getY() + (this.getBbHeight() / 2.0F);
        double z = this.getZ();

        for (int i = 0; i < 6; i++) {
            double dx = back.x + (this.random.nextDouble() - 0.5D) * 0.22D;
            double dy = back.y + (this.random.nextDouble() - 0.5D) * 0.22D;
            double dz = back.z + (this.random.nextDouble() - 0.5D) * 0.22D;

            double along = this.random.nextDouble();

            float glow = (float) (1.35D - along * 1.05D);

            this.level().addParticle(new FlameParticle.FlameParticleOptions(0.46F + (float) along * 0.16F, false, glow, 18),
                    x - this.getDeltaMovement().x * along,
                    y - this.getDeltaMovement().y * along,
                    z - this.getDeltaMovement().z * along,
                    dx, dy, dz);
        }

        for (int i = 0; i < 2; i++) {
            this.level().addParticle(new FlameParticle.FlameParticleOptions(0.14F, false, 0.85F, 24),
                    x, y, z,
                    back.x * 2.0D + (this.random.nextDouble() - 0.5D) * 0.5D,
                    back.y * 2.0D + (this.random.nextDouble() - 0.5D) * 0.5D,
                    back.z * 2.0D + (this.random.nextDouble() - 0.5D) * 0.5D);
        }
    }

    private void burst(Vec3 center) {
        if (!(this.level() instanceof ServerLevel level)) return;

        FugaS2CPacket packet = new FugaS2CPacket(center, this.getExplosionRadius());

        for (ServerPlayer player : level.players()) {
            PacketHandler.sendToClient(packet, player);
        }
    }

}

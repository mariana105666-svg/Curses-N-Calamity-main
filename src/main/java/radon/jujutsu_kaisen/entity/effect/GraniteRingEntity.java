package radon.jujutsu_kaisen.entity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GraniteRingEntity extends Entity {
    public static final double DIAMETER = 16.0D;
    public static final double RADIUS = DIAMETER / 2.0D;

    private static final int DURATION = 25 * 20;

    private static final double CONTACT = 0.9D;

    public static final double HEIGHT = 4.0D;

    private static final float DAMAGE = 2.0F;

    private static final double INNER_KNOCKBACK = 0.25D;
    private static final double OUTER_KNOCKBACK = 0.5D;

    private static final double CLEARANCE = 0.7D;

    private static final int DAMAGE_INTERVAL = 10;

    private static final int SEGMENTS = 64;

    private static final double OVERLAP = 1.05D;

    private static final double RIDE_HEIGHT = 1.0D;

    private static final int PIECE_LIFETIME = DURATION;

    @Nullable
    private UUID ownerUUID;

    private final Set<UUID> caught = new HashSet<>();

    public GraniteRingEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);

        this.noCulling = true;
    }

    public GraniteRingEntity(LivingEntity owner) {
        this(JJKEntities.GRANITE_RING.get(), owner.level());

        this.ownerUUID = owner.getUUID();

        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.ownerUUID == null || !(this.level() instanceof net.minecraft.server.level.ServerLevel level)) return null;

        return level.getEntity(this.ownerUUID) instanceof LivingEntity owner ? owner : null;
    }

    public static boolean isCaged(LivingEntity entity) {
        for (GraniteRingEntity ring : entity.level().getEntitiesOfClass(GraniteRingEntity.class,
                entity.getBoundingBox().inflate(DIAMETER))) {
            if (ring.isRemoved()) continue;
            if (ring.contains(entity)) return true;
        }
        return false;
    }

    public static boolean suppresses(LivingEntity owner, Ability ability) {
        if (ability == JJKAbilities.PUNCH.get() || ability.isMelee()) return false;
        if (!ability.isTechnique() && !ability.isDomain()) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap != null && (cap.hasToggled(ability) || cap.isChanneling(ability))) return false;

        return isCaged(owner);
    }

    public boolean contains(Entity entity) {
        double vertical = entity.getY() - this.getY();

        if (vertical < -1.0D || vertical > HEIGHT) return false;

        return this.horizontalDistance(entity) < RADIUS;
    }

    private double horizontalDistance(Entity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();

        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.tickCount > DURATION || this.getOwner() == null) {
            this.discard();
            return;
        }

        if (this.tickCount == 1) {
            this.draw();

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(this.getX() - RADIUS, this.getY() - 1.0D, this.getZ() - RADIUS,
                            this.getX() + RADIUS, this.getY() + HEIGHT, this.getZ() + RADIUS))) {
                if (this.contains(entity)) this.caught.add(entity.getUUID());
            }
        }

        AABB bounds = new AABB(this.getX() - RADIUS - 2.0D, this.getY() - 1.0D, this.getZ() - RADIUS - 2.0D,
                this.getX() + RADIUS + 2.0D, this.getY() + HEIGHT, this.getZ() + RADIUS + 2.0D);

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, bounds)) {
            double distance = this.horizontalDistance(entity);

            if (Math.abs(distance - RADIUS) > CONTACT) continue;

            double vertical = entity.getY() - this.getY();

            if (vertical < -1.0D || vertical > HEIGHT) continue;

            boolean inside = this.caught.contains(entity.getUUID());

            Vec3 outward = distance < 1.0E-4D
                    ? new Vec3(1.0D, 0.0D, 0.0D)
                    : new Vec3(entity.getX() - this.getX(), 0.0D, entity.getZ() - this.getZ()).normalize();

            Vec3 through = inside ? outward : outward.scale(-1.0D);

            Vec3 motion = entity.getDeltaMovement();

            double radial = motion.x * through.x + motion.z * through.z;

            double strength = inside ? INNER_KNOCKBACK : OUTER_KNOCKBACK;

            if (radial > 0.0D) {
                motion = motion.subtract(through.scale(radial + strength));

                entity.setDeltaMovement(motion.x, motion.y, motion.z);
                entity.hurtMarked = true;
            }

            if (inside ? distance <= RADIUS : distance >= RADIUS) continue;

            double limit = inside ? RADIUS - CLEARANCE : RADIUS + CLEARANCE;

            entity.teleportTo(this.getX() + outward.x * limit, entity.getY(),
                    this.getZ() + outward.z * limit);

            if (!inside || this.tickCount % DAMAGE_INTERVAL != 0) continue;

            LivingEntity owner = this.getOwner();

            if (entity == owner) continue;

            entity.hurt(JJKDamageSources.indirectJujutsuAttack(this, owner, JJKAbilities.GRANITE_RING.get()), DAMAGE);
        }
    }

    private void draw() {
        LivingEntity owner = this.getOwner();

        if (owner == null) return;

        Vec3 head = new Vec3(owner.getX(), owner.getY() + owner.getBbHeight(), owner.getZ())
                .add(RotationUtil.getTargetAdjustedLookAngle(owner));

        float baseYaw = (float) ((RotationUtil.getTargetAdjustedYRot(owner) + 90.0F) * Math.PI / 180.0D);
        float basePitch = (float) (-RotationUtil.getTargetAdjustedXRot(owner) * Math.PI / 180.0D);

        double step = 2.0D * Math.PI / SEGMENTS;

        double chord = 2.0D * RADIUS * Math.sin(step / 2.0D) * OVERLAP;

        for (int i = 0; i < SEGMENTS; i++) {
            double angle = i * step;

            Vec3 point = this.position().add(Math.cos(angle) * RADIUS, RIDE_HEIGHT, Math.sin(angle) * RADIUS);

            double ahead = angle + step;
            Vec3 next = this.position().add(Math.cos(ahead) * RADIUS, RIDE_HEIGHT, Math.sin(ahead) * RADIUS);

            Vec3 direction = next.subtract(point).normalize();

            float yaw = (float) (Math.atan2(direction.z, direction.x) - baseYaw);
            float pitch = -basePitch;

            GraniteBlastVolleyEntity piece = new GraniteBlastVolleyEntity(owner, 1.0F, yaw, pitch,
                    0.0F, 0.0F, point.subtract(head), chord, true, true);

            piece.setLifetime(PIECE_LIFETIME);

            this.level().addFreshEntity(piece);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (pCompound.hasUUID("owner")) {
            this.ownerUUID = pCompound.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("owner", this.ownerUUID);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

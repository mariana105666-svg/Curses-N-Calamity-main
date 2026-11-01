package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.ai.attributes.Attributes;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.block.entity.VeilRodBlockEntity;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.VeilEntity;
import radon.jujutsu_kaisen.item.veil.modifier.AdminModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.TransparentModifier;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.OpenVeilConfigurationS2CPacket;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Veil extends Ability implements Ability.IToggled {
    public static final double RANGE = 16.0D;
    private static final int RADIUS = 64;

    private static final int ADMIN_RADIUS_MULTIPLIER = 2;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }
   
    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private record Placement(Vec3 center, int radius, List<Modifier> modifiers, BlockPos rod, boolean anchored) {}

    @Nullable
    private static Placement resolve(LivingEntity owner) {
        if (!(RotationUtil.getLookAtHit(owner, RANGE) instanceof BlockHitResult blockHit)) return null;

        BlockPos pos = blockHit.getBlockPos();

        if (owner.level().getBlockEntity(pos) instanceof VeilRodBlockEntity be
                && be.getOwnerUUID() != null && be.getOwnerUUID().equals(owner.getUUID())) {
            return new Placement(pos.getCenter(), be.getSize(), be.getModifiers(), pos, true);
        }

        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        Vec3 center = result.getType() == HitResult.Type.MISS ? end : result.getLocation();

        return new Placement(center, RADIUS, List.of(), pos, false);
    }

    private static boolean canPlace(ServerLevel level, LivingEntity owner, Vec3 center, int radius) {
        if (!VeilHandler.getVeils(level, owner.blockPosition()).isEmpty()) return false;

        return !VeilHandler.wouldOverlapVeil(level, center, radius);
    }

    private static int radiusFor(Placement placement, List<Modifier> modifiers, int size) {
        // A rod dictates its own size, so it is left alone
        if (placement.anchored()) return placement.radius();

        int radius = Mth.clamp(size,
                ConfigHolder.SERVER.minimumVeilSize.get(),
                ConfigHolder.SERVER.maximumVeilSize.get());

        for (Modifier modifier : modifiers) {
            if (modifier.getType() == Modifier.Type.ADMIN) return radius * ADMIN_RADIUS_MULTIPLIER;
        }
        return radius;
    }

    public static void place(LivingEntity owner, List<Modifier> modifiers, int size) {
        if (!(owner.level() instanceof ServerLevel level)) return;

        if (!JJKAbilities.VEIL.get().isValid(owner)) return;

        Placement placement = resolve(owner);

        if (placement == null) return;

        List<Modifier> effective = vet(owner, modifiers);

        int radius = radiusFor(placement, effective, size);

        if (!canPlace(level, owner, placement.center(), radius)) return;

        level.addFreshEntity(new VeilEntity(owner, placement.center(), radius,
                effective, placement.rod()));

        announce(owner, level);
    }

    private static List<Modifier> vet(LivingEntity owner, List<Modifier> modifiers) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        boolean permitted = cap != null && cap.hasAdminVeil();
        boolean admin = false;

        Modifier transparent = null;

        List<Modifier> result = new ArrayList<>();

        for (Modifier modifier : modifiers) {
            if (modifier.getType() == Modifier.Type.ADMIN) {
                admin |= permitted;
                continue;
            }
            if (modifier.getType() == Modifier.Type.TRANSPARENT) transparent = modifier;

            result.add(modifier);
        }

        if (admin) {
            List<Modifier> sealed = new ArrayList<>();
            sealed.add(new AdminModifier(Modifier.Action.ALLOW));

            if (transparent != null) sealed.add(transparent);

            for (Modifier modifier : modifiers) {
                switch (modifier.getType()) {
                    case CURSE, SORCERER, DOMAIN, ABILITY -> sealed.add(modifier);
                    default -> { }
                }
            }
            return sealed;
        }
        return result;
    }

    private static void announce(LivingEntity owner, ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (owner instanceof Mob && player.distanceTo(owner) > owner.getAttributeValue(Attributes.FOLLOW_RANGE))
                continue;
            player.sendSystemMessage(Component.translatable(String.format("chat.%s.veil", JujutsuKaisen.MOD_ID), owner.getName().getString()));
        }
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (owner.level() instanceof ServerLevel level) {
            Placement placement = resolve(owner);

            if (placement == null || !canPlace(level, owner, placement.center(), placement.radius())) return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;

        Placement placement = resolve(owner);

        if (placement == null || !canPlace(level, owner, placement.center(), placement.radius())) return;

        if (placement.anchored()) {
            level.addFreshEntity(new VeilEntity(owner, placement.center(), placement.radius(),
                    placement.modifiers(), placement.rod()));

            announce(owner, level);
            return;
        }

        // The veil is raised by the reply to this, once the caster has set its restrictions
        if (owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new OpenVeilConfigurationS2CPacket(), player);
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.6F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.INCOMPLETE_DOMAIN.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 0.0F);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public int getPointsCost() {
        return 60;
    }
}

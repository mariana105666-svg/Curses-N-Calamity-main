package radon.jujutsu_kaisen.limb;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ReverseHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.util.CuriosUtil;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.SorcererUtil;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.ten_shadows.MahoragaEntity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LimbHandler {
    public static final float THRESHOLD = 0.25F;

    private static final float MIN_LOSS = 0.25F;

    private static final float WHOLE_ARM = 0.15F;

    private static final float STUB = 0.2F;

    private static final float REGROW_PER_TICK = 1.0F / 300.0F;
    private static final int REGROW_STEP = 4;

    private static final UUID SEVERED = UUID.fromString("7f3c9d12-5b84-4a61-9e27-0c6d8af31b45");

    private static final int HOBBLE_DURATION = 60;
    private static final int HOBBLE_REFRESH = 40;

    private static final float BLADE_DAMAGE = 10.0F;
    private static final float BLADE_CHANCE = 0.1F;
    private static final float BLADE_RESTRICTION_CHANCE = 0.2F;
    private static final float FULL_CHARGE = 0.9F;

    private static final String SEVER_TAG = "jujutsu_kaisen:last_sever";
    private static final int SEVER_COOLDOWN = 60;

    private LimbHandler() {
    }

    private static Set<Ability> blades() {
        return Set.of(
                JJKAbilities.CLEAVE.get(),
                JJKAbilities.DISMANTLE.get(),
                JJKAbilities.DISMANTLE_NET.get(),
                JJKAbilities.DISMANTLE_SKATING.get(),
                JJKAbilities.WORLD_SLASH.get(),
                JJKAbilities.SWORD_ARMS.get());
    }

    private static Set<Ability> blasts() {
        return Set.of(
                JJKAbilities.MAXIMUM_METEOR.get(),
                JJKAbilities.VOLCANO.get(),
                JJKAbilities.COFFIN_OF_THE_IRON_MOUNTAIN.get(),
                JJKAbilities.FIRE_ARROW.get(),
                JJKAbilities.CURSED_ENERGY_BOMB.get(),
                JJKAbilities.SWEET_BURNING_MATSURI.get());
    }

    @Nullable
    private static Wound kind(Ability ability) {
        if (blades().contains(ability)) return Wound.CLEAN;
        if (blasts().contains(ability)) return Wound.RAGGED;

        return null;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;

        ISorcererData cap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (recent(victim)) return;

        DamageSource source = event.getSource();

        Wound kind;
        float loss;

        if (source instanceof JJKDamageSources.JujutsuDamageSource jujutsu) {
            Ability ability = jujutsu.getAbility();

            if (ability == null) return;

            kind = kind(ability);

            if (kind == null) return;

            float share = event.getAmount() / victim.getMaxHealth();

            if (share < THRESHOLD) return;

            loss = victim.getRandom().nextFloat() < WHOLE_ARM ? 1.0F : Mth.clamp(share, MIN_LOSS, 1.0F);
        } else if (blade(victim, source, event.getAmount())) {
            kind = Wound.CLEAN;
            loss = 1.0F;
        } else {
            return;
        }

        Limb limb = target(victim, cap, source.getEntity());

        if (limb == null) return;

        float left = cap.getLimb(limb) - loss;

        if (left < STUB) left = 0.0F;

        cap.wound(limb, left, kind);

        victim.getPersistentData().putLong(SEVER_TAG, victim.level().getGameTime());

        SorcererUtil.tryUnlockReverse(victim, SorcererUtil.LIMB_REVERSE_CHANCE, SorcererGrade.GRADE_2);

        release(victim, cap);

        victim.sendSystemMessage(Component.translatable(
                String.format("chat.%s.limb_lost.%s", JujutsuKaisen.MOD_ID, limb.name().toLowerCase()))
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static boolean recent(ServerPlayer victim) {
        CompoundTag data = victim.getPersistentData();

        if (!data.contains(SEVER_TAG)) return false;

        long now = victim.level().getGameTime();
        long last = data.getLong(SEVER_TAG);

        return now >= last && now - last < SEVER_COOLDOWN;
    }

    private static boolean blade(ServerPlayer victim, DamageSource source, float amount) {
        if (amount < BLADE_DAMAGE) return false;
        if (source instanceof JJKDamageSources.JujutsuDamageSource) return false;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return false;
        if (!HelperMethods.isMelee(source)) return false;
        if (attacker instanceof Player player && player.getAttackStrengthScale(0.5F) <= FULL_CHARGE) return false;
        if (!sword(attacker)) return false;

        float chance = attacker instanceof MahoragaEntity || JJKAbilities.hasTrait(attacker, Trait.HEAVENLY_RESTRICTION)
                ? BLADE_RESTRICTION_CHANCE : BLADE_CHANCE;

        return victim.getRandom().nextFloat() < chance;
    }

    private static boolean sword(LivingEntity attacker) {
        if (attacker instanceof MahoragaEntity) return true;
        if (JJKAbilities.hasToggled(attacker, JJKAbilities.SWORD_ARMS.get())) return true;
        if (attacker.getMainHandItem().getItem() instanceof SwordItem) return true;

        String identifier = attacker.getMainArm() == HumanoidArm.RIGHT ? "right_hand" : "left_hand";

        for (ItemStack stack : CuriosUtil.findSlots(attacker, identifier)) {
            if (stack.getItem() instanceof SwordItem) return true;
        }

        return false;
    }

    @Nullable
    private static Limb target(ServerPlayer victim, ISorcererData cap, @Nullable Entity attacker) {
        HumanoidArm struck = side(victim, attacker);

        if (JJKAbilities.hasTrait(victim, Trait.PERFECT_BODY)) {
            Limb extra = Limb.extraForArm(struck);

            if (cap.getLimb(extra) > 0.0F) return extra;

            Limb opposite = Limb.extraForArm(struck.getOpposite());

            if (cap.getLimb(opposite) > 0.0F) return opposite;
        }

        Limb limb = victim.getRandom().nextBoolean() ? Limb.forArm(struck) : Limb.forLeg(struck);

        return cap.getLimb(limb) > 0.0F ? limb : null;
    }

    private static HumanoidArm side(ServerPlayer victim, @Nullable Entity attacker) {
        if (attacker == null) {
            return victim.getRandom().nextBoolean() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        }

        Vec3 look = victim.getLookAngle();
        Vec3 to = attacker.position().subtract(victim.position());

        Vec3 right = new Vec3(-look.z, 0.0D, look.x);

        return to.dot(right) >= 0.0D ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        release(player, cap);
        hobble(player, cap);
        regrow(player, cap);
    }

    private static void release(ServerPlayer player, ISorcererData cap) {
        HumanoidArm main = player.getMainArm();

        Limb mainLimb = Limb.forArm(main);
        Limb offLimb = Limb.forArm(main.getOpposite());

        if (cap.getLimb(mainLimb) < 1.0F) empty(player, EquipmentSlot.MAINHAND);
        if (cap.getLimb(offLimb) < 1.0F) empty(player, EquipmentSlot.OFFHAND);

        boolean perfect = JJKAbilities.hasTrait(player, Trait.PERFECT_BODY);

        for (Limb limb : Limb.values()) {
            if (!limb.isExtra()) continue;

            slot(player, limb, perfect && cap.getLimb(limb) < 1.0F);
        }
    }

    private static void hobble(ServerPlayer player, ISorcererData cap) {
        if (cap.getLimb(Limb.RIGHT_LEG) >= 1.0F && cap.getLimb(Limb.LEFT_LEG) >= 1.0F) return;

        MobEffectInstance existing = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

        if (existing != null && existing.getAmplifier() >= 1 && existing.getDuration() > HOBBLE_REFRESH) return;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, HOBBLE_DURATION, 1, false, false, true));
    }

    private static void slot(ServerPlayer player, Limb limb, boolean severed) {
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);

        if (!optional.isPresent()) return;

        ICuriosItemHandler inventory = optional.resolve().orElseThrow();

        ICurioStacksHandler handler = inventory.getStacksHandler(limb.curio()).orElse(null);

        if (handler == null) return;

        if (severed == handler.getModifiers().containsKey(SEVERED)) return;

        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(limb.curio(), new AttributeModifier(SEVERED, "jujutsu_kaisen:severed_arm", -1.0D,
                AttributeModifier.Operation.ADDITION));

        if (!severed) {
            inventory.removeSlotModifiers(modifiers);
            return;
        }

        Optional<SlotResult> equipped = inventory.findCurio(limb.curio(), 0);

        if (equipped.isPresent()) {
            ItemStack held = equipped.get().stack().copy();

            inventory.setEquippedCurio(limb.curio(), 0, ItemStack.EMPTY);

            stow(player, held);
        }

        inventory.addTransientSlotModifiers(modifiers);
    }

    private static void empty(ServerPlayer player, EquipmentSlot slot) {
        ItemStack held = player.getItemBySlot(slot);

        if (held.isEmpty()) return;

        player.setItemSlot(slot, ItemStack.EMPTY);

        stow(player, held);
    }

    private static void stow(ServerPlayer player, ItemStack held) {
        if (held.isEmpty()) return;

        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.items.size(); i++) {
            if (i == inventory.selected || !inventory.items.get(i).isEmpty()) continue;

            inventory.items.set(i, held);
            return;
        }

        player.drop(held, false);
    }

    private static void regrow(ServerPlayer player, ISorcererData cap) {
        if (player.tickCount % REGROW_STEP != 0) return;

        Ability channeled = cap.getChanneled();

        if (channeled == null || !channeled.isReverseCursedTechnique()) return;

        for (Limb limb : Limb.values()) {
            float remaining = cap.getLimb(limb);

            if (remaining >= 1.0F) continue;

            cap.setLimb(limb, remaining + REGROW_PER_TICK * REGROW_STEP);

            if (cap.getLimb(limb) >= 1.0F) cap.addReverseEfficiency(ReverseHandler.PER_LIMB);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        for (Limb limb : Limb.values()) cap.setLimb(limb, 1.0F);
    }
}

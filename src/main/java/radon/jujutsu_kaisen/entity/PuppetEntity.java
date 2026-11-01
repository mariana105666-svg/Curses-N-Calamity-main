package radon.jujutsu_kaisen.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import radon.jujutsu_kaisen.entity.ai.goal.PuppetAttackGoal;
import radon.jujutsu_kaisen.entity.ai.goal.PuppetFollowGoal;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.puppet.PuppetRecord;

import java.util.List;
import radon.jujutsu_kaisen.puppet.PuppetUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PuppetEntity extends TamableAnimal {
    public static final double HEALTH = 70.0D;
    public static final double ATTACK = 9.0D;
    public static final float REPAIR = 6.0F;
    public static final double ARMOR = 10.0D;
    public static final double TOUGHNESS = 6.0D;

    private boolean working;

    private net.minecraft.core.BlockPos breaking;
    private float breakProgress;

    public PuppetEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);

        this.setPersistenceRequired();
    }

    public boolean hasSwordArms() {
        return this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)
                .is(radon.jujutsu_kaisen.item.JJKItems.SWORD_ARMS.get());
    }

    public void setSwordArms(boolean enabled) {
        if (this.hasSwordArms() == enabled) return;

        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, enabled
                ? new ItemStack(radon.jujutsu_kaisen.item.JJKItems.SWORD_ARMS.get())
                : ItemStack.EMPTY);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.0F);
    }

    private static final EntityDataAccessor<Integer> DATA_INFUSION =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> DATA_INFUSER =
            SynchedEntityData.defineId(PuppetEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_INFUSION, 0);
        this.entityData.define(DATA_INFUSER, Optional.empty());
    }

    public int getInfusion() {
        return this.entityData.get(DATA_INFUSION);
    }

    public void setInfusion(int ticks) {
        this.entityData.set(DATA_INFUSION, ticks);
    }

    public @Nullable UUID getInfuser() {
        return this.entityData.get(DATA_INFUSER).orElse(null);
    }

    public void setInfuser(@Nullable UUID infuser) {
        this.entityData.set(DATA_INFUSER, Optional.ofNullable(infuser));
    }

    public void clearInfusion() {
        this.setInfusion(0);
        this.setInfuser(null);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, HEALTH)
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, ATTACK)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, TOUGHNESS);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PuppetAttackGoal(this, 1.2D));
        this.goalSelector.addGoal(2, new PuppetFollowGoal(this, 1.1D, 5.0F, 2.5F));
    }

    public boolean isWorking() {
        return this.working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    private void strike(ServerLevel level, Entity target) {
        org.joml.Vector3f color = this.getOwner() instanceof net.minecraft.world.entity.player.Player owner
                ? radon.jujutsu_kaisen.client.particle.ParticleColors.getCursedEnergyColorBright(owner)
                : radon.jujutsu_kaisen.client.particle.ParticleColors.CURSED_ENERGY_SORCERER;

        double y = target.getY() + target.getBbHeight() * 0.6D;

        for (int i = 0; i < 24; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * target.getBbWidth() * 2.0D;
            double oy = (this.random.nextDouble() - 0.5D) * target.getBbHeight();
            double oz = (this.random.nextDouble() - 0.5D) * target.getBbWidth() * 2.0D;

            level.sendParticles(new radon.jujutsu_kaisen.client.particle.CursedEnergyParticle.CursedEnergyParticleOptions(
                            color, 0.5F, 1.0F, 6),
                    target.getX() + ox, y + oy, target.getZ() + oz, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        level.sendParticles(ParticleTypes.CRIT, target.getX(), y, target.getZ(), 16, 0.3D, 0.3D, 0.3D, 0.2D);

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 1.0F, 0.8F);

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                radon.jujutsu_kaisen.sound.JJKSounds.SPARK.get(), SoundSource.HOSTILE, 0.6F, 1.4F);
    }

    @Override
    public void die(@NotNull net.minecraft.world.damagesource.DamageSource pDamageSource) {
        super.die(pDamageSource);

        if (this.level().isClientSide) return;

        UUID owner = this.getOwnerUUID();

        if (owner == null || this.getServer() == null) return;

        ServerPlayer player = this.getServer().getPlayerList().getPlayer(owner);

        if (player == null) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        PuppetRecord record = cap.getPuppetByEntity(this.getUUID());

        if (record == null) return;

        cap.removePuppet(record);
        cap.syncFull();
    }

    public float addBreakProgress(net.minecraft.core.BlockPos pos, float amount) {
        if (!pos.equals(this.breaking)) {
            this.breaking = pos.immutable();
            this.breakProgress = 0.0F;
        }

        this.breakProgress += amount;

        return this.breakProgress;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        boolean swords = this.hasSwordArms();

        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)
                + (swords ? (float) radon.jujutsu_kaisen.ability.puppet_manipulation.SwordArms.BONUS : 0.0F);

        boolean hurt = pEntity.hurt(JJKDamageSources.jujutsuAttack(this,
                swords ? radon.jujutsu_kaisen.ability.JJKAbilities.SWORD_ARMS.get() : null), damage);

        if (!hurt) return false;

        this.setLastHurtMob(pEntity);

        if (pEntity instanceof LivingEntity living) {
            living.knockback(0.4F, this.getX() - pEntity.getX(), this.getZ() - pEntity.getZ());
        }

        if (this.level() instanceof ServerLevel level) {
            this.strike(level, pEntity);
        }
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOther) {
        return null;
    }

    @Override
    public boolean isFood(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    public void link(Player owner, String name) {
        this.setTame(true);
        this.setOwnerUUID(owner.getUUID());
        this.setCustomName(Component.literal(name));
        this.setCustomNameVisible(true);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack held = pPlayer.getItemInHand(pHand);

        if (!pPlayer.isShiftKeyDown() && held.is(Items.IRON_INGOT) && this.isLinked()
                && pPlayer.getUUID().equals(this.getOwnerUUID())) {
            if (this.getHealth() >= this.getMaxHealth()) return InteractionResult.PASS;

            if (this.level() instanceof ServerLevel level) {
                this.heal(REPAIR);

                if (!pPlayer.getAbilities().instabuild) held.shrink(1);

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(0.6D), this.getZ(),
                        12, this.getBbWidth() * 0.5D, this.getBbHeight() * 0.4D, this.getBbWidth() * 0.5D, 0.0D);

                level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!pPlayer.isShiftKeyDown() || !this.isLinked()) return InteractionResult.PASS;
        if (!pPlayer.getUUID().equals(this.getOwnerUUID())) return InteractionResult.PASS;

        if (this.level().isClientSide) return InteractionResult.SUCCESS;

        if (!(pPlayer instanceof ServerPlayer player)) return InteractionResult.FAIL;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null || cap.isPossessing()) return InteractionResult.FAIL;

        PuppetRecord record = cap.getPuppetByEntity(this.getUUID());

        if (record == null) return InteractionResult.FAIL;

        int slot = cap.getPuppets().indexOf(record);

        List<Ability> abilities = JJKAbilities.getPuppetAbilities();

        if (slot < 0 || slot >= abilities.size()) return InteractionResult.FAIL;

        cap.setInside(true);

        AbilityHandler.trigger(player, abilities.get(slot));

        if (!cap.isPossessing()) {
            cap.setInside(false);
            return InteractionResult.FAIL;
        }

        return InteractionResult.CONSUME;
    }

    public boolean isLinked() {
        return this.getOwnerUUID() != null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || this.tickCount < 40 || this.tickCount % 40 != 0) return;

        UUID owner = this.getOwnerUUID();

        if (owner == null) return;

        Player player = this.level().getPlayerByUUID(owner);

        if (player == null) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        if (!cap.isPossessing()) {
            this.setSwordArms(cap.hasToggled(radon.jujutsu_kaisen.ability.JJKAbilities.SWORD_ARMS.get()));
        }

        if (cap.getPuppetByEntity(this.getUUID()) != null) return;

        this.setTame(false);
        this.setOwnerUUID(null);
        this.setCustomName(null);
        this.setCustomNameVisible(false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putFloat("puppet_health", this.getHealth());
        pCompound.putInt("infusion", this.getInfusion());

        UUID infuser = this.getInfuser();

        if (infuser != null) pCompound.putUUID("infuser", infuser);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("puppet_health")) {
            this.setHealth(pCompound.getFloat("puppet_health"));
        }

        this.setInfusion(pCompound.getInt("infusion"));
        this.setInfuser(pCompound.hasUUID("infuser") ? pCompound.getUUID("infuser") : null);
    }
}

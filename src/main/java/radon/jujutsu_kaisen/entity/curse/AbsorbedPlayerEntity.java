package radon.jujutsu_kaisen.entity.curse;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.entity.JJKEntityDataSerializers;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;

import java.util.Optional;

public class AbsorbedPlayerEntity extends CursedSpirit {
    private static final EntityDataAccessor<Optional<CompoundTag>> DATA_PLAYER = SynchedEntityData.defineId(AbsorbedPlayerEntity.class, JJKEntityDataSerializers.OPTIONAL_COMPOUND_TAG.get());

    public AbsorbedPlayerEntity(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    public @NotNull Component getName() {
        GameProfile profile = this.getPlayer();

        if (profile == null || profile.getName() == null) return super.getName();

        return Component.literal(profile.getName());
    }

    public void setPlayer(GameProfile profile) {
        this.entityData.set(DATA_PLAYER, Optional.of(NbtUtils.writeGameProfile(new CompoundTag(), profile)));
    }

    public @Nullable GameProfile getPlayer() {
        CompoundTag a = this.entityData.get(DATA_PLAYER).orElse(null);
        if (a == null) {
            return null;
        }
        else {
            return NbtUtils.readGameProfile(a);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_PLAYER, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        this.entityData.get(DATA_PLAYER).ifPresent(player -> pCompound.put("player", player));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("player")) {
            this.entityData.set(DATA_PLAYER, Optional.of(pCompound.getCompound("player")));
        }
    }

    @Override
    public void init(ISorcererData data) {

    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public boolean hasMeleeAttack() {
        return true;
    }

    @Override
    public boolean hasArms() {
        return true;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public boolean canChant() {
        return true;
    }

    @Override
    public float getExperience() {
        return 0;
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return null;
    }
}

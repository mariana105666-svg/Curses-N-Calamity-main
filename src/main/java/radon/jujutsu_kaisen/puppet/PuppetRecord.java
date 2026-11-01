package radon.jujutsu_kaisen.puppet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PuppetRecord {
    private String name;

    private @Nullable UUID entity;

    private float health;
    private float maxHealth;

    private double x;
    private double y;
    private double z;

    private float distance;

    private float storedEnergy;

    private String dimension = "";

    public PuppetRecord(String name, @Nullable UUID entity, float health, float maxHealth, Vec3 pos) {
        this.name = name;
        this.entity = entity;
        this.health = health;
        this.maxHealth = maxHealth;

        this.setPos(pos);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable UUID getEntity() {
        return this.entity;
    }

    public void setEntity(@Nullable UUID entity) {
        this.entity = entity;
    }

    public boolean isWorn() {
        return this.entity == null;
    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }

    public void setPos(Vec3 pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getDimension() {
        return this.dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public float getStoredEnergy() {
        return this.storedEnergy;
    }

    public void setStoredEnergy(float storedEnergy) {
        this.storedEnergy = storedEnergy;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putString("name", this.name);

        if (this.entity != null) nbt.putUUID("entity", this.entity);

        nbt.putFloat("health", this.health);
        nbt.putFloat("max_health", this.maxHealth);
        nbt.putDouble("x", this.x);
        nbt.putDouble("y", this.y);
        nbt.putDouble("z", this.z);
        nbt.putFloat("distance", this.distance);
        nbt.putFloat("stored_energy", this.storedEnergy);
        nbt.putString("dimension", this.dimension);

        return nbt;
    }

    public static PuppetRecord deserializeNBT(CompoundTag nbt) {
        PuppetRecord record = new PuppetRecord(
                nbt.getString("name"),
                nbt.hasUUID("entity") ? nbt.getUUID("entity") : null,
                nbt.getFloat("health"),
                nbt.getFloat("max_health"),
                new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z")));

        record.distance = nbt.getFloat("distance");
        record.storedEnergy = nbt.getFloat("stored_energy");
        record.dimension = nbt.getString("dimension");

        return record;
    }
}

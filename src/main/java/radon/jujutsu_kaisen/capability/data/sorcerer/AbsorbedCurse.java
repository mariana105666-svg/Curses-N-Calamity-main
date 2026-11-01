package radon.jujutsu_kaisen.capability.data.sorcerer;


import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class AbsorbedCurse {
    private final Component name;
    private final EntityType<?> type;
    private final CompoundTag data;
    private final UUID uuid;

    @Nullable
    private GameProfile profile;

    public AbsorbedCurse(Component name, EntityType<?> type, CompoundTag data) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.uuid = UUID.randomUUID();
    }

    public AbsorbedCurse(Component name, EntityType<?> type, CompoundTag data, @Nullable GameProfile profile) {
        this(name, type, data);

        this.profile = profile;
    }

    private AbsorbedCurse(CompoundTag nbt, EntityType<?> type) {
        this.uuid = nbt.contains("UUID") ? nbt.getUUID("UUID") : UUID.randomUUID();
        this.type = type;

        Component name = Component.Serializer.fromJson(nbt.getString("name"));

        this.name = name == null ? type.getDescription() : name;
        this.data = nbt.getCompound("data");

        if (nbt.contains("profile")) {
            this.profile = NbtUtils.readGameProfile(nbt.getCompound("profile"));
        }
    }

    @Nullable
    public static AbsorbedCurse read(CompoundTag nbt) {
        EntityType<?> type = EntityType.byString(nbt.getString("type")).orElse(null);

        if (type == null) return null;

        return new AbsorbedCurse(nbt, type);
    }


    public UUID getUUID() {
        return uuid;
    }
    public Component getName() {
        return this.name;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    public CompoundTag getData() {
        return this.data;
    }

    @Nullable
    public GameProfile getProfile() {
        return this.profile;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("UUID", this.uuid);
        nbt.putString("name", Component.Serializer.toJson(this.name));
        nbt.putString("type", EntityType.getKey(this.type).toString());
        nbt.put("data", this.data);
        if (this.profile != null) {
            nbt.put("profile", NbtUtils.writeGameProfile(new CompoundTag(), this.profile));
        }
        return nbt;
    }
}

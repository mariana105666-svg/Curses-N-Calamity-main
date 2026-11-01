package radon.jujutsu_kaisen.item.veil.modifier;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class ModifierUtils {
    private static final int MAX_MODIFIERS = 8;

    public static Modifier resolve(CompoundTag nbt) {
        Modifier.Type type = Modifier.Type.values()[nbt.getInt("type")];

        return switch (type) {
            case NONE -> new Modifier(nbt);
            case PLAYER -> new PlayerModifier(nbt);
            case COLOR -> new ColorModifier(nbt);
            case TRANSPARENT -> new TransparentModifier(nbt);
            case CURSE -> new CurseModifier(nbt);
            case SORCERER -> new SorcererModifier(nbt);
            case GRIEFING -> new GriefingModifier(nbt);
            case OWNER_BYPASS -> new OwnerBypassModifier(nbt);
            case TELEPORT -> new TeleportModifier(nbt);
            case CURSE_SPAWN -> new CurseSpawnModifier(nbt);
            case SORCERER_SPAWN -> new SorcererSpawnModifier(nbt);
            case VIOLENCE -> new ViolenceModifier(nbt);
            case ADMIN -> new AdminModifier(nbt);
            case DOMAIN -> new DomainModifier(nbt);
            case ABILITY -> new AbilityModifier(nbt);
        };
    }

    public static Modifier create(Modifier.Type type, Modifier.Action action) {
        return switch (type) {
            case CURSE -> new CurseModifier(action);
            case SORCERER -> new SorcererModifier(action);
            case GRIEFING -> new GriefingModifier(action);
            case OWNER_BYPASS -> new OwnerBypassModifier(action);
            case TELEPORT -> new TeleportModifier(action);
            case CURSE_SPAWN -> new CurseSpawnModifier(action);
            case SORCERER_SPAWN -> new SorcererSpawnModifier(action);
            case VIOLENCE -> new ViolenceModifier(action);
            case DOMAIN -> new DomainModifier(action);
            case ABILITY -> new AbilityModifier(action);
            case ADMIN -> new AdminModifier(action);
            case TRANSPARENT -> new TransparentModifier(action);
            default -> new Modifier(type, action);
        };
    }

    public static List<Modifier> getModifiers(CompoundTag nbt) {
        if (!nbt.contains("modifiers")) return List.of();

        ListTag modifiersTag = nbt.getList("modifiers", Tag.TAG_COMPOUND);
        return deserialize(modifiersTag);
    }

    // public static void setModifier(CompoundTag nbt, int index, Modifier modifier) {
    //     if (!nbt.contains("modifiers")) {
    //         ListTag modifiersTag = new ListTag();

    //         for (int i = 0; i < MAX_MODIFIERS; i++) {
    //             modifiersTag.add(new Modifier(Modifier.Type.NONE, Modifier.Action.NONE).serializeNBT());
    //         }
    //         nbt.put("modifiers", modifiersTag);
    //     }
    //     ListTag modifiersTag = nbt.getList("modifiers", Tag.TAG_COMPOUND);
    //     modifiersTag.set(index, modifier.serializeNBT());
    // }
    public static void setModifier(CompoundTag nbt, int index, Modifier modifier) {
        if (index < 0 || index >= MAX_MODIFIERS) {
            return;
        }

        ListTag modifiersTag;
        if (!nbt.contains("modifiers", Tag.TAG_LIST)) {
            modifiersTag = new ListTag();
        } else {
            modifiersTag = nbt.getList("modifiers", Tag.TAG_COMPOUND);
        }

        while (modifiersTag.size() < MAX_MODIFIERS) {
            modifiersTag.add(new Modifier(Modifier.Type.NONE, Modifier.Action.NONE).serializeNBT());
        }

        modifiersTag.set(index, modifier.serializeNBT());
        nbt.put("modifiers", modifiersTag);
    }       

    public static List<Modifier> deserialize(ListTag modifiersTag) {
        List<Modifier> modifiers = new ArrayList<>();

        for (Tag tag : modifiersTag) {
            CompoundTag modifier = (CompoundTag) tag;
            modifiers.add(resolve(modifier));
        }
        return modifiers;
    }

    public static ListTag serialize(List<Modifier> modifiers) {
        ListTag modifiersTag = new ListTag();

        for (Modifier modifier : modifiers) {
            modifiersTag.add(modifier.serializeNBT());
        }
        return modifiersTag;
    }
}
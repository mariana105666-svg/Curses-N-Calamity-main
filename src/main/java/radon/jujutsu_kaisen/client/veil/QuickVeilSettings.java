package radon.jujutsu_kaisen.client.veil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.item.veil.modifier.ColorModifier;
import radon.jujutsu_kaisen.item.veil.modifier.Modifier;
import radon.jujutsu_kaisen.item.veil.modifier.ModifierUtils;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.c2s.SetQuickVeilC2SPacket;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class QuickVeilSettings {
    public static final Modifier.Type[] RULES = {
            Modifier.Type.CURSE,
            Modifier.Type.SORCERER,
            Modifier.Type.GRIEFING,
            Modifier.Type.OWNER_BYPASS,
            Modifier.Type.TELEPORT,
            Modifier.Type.CURSE_SPAWN,
            Modifier.Type.SORCERER_SPAWN,
            Modifier.Type.VIOLENCE
    };

    public static final Modifier.Type[] ADMIN_RULES = {
            Modifier.Type.DOMAIN,
            Modifier.Type.ABILITY
    };

    private static final Set<Modifier.Type> ADMIN_KEEPS =
            EnumSet.of(Modifier.Type.CURSE, Modifier.Type.SORCERER,
                    Modifier.Type.DOMAIN, Modifier.Type.ABILITY);

    public static final int DEFAULT_SIZE = 64;

    private QuickVeilSettings() {
    }

    private static CompoundTag settings() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return new CompoundTag();

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        return cap == null ? new CompoundTag() : cap.getQuickVeil();
    }

    private static void store(CompoundTag nbt) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return;

        cap.setQuickVeil(nbt);

        PacketHandler.sendToServer(new SetQuickVeilC2SPacket(nbt));
    }

    public static Modifier.Action get(Modifier.Type type) {
        CompoundTag nbt = settings();

        if (!nbt.contains(type.name())) return Modifier.Action.NONE;

        int ordinal = nbt.getInt(type.name());

        if (ordinal < 0 || ordinal >= Modifier.Action.values().length) return Modifier.Action.NONE;

        return Modifier.Action.values()[ordinal];
    }

    public static Modifier.Action cycle(Modifier.Type type) {
        Modifier.Action next = switch (get(type)) {
            case NONE -> Modifier.Action.ALLOW;
            case ALLOW -> Modifier.Action.DENY;
            case DENY -> Modifier.Action.NONE;
        };

        CompoundTag nbt = settings().copy();
        nbt.putInt(type.name(), next.ordinal());

        store(nbt);

        return next;
    }

    public static boolean isTransparent() {
        return settings().getBoolean("transparent");
    }

    public static void setTransparent(boolean value) {
        CompoundTag nbt = settings().copy();
        nbt.putBoolean("transparent", value);

        store(nbt);
    }

    public static boolean isAdmin() {
        return settings().getBoolean("admin");
    }

    public static void setAdmin(boolean value) {
        CompoundTag nbt = settings().copy();
        nbt.putBoolean("admin", value);

        store(nbt);
    }

    public static int getColor() {
        CompoundTag nbt = settings();

        return nbt.contains("color") ? nbt.getInt("color") : -1;
    }

    public static int cycleColor() {
        int color = getColor();

        color = color + 1 >= DyeColor.values().length ? -1 : color + 1;

        CompoundTag nbt = settings().copy();
        nbt.putInt("color", color);

        store(nbt);

        return color;
    }

    public static int getSize() {
        CompoundTag nbt = settings();

        return nbt.contains("size") ? nbt.getInt("size") : DEFAULT_SIZE;
    }

    public static void setSize(int value) {
        if (value == getSize()) return;

        CompoundTag nbt = settings().copy();
        nbt.putInt("size", value);

        store(nbt);
    }

    public static List<Modifier> toModifiers() {
        List<Modifier> modifiers = new ArrayList<>();

        boolean transparent = isTransparent();

        if (isAdmin()) {
            modifiers.add(ModifierUtils.create(Modifier.Type.ADMIN, Modifier.Action.ALLOW));

            if (transparent) {
                modifiers.add(ModifierUtils.create(Modifier.Type.TRANSPARENT, Modifier.Action.ALLOW));
            }

            for (Modifier.Type type : ADMIN_KEEPS) {
                Modifier.Action action = get(type);

                if (action == Modifier.Action.NONE) continue;

                modifiers.add(ModifierUtils.create(type, action));
            }
            return modifiers;
        }

        for (Modifier.Type type : RULES) {
            Modifier.Action action = get(type);

            if (action == Modifier.Action.NONE) continue;

            modifiers.add(ModifierUtils.create(type, action));
        }

        if (transparent) {
            modifiers.add(ModifierUtils.create(Modifier.Type.TRANSPARENT, Modifier.Action.ALLOW));
        }

        int color = getColor();

        if (color >= 0) {
            modifiers.add(new ColorModifier(DyeColor.values()[color], Modifier.Action.ALLOW));
        }
        return modifiers;
    }
}

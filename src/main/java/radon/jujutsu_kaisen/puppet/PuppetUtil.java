package radon.jujutsu_kaisen.puppet;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.PuppetEntity;

import java.util.UUID;

public final class PuppetUtil {
    public static final int MAX_NAME_LENGTH = 32;

    private PuppetUtil() {
    }

    public static boolean rename(ServerPlayer player, PuppetRecord record, String name) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);

        if (cap == null) return false;

        String trimmed = name.trim();

        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) return false;
        if (trimmed.equals(record.getName())) return true;
        if (cap.getPuppetByName(trimmed) != null) return false;

        boolean possessed = record.getName().equals(cap.getPossessed());

        record.setName(trimmed);

        if (possessed) cap.setPossessed(trimmed);

        UUID id = record.getEntity();

        if (id != null && player.level() instanceof ServerLevel level
                && level.getEntity(id) instanceof PuppetEntity puppet) {
            puppet.setCustomName(Component.literal(trimmed));
            puppet.setCustomNameVisible(true);
        }

        cap.syncFull();

        return true;
    }
}

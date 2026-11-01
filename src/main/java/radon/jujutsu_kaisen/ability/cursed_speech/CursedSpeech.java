package radon.jujutsu_kaisen.ability.cursed_speech;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.Entity;


import net.minecraft.world.entity.LivingEntity;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;


public abstract class CursedSpeech extends Ability implements ICursedSpeech {
    private static final Map<UUID, List<Entity>> AUDIENCE = new HashMap<>();

    public static void speak(LivingEntity owner, CursedSpeech command, List<Entity> audience) {
        AUDIENCE.put(owner.getUUID(), audience);

        try {
            command.run(owner);
        } finally {
            AUDIENCE.remove(owner.getUUID());
        }
    }

    @Nullable
    protected List<Entity> getAudience(LivingEntity owner) {
        return AUDIENCE.get(owner.getUUID());
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData selfCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (selfCap.isThroatDamaged()) {
            return Status.THROAT;
        }
        return super.isTriggerable(owner);
    }

    // @Override
    // public void run(LivingEntity owner) {
        
    // }
}
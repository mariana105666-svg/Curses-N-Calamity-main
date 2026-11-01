package radon.jujutsu_kaisen.chant;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.chant.ClientChantHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;

import java.util.*;

public class ChantHandler {
    public static boolean isChanted(LivingEntity owner, Ability ability) {
        return getChant(owner, ability) > 0.0F;
    }

    public static float getOutput(LivingEntity owner, Ability ability) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getAbilityOutput(ability) + (getChant(owner, ability));
    }

    public static float getChant(LivingEntity owner, Ability ability) {
        List<String> messages = owner.level().isClientSide ? ClientChantHandler.getMessages() : ServerChantHandler.getMessages(owner);

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        List<String> chants = new ArrayList<>(cap.getFirstChants(ability));

        //if (chants.isEmpty()) return 0.0F;

        //if (messages.isEmpty()) return 0.0F;

        int count = 0;
        int length = 0;

        float countMult = 0.2F;
        float lengthMult = 0.6F;

        float outputMod = 0.0F;

        if (cap.hasBindingVow(BindingVow.CHANTER)) {
        outputMod += -0.2F;
        countMult = 0.5F;
        lengthMult = 1.0F;
        }

        //Iterator<String> iter = chants.iterator();
        int index = 0;
        // for (String chant : messages) {
        //     if (!iter.hasNext() || !chant.equals(iter.next())) break;

        //     count++;
        //     length += chant.length();
        // }
         for (String chant : messages) {
            if (index == chants.size()) break;
            if (!chant.equals(chants.get(index))) continue;

            index++;

            count++;
            length += chant.length();
        }
        float countFactor = (float) count / ConfigHolder.SERVER.maximumChantCount.get();
        float lengthFactor = (float) length / (ConfigHolder.SERVER.maximumChantCount.get() * ConfigHolder.SERVER.maximumChantLength.get());

        if (cap.hasBindingVow(BindingVow.RISK) && (owner.getHealth()/owner.getMaxHealth() < 0.25F ) && ability.isTechnique()) {
            outputMod += 0.4F;
        }
    
        if (chants.isEmpty() || messages.isEmpty() ) {
            countMult = 0;
            countFactor = 0;
            lengthMult = 0;
            lengthFactor = 0;
        }

        return (countMult * countFactor) + (lengthMult * lengthFactor) + (outputMod);
    }

    @Nullable
    public static String next(LivingEntity owner) {
        List<String> messages = owner.level().isClientSide ? ClientChantHandler.getMessages() : ServerChantHandler.getMessages(owner);

        if (messages.isEmpty()) return null;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        Ability ability = cap.getAbility(messages.get(messages.size() - 1));

        if (ability != null) {
            List<String> chants = new ArrayList<>(cap.getFirstChants(ability));

            if (chants.size() == 1) return null;

            int index = 0;

            Iterator<String> iter = chants.iterator();

            for (String chant : messages) {
                if (!iter.hasNext() || !chant.equals(iter.next())) break;

                index++;
            }
            return index < chants.size() ? chants.get(index) : null;
        }
        return null;
    }
}

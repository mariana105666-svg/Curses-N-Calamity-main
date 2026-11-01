package radon.jujutsu_kaisen.ability.barrier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.Configuration;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.effect.PureLoveBeamEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;


public class BarrierSizeConfig extends Configuration {
    @Override
    public boolean isDisplayed(LivingEntity owner) {
        return this.isParentUnlocked(owner);
    }

    @Override
    public int getPointsCost() {
        return 100;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return technique == null || technique.getDomain() == null ? null : technique.getDomain();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(3.0F, -2.0F);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }
}

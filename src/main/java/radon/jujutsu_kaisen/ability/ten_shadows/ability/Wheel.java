package radon.jujutsu_kaisen.ability.ten_shadows.ability;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsMode;
import radon.jujutsu_kaisen.client.JJKPose;
import radon.jujutsu_kaisen.client.JJKPoses;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.ten_shadows.WheelEntity;
import radon.jujutsu_kaisen.entity.ten_shadows.MahoragaEntity;
import radon.jujutsu_kaisen.ability.base.Ability;
import java.util.List;

public class Wheel extends Summon<WheelEntity> implements Ability.IPosedMove {
    public Wheel() {
        super(WheelEntity.class);
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    protected boolean isNotDisabledFromDA() {
        return true;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (owner instanceof MahoragaEntity) return true;
        if (target == null) return false;
        return true;
        // ITenShadowsData ownerCap = owner.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

        // if (target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
        //     ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        //     for (CursedTechnique technique : targetCap.getTechniques()) {
        //         if (!ownerCap.isAdaptedTo(technique)) {
        //             return true;
        //         } else if (JJKAbilities.hasToggled(owner, this) ) {
        //             return true;
        //         }
        //     }
        // }
        // return false;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (!super.isValid(owner)) return false;
        ITenShadowsData cap = owner.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
          ISorcererData ownercap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (owner instanceof net.minecraft.world.entity.player.Player && !ownercap.hasNode(ISorcererData.MAHORAGA_WHEEL_NODE)) return false;

        return !ownercap.hasToggled(JJKAbilities.MAHORAGA.get()) &&
                cap.hasTamed(owner.level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE), JJKEntities.MAHORAGA.get()) &&
                (ownercap.hasToggled(this) || cap.getMode() == TenShadowsMode.ABILITY) && ( ownercap.getExperience() >= ConfigHolder.SERVER.requiredExperienceForExperienced.get() || !ownercap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()) )  ;
    }
    

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.WHEEL.get());
    }

    @Override
    public boolean isTenShadows() {
        return false;
    }

    @Override
    protected WheelEntity summon(LivingEntity owner) {
        return new WheelEntity(owner);
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public float getCost(LivingEntity owner) {
        if (JJKAbilities.hasToggled(owner, JJKAbilities.DOMAIN_AMPLIFICATION.get())) return 0.0F;
        return 0.5F;
    }

    @Override
    public boolean shouldLog(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean display() {
        return false;
    }

    @Override
    public JJKPose getArmPose(LivingEntity entityLiving) {
        return JJKPoses.MAHORAGA_WHEEL;
    }

    @Override 
    public int poseTimer(LivingEntity owner) {
        return 20;
    }

}

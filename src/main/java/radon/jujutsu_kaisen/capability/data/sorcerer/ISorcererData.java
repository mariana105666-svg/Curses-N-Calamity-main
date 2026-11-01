package radon.jujutsu_kaisen.capability.data.sorcerer;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.base.ActivePose;
import radon.jujutsu_kaisen.ability.base.Ability.IPosedMove;
import net.minecraft.world.phys.Vec3;
import radon.jujutsu_kaisen.limb.Limb;
import radon.jujutsu_kaisen.limb.Wound;
import radon.jujutsu_kaisen.puppet.PuppetRecord;

import java.util.*;

@AutoRegisterCapability
public interface ISorcererData {
    float MAX_MASTERY = 100.0F;

    void attack(DamageSource source, LivingEntity target);

    void onRightClick(LivingEntity owner);

    void tick(LivingEntity owner);

    void init(LivingEntity owner);

    int getCursedEnergyColor();

    void setCursedEnergyColor(int color);

    float getMaximumOutput();

    void increaseOutput();

    void decreaseOutput();

    void maxOutput();

    int getPoints();

    void setPoints(int points);

    void addPoints(int points);

    void usePoints(int count);

    boolean isUnlocked(Ability ability);

    void unlock(Ability ability);

    void unlockAll(List<Ability> abilities);

    void lock(Ability ability);

    void lockAll();

    void createPact(UUID recipient, Pact pact);

    Map<UUID, Set<Pact>> getAcceptedPacts();

    boolean hasPact(UUID recipient, Pact pact);

    Player getPactPartner(UUID recipient, Pact pact);

    void removePact(UUID recipient, Pact pact);

    void createPactCreationRequest(UUID recipient, Pact pact);

    void createPactRemovalRequest(UUID recipient, Pact pact);

    void removePactCreationRequest(UUID recipient, Pact pact);

    void removePactRemovalRequest(UUID recipient, Pact pact);

    boolean hasRequestedPactCreation(UUID recipient, Pact pact);

    boolean hasRequestedPactRemoval(UUID recipient, Pact pact);

    void addBindingVow(BindingVow vow);

    void removeBindingVow(BindingVow vow);

    boolean hasBindingVow(BindingVow vow);

    void addBindingVowCooldown(BindingVow vow);

    int getRemainingCooldown(BindingVow vow);

    void resetVows();

    float getAdditionalEnergy();

    void setAdditionalEnergy(float additionalEnergy);

    int getShieldTicks();

    void setShieldTicks(int ticks);

    boolean isCooldownDone(BindingVow vow);

    void addAbilityPose(LivingEntity owner, IPosedMove move, InteractionHand hand, int duration);

    void removeAbilityPose(LivingEntity owner, IPosedMove move);

    Map<ResourceLocation, ActivePose> getActivePoses();

    void addChant(Ability ability, String chant);

    void addChants(Ability ability, Set<String> chants);

    void removeChant(Ability ability, String chant);

    boolean hasChant(Ability ability, String chant);

    boolean isChantsAvailable(Set<String> chants);

    @Nullable Ability getAbility(String chant);

    @Nullable Ability getAbility(Set<String> chants);

    Set<String> getFirstChants();

    Set<String> getFirstChants(Ability ability);

    //void unlockDomain();

    float getOutput();

    float getAbilityPower();

    float getRealPower();

    float getRealPower(float exp);

    float getExperience();

    void setExperience(float experience);

    boolean addExperience(float amount);

    void setDomainConfig(DomainConfiguration config, Object value);

    <T> T getDomainConfig(DomainConfiguration config);

    float getDomainSize();

    void setDomainSize(float domainSize);

    void setShellBalance(boolean shellBalance);

    boolean getShellBalance();

    void setToggleSureHit(boolean toggled);

    boolean getToggleSureHit();

    void setAlliedSureHit(boolean toggled);

    boolean getAlliedSureHit();
    // void blacklistSureHitEntity(UUID entityUUID);

    // Set<UUID> getSureHitEntities();

    //boolean checkSureHitEntity();

    boolean hasToggled(Ability ability);

    @Nullable CursedTechnique getAdditional();

    void setAdditional(CursedTechnique technique);

    @Nullable CursedTechnique getTechnique();
    Set<CursedTechnique> getTechniques();

    boolean hasTechnique(CursedTechnique technique);

    void setTechnique(@Nullable CursedTechnique technique);

    CursedEnergyNature getNature();

    void setNature(CursedEnergyNature nature);

    void setGrade(SorcererGrade grade);

    boolean hasTrait(Trait trait);

    void addTrait(Trait trait);

    void addTraits(List<Trait> traits);

    void removeTrait(Trait trait);

    Set<Trait> getTraits();

    void setTraits(Set<Trait> traits);

    void setType(JujutsuType type);

    JujutsuType getType();

    void toggle(Ability ability);

    void clearToggled();

    Set<Ability> getToggled();

    void clearCooldown(Ability ability);

    void addCooldown(Ability ability);

    void setCooldown(Ability ability, int time);

    int getRemainingCooldown(Ability ability);

    void disrupt(Ability ability, int duration);

    boolean isCooldownDone(Ability ability);

    void addDuration(Ability ability);

    void increaseBrainDamage();
    int getBrainDamage();
    
    void hurtThroat(int cooldown);
    
    int getThroatDamage();

    boolean isThroatDamaged();

    void setBurnout(int duration);

    void setDisable(int duration);

    void setDisarmed(int duration);

    void setSilenced(int duration);

    void setSelfHit(int duration);

    void setExtraMeleeTaken(int duration);

    int getBurnout();

    int getDisable();

    int getDisarmed();

    int getSilenced();

    int getSelfHit();

    int getExtraMeleeTaken();

    boolean hasBurnout();

    boolean hasDisable();

    boolean hasDisarmed();

    boolean hasSilenced();

    boolean hasSelfHit();

    boolean hasExtraMeleeTaken();

    void resetCooldowns();

    void resetBurnout();

    void resetDisable();

    void resetDisarmed();

    void resetSilenced();

    void resetSelfHit();

    void setStoredHealth(float energy);

    void updateMaxHealth();

    float getMaxEnergy();

    void setMaxEnergy(float maxEnergy);

    float getEnergy();

    void addEnergy(float amount);

    void useEnergy(float amount);

    void setEnergy(float energy);

    float getExtraEnergy();

    void addExtraEnergy(float amount);

    void resetExtraEnergy();

    void onBlackFlash();

    long getLastBlackFlashTime();

    boolean addBlackFlash();

    void removeLife();
        
    void setLives(int count);

    int getLives();
    
    boolean hasAdminVeil();

    void setAdminVeil(boolean adminVeil);

    @Nullable String getPossessed();

    void setPossessed(@Nullable String puppet);

    @Nullable UUID getVessel();

    void setVessel(@Nullable UUID vessel);

    default boolean isPossessing() {
        return this.getPossessed() != null;
    }

    net.minecraft.nbt.ListTag getStoredItems();

    void setStoredItems(net.minecraft.nbt.ListTag items);

    float getCooldownReduction();

    float getAbilityOutput(Ability ability);

    void setAbilityOutput(Ability ability, float output);

    void clearAbilityOutput(Ability ability);

    boolean hasAbilityOutput(Ability ability);

    boolean isZoneLocked(Ability ability);

    void setZoneLocked(Ability ability, boolean locked);

    boolean isOutputLocked();

    void setOutputLocked(boolean locked);

    float getAbilityPower(Ability ability);

    boolean isOpenDomain();

    boolean isInside();

    void setInside(boolean inside);

    void syncFull();

    List<PuppetRecord> getPuppets();

    boolean hasPuppetRegister();

    void createPuppetRegister();

    void clearPuppetRegister();

    void addPuppet(PuppetRecord record);

    void removePuppet(PuppetRecord record);

    @Nullable PuppetRecord getPuppetByEntity(UUID entity);

    @Nullable PuppetRecord getPuppetByName(String name);

    String nextPuppetName();

    float getBurnoutSpeed();

    void setBurnoutSpeed(float experience);

    void addBurnoutSpeed(float amount);

    float getBrainRecovery();

    void setBrainRecovery(float experience);

    void addBrainRecovery(float amount);

    boolean hasRewardCooldown(Ability ability);

    void addRewardCooldown(Ability ability, int ticks);

    float getRefinement();

    void setRefinement(float experience);

    void addRefinement(float amount);

    String CE_NODE = "cursed_energy_node";
    String CT_NODE = "cursed_technique_node";
    String CE2_NODE = "cursed_energy_moderate_node";
    String CE3_NODE = "cursed_energy_advanced_node";
    String BARRIER_NODE = "barrier_node";
    String DOMAIN_NODE = "domain_node";
    String DOMAIN_REFINEMENT_NODE = "domain_refinement_node";
    String OPEN_DOMAIN_NODE = "open_domain_node";
    String INSTANT_DOMAIN_NODE = "instant_domain_node";
    String REVERSE_BARRIER_NODE = "reverse_barrier_node";
    String SELECTIVE_TARGETING_NODE = "selective_targeting_node";
    String FUGA_NODE = "fuga_node";
    String SIMPLE_DOMAIN_NODE = "simple_domain_node";
    String ENLARGED_SIMPLE_DOMAIN_NODE = "enlarged_simple_domain_node";
    String QUICK_DRAW_NODE = "quick_draw_node";
    String HOLLOW_WICKER_BASKET_NODE = "hollow_wicker_basket_node";
    String FALLING_BLOSSOM_NODE = "falling_blossom_node";
    String MAXIMUM_METEOR_NODE = "maximum_meteor_node";
    String MINI_UZUMAKI_NODE = "mini_uzumaki_node";
    String MAXIMUM_UZUMAKI_NODE = "maximum_uzumaki_node";
    String DOMAIN_AMPLIFICATION_NODE = "domain_amplification_node";
    String CURSED_ENERGY_BOMB_NODE = "cursed_energy_bomb_node";
    String CURSED_ENERGY_BLAST_NODE = "cursed_energy_blast_node";
    String INCOMPLETE_DOMAIN_NODE = "incomplete_domain_node";
    String RCT_OUTPUT_NODE = "rct_output_node";
    String RCT_OUTPUT_PURIFY_NODE = "rct_output_purify_node";
    String VEIL_NODE = "veil_node";
    String MAHORAGA_NODE = "mahoraga_node";
    String MAHORAGA_WHEEL_NODE = "mahoraga_wheel_node";
    String RCT_NODE = "rct_node";
    String RCT_PURIFY_NODE = "rct_purify_node";
    String BURNOUT_NODE = "burnout_healing_node";
    String BURNOUT_SPEED_NODE = "burnout_speed_node";
    String BRAIN_RECOVERY_NODE = "brain_recovery_node";

    float CE_MAX = 100.0F;
    float CE2_MAX = 250.0F;
    float CE3_MAX = 400.0F;
    float RCT_MAX = 250.0F;
    float RCT2_MASTERY = 100.0F;
    float TECHNIQUE_MASTERY_MAX = 200.0F;
    float BLACK_FLASH_MASTERY = 25.0F;
    float FUGA_MASTERY = 75.0F;
    float SIMPLE_DOMAIN_MAX = 150.0F;
    float SIMPLE_DOMAIN_PER_USE = 0.6F;
    float ENLARGED_SIMPLE_DOMAIN_MASTERY = 100.0F;
    float MINI_UZUMAKI_MASTERY = 150.0F;
    float BURNOUT_STAT_MAX = 100.0F;
    float BURNOUT_SPEED_BONUS = 0.15F;
    float BRAIN_RECOVERY_REDUCTION = 0.3F;
    float BURNOUT_STAT_PER_AWARD = 0.1F;
    int BURNOUT_STAT_INTERVAL = 40;
    float EFFICIENCY_COOLDOWN = 0.15F;

    float BARRIER_REFINEMENT = 100.0F;
    float INSTANT_DOMAIN_REFINEMENT = 200.0F;
    float OPEN_DOMAIN_REFINEMENT = 400.0F;

    int REFINEMENT_INTERVAL = 60;
    float REFINEMENT_PER_INTERVAL = 0.4F;
    float REFINEMENT_PER_CLASH = 0.8F;
    float REFINEMENT_SOFT_CAP = 300.0F;
    float REFINEMENT_SOFT_SHARE = 0.5F;

    float DOMAIN_REFINEMENT_STEP = 100.0F;
    float DOMAIN_BASE_HEALTH = 400.0F;
    float DOMAIN_HEALTH_PER_STEP = 400.0F;
    float DOMAIN_BASE_DAMAGE = 20.0F;
    float DOMAIN_DAMAGE_PER_STEP = 20.0F;

    boolean hasNode(String node);

    void unlockNode(String node);

    void addProgress(float amount);

    boolean hasBlackFlashed();

    void setBlackFlashed(boolean blackFlashed);

    float getTechniqueMastery();

    void setTechniqueMastery(float experience);

    void addTechniqueMastery(float amount);

    float getSimpleDomainMastery();

    void setSimpleDomainMastery(float experience);

    void addSimpleDomainMastery(float amount);

    float getEfficiencyModerate();

    void setEfficiencyModerate(float experience);

    float getEfficiencyAdvanced();

    void setEfficiencyAdvanced(float experience);

    float getReverseEfficiency();

    void setReverseEfficiency(float experience);

    void addReverseEfficiency(float amount);

    float getEfficiency();

    void setEfficiency(float experience);

    void addEfficiency(float amount);

    float getLimb(Limb limb);

    void setLimb(Limb limb, float remaining);

    Wound getWound(Limb limb);

    float getCut(Limb limb);

    void wound(Limb limb, float remaining, Wound type);

    float getBodyHealth();

    void setBodyHealth(float health);

    float getBodyMaxHealth();

    void setBodyMaxHealth(float health);

    @Nullable Vec3 getBodyPos();

    void setBodyPos(@Nullable Vec3 pos);

    float getMastery(Mastery mastery);

    void setMastery(Mastery mastery, float value);

    void addMastery(Mastery mastery, float amount);

    CompoundTag getQuickVeil();

    void setQuickVeil(CompoundTag settings);

    Set<Ability> getBorrowed();

    void borrow(Ability ability);

    void clearBorrowed();

    boolean hasGuaranteedBlackFlash();

    void setGuaranteedBlackFlash(boolean guaranteedBlackFlash);

    void setRevivable(boolean revivable);

    boolean getRevivable();

    boolean checkWombAwakened();

    void setWombAwakened(boolean bool);

    void moreBlackFlash(boolean bool);

    void resetBlackFlash();

    boolean isInZone();

    void delayTickEvent(Runnable task, int delay);
    
    void uncopy(CursedTechnique technique);

    void unsteal(CursedTechnique technique);

    void copy(@Nullable CursedTechnique technique);

  
    void steal(@Nullable CursedTechnique technique);

    void resetSteal();

    void setStolen(Set<CursedTechnique> stolenTechs);
    
    void resetAbsorbed();

    void resetCopy();

    void resetCurses();

    void setCopies(Set<CursedTechnique> copiedTechs);

    Set<CursedTechnique> getCopied();

    void setCurrentCopied(@Nullable CursedTechnique technique);

    @Nullable CursedTechnique getCurrentCopied();

    void absorb(@Nullable CursedTechnique technique);

    void unabsorb(CursedTechnique technique);

    Set<CursedTechnique> getAbsorbed();

    void setCurrentAbsorbed(@Nullable CursedTechnique technique);

    @Nullable CursedTechnique getCurrentAbsorbed();

    Set<CursedTechnique> getStolen();

    void setCurrentStolen(@Nullable CursedTechnique technique);

    @Nullable CursedTechnique getCurrentStolen();

    @Nullable CursedTechnique getLastStolen();

    @Nullable CursedTechnique getLatestStolen();

    void addStolen(CursedTechnique technique);

    boolean hasStolen(CursedTechnique technique);

    // void setStolenSkinTexture(ResourceLocation skin);

    // void setStolenSkinProfile(GameProfile profile);


    // @Nullable GameProfile getStolenSkinProfile();

    // @Nullable ResourceLocation getStolenSkinTexture();

    int getTransfiguredSouls();

    void increaseTransfiguredSouls();

    void decreaseTransfiguredSouls();

    void useTransfiguredSouls(int amount);

    @Nullable Ability getChanneled();

    void channel(@Nullable Ability ability);

    boolean isChanneling(Ability ability);

    int getCharge(Ability ability);

    void wipe(ServerPlayer owner);

    void generate(ServerPlayer owner);

    void addSummon(Entity entity);

    void removeSummon(Entity entity);

    List<Entity> getSummons();

    @Nullable
    <T extends Entity> T getSummonByClass(Class<T> clazz);

    <T extends Entity> List<T> getSummonsByClass(Class<T> clazz);

    <T extends Entity> void unsummonByClass(Class<T> clazz);

    <T extends Entity> void removeSummonByClass(Class<T> clazz);

    <T extends Entity> boolean hasSummonOfClass(Class<T> clazz);

    void addCurse(AbsorbedCurse curse);

    void removeCurse(AbsorbedCurse curse);

    List<AbsorbedCurse> getCurses();

    @Nullable
    AbsorbedCurse getCurse(EntityType<?> type);

    boolean hasCurse(EntityType<?> type);

    List<AbstractMap.SimpleEntry<Vec3, Float>> getFrames();
    void addFrame(Vec3 frame, float yaw);
    void removeFrame(AbstractMap.SimpleEntry<Vec3, Float> frame);
    void resetFrames();

    int getSpeedStacks();
    void addSpeedStack();
    void lowerSpeedStacks();
    void resetSpeedStacks();

    int getFingers();
    void setFingers(int count);
    int addFingers(int count);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);

    void addDash();
    int getDash();
    void subDash();
    void resetDash();
}

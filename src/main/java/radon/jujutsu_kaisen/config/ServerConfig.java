package radon.jujutsu_kaisen.config;

import net.minecraftforge.common.ForgeConfigSpec;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerConfig {
    public final ForgeConfigSpec.DoubleValue cursedEnergyAmount;
    public final ForgeConfigSpec.DoubleValue cursedEnergyRegenerationAmount;
    public final ForgeConfigSpec.DoubleValue cursedEnergyCostAmount;
    public final ForgeConfigSpec.DoubleValue maximumExperienceAmount;
    public final ForgeConfigSpec.DoubleValue Grade4Exp;
    public final ForgeConfigSpec.DoubleValue Grade3Exp;
    public final ForgeConfigSpec.DoubleValue SemiGrade2Exp;
    public final ForgeConfigSpec.DoubleValue Grade2Exp;
    public final ForgeConfigSpec.DoubleValue SemiGrade1Exp;
    public final ForgeConfigSpec.DoubleValue Grade1Exp;
    public final ForgeConfigSpec.DoubleValue SpecialGrade1Exp;
    public final ForgeConfigSpec.DoubleValue SpecialGradeExp;
    public final ForgeConfigSpec.DoubleValue cursedObjectEnergyForGrade;
    public final ForgeConfigSpec.IntValue bindingVowCooldown;
    public final ForgeConfigSpec.IntValue livesconfig;
    public final ForgeConfigSpec.IntValue reverseCursedTechniqueChance;
    public final ForgeConfigSpec.IntValue totemRCTChanceMult;
    public final ForgeConfigSpec.DoubleValue requiredExperienceForExperienced;
    //public final ForgeConfigSpec.DoubleValue domainExpReq;
    public final ForgeConfigSpec.DoubleValue wcsExpMahoReq;
    public final ForgeConfigSpec.DoubleValue wcsExpOtherReq;
    public final ForgeConfigSpec.IntValue sorcererFleshRarity;
    public final ForgeConfigSpec.IntValue curseFleshRarity;
    public final ForgeConfigSpec.IntValue sorcererVillageSpawnRate;
    public final ForgeConfigSpec.IntValue curseVillageSpawnRate;
    public final ForgeConfigSpec.IntValue displayCaseSpawnRate;
    public final ForgeConfigSpec.IntValue displayCaseSpawnRange;
    public final ForgeConfigSpec.IntValue disasterCurseSpawnRate;
    public final ForgeConfigSpec.IntValue minimumSpawnDangerDistance;
    public final ForgeConfigSpec.DoubleValue pointMultiplier;
    public final ForgeConfigSpec.DoubleValue experienceMultiplier;
    public final ForgeConfigSpec.DoubleValue minimumBodyStealEXP;
    public final ForgeConfigSpec.DoubleValue deathPenalty;
    public final ForgeConfigSpec.DoubleValue pointPenalty;
    public final ForgeConfigSpec.DoubleValue pvpGain;
    public final ForgeConfigSpec.IntValue minPoints;
    public final ForgeConfigSpec.IntValue maxPoints;
    public final ForgeConfigSpec.DoubleValue minEXP;
    public final ForgeConfigSpec.DoubleValue maxEXP;
    public final ForgeConfigSpec.IntValue blackFlashChanceRNG;
    public final ForgeConfigSpec.DoubleValue blackFlashPower;
    public final ForgeConfigSpec.DoubleValue blackFlashDmgCap;
    public final ForgeConfigSpec.BooleanValue newShadowStyleForAll;
    public final ForgeConfigSpec.BooleanValue incarnatedSimpleDomain;
    public final ForgeConfigSpec.BooleanValue hwbForAll;
    public final ForgeConfigSpec.BooleanValue realisticShikigami;
    public final ForgeConfigSpec.BooleanValue realisticCurses;
    public final ForgeConfigSpec.BooleanValue sorcererSaturation;
    public final ForgeConfigSpec.BooleanValue curseSaturation;
    public final ForgeConfigSpec.BooleanValue foodCERegen;
    public final ForgeConfigSpec.BooleanValue playerMimicry;
    public final ForgeConfigSpec.BooleanValue playerBodySteal;
    public final ForgeConfigSpec.BooleanValue bodyStealEXPReset;
    public final ForgeConfigSpec.BooleanValue mimicryBodyStealCompat;
    public final ForgeConfigSpec.BooleanValue bodyStealTraits;
    public final ForgeConfigSpec.BooleanValue bodyStealReroll;
    public final ForgeConfigSpec.BooleanValue MBAReroll;
    public final ForgeConfigSpec.BooleanValue MBAEXPReset;
    public final ForgeConfigSpec.BooleanValue MBADeath;
    public final ForgeConfigSpec.BooleanValue wcsCutAnything;
    public final ForgeConfigSpec.BooleanValue hrRequiredForISOH;
    public final ForgeConfigSpec.BooleanValue playerRequiredForRCT;
    public final ForgeConfigSpec.BooleanValue playerRequiredForGradeUp;

    public final ForgeConfigSpec.DoubleValue sorcererHealingAmount;
    public final ForgeConfigSpec.DoubleValue curseHealingAmount;
    public final ForgeConfigSpec.DoubleValue curseDamageMult;
    public final ForgeConfigSpec.DoubleValue curseDefenseMult;
    public final ForgeConfigSpec.DoubleValue sorcererDamageMult;
    public final ForgeConfigSpec.DoubleValue sorcererDefenseMult;
    public final ForgeConfigSpec.DoubleValue jujutsuDefenseMult;
    public final ForgeConfigSpec.DoubleValue hrDefenseMult;
    public final ForgeConfigSpec.DoubleValue playerDamageMult;
    public final ForgeConfigSpec.DoubleValue npcvsnpcDamageMult;
    public final ForgeConfigSpec.DoubleValue playerHPMult;
    public final ForgeConfigSpec.DoubleValue npcHPMult;
    public final ForgeConfigSpec.DoubleValue playerCEArmor;
    public final ForgeConfigSpec.DoubleValue playerCEArmorMax;
    public final ForgeConfigSpec.DoubleValue playerCEArmorMin;
    public final ForgeConfigSpec.IntValue playerHPMin;
    public final ForgeConfigSpec.DoubleValue playerMaxSpeed;
    public final ForgeConfigSpec.DoubleValue HRMaxSpeed;
    public final ForgeConfigSpec.IntValue npcHPMin;
    public final ForgeConfigSpec.DoubleValue hrHPMult;
    public final ForgeConfigSpec.IntValue hrHPMin;
    public final ForgeConfigSpec.DoubleValue playerM1Mult;
    public final ForgeConfigSpec.DoubleValue limitlessNoSixEyesMult;
    public final ForgeConfigSpec.DoubleValue sixEyesMult;
    public final ForgeConfigSpec.DoubleValue perfectBodyMult;




    public final ForgeConfigSpec.BooleanValue uniqueTechniques;
    public final ForgeConfigSpec.BooleanValue uniqueTraits;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> uniqueTraitList;
    public final ForgeConfigSpec.BooleanValue destruction;
    public final ForgeConfigSpec.BooleanValue turboMode;
    public final ForgeConfigSpec.BooleanValue entitySlicing;
    public final ForgeConfigSpec.BooleanValue chantRequiredForWCS;

    public final ForgeConfigSpec.IntValue minimumVeilSize;
    public final ForgeConfigSpec.IntValue maximumVeilSize;
    public final ForgeConfigSpec.DoubleValue minimumDomainSize;
    public final ForgeConfigSpec.DoubleValue maximumDomainSize;

    public final ForgeConfigSpec.IntValue maximumChantCount;
    public final ForgeConfigSpec.IntValue minimumChantLength;
    public final ForgeConfigSpec.IntValue maximumChantLength;
    public final ForgeConfigSpec.DoubleValue chantSimilarityThreshold;

    public final ForgeConfigSpec.IntValue simpleDomainCost;
    public final ForgeConfigSpec.IntValue simpleDomainEnlargementCost;
    public final ForgeConfigSpec.IntValue maximumUzumakiCost;
    public final ForgeConfigSpec.IntValue miniUzumakiCost;
    public final ForgeConfigSpec.IntValue maximumMeteorCost;
    public final ForgeConfigSpec.IntValue ceBombCost;
    public final ForgeConfigSpec.IntValue ceBlastCost;
    public final ForgeConfigSpec.IntValue quickDrawCost;
    public final ForgeConfigSpec.IntValue hollowWickerBasketCost;
    public final ForgeConfigSpec.IntValue fallingBlossomEmotionCost;
    public final ForgeConfigSpec.IntValue domainExpansionCost;
    public final ForgeConfigSpec.IntValue domainAmplificationCost;
    public final ForgeConfigSpec.IntValue zeroPointTwoSecondDomainExpansionCost;
    public final ForgeConfigSpec.IntValue rct2Cost;
    public final ForgeConfigSpec.IntValue rct3Cost;
    public final ForgeConfigSpec.IntValue outputRCTCost;
    public final ForgeConfigSpec.IntValue maximumCopiedTechniques;
    public final ForgeConfigSpec.IntValue maximumStolenTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> unlockableSorcererTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> unlockableCursedSpiritTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sorcererTraitList;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> curseTraitList;
    public final ForgeConfigSpec.ConfigValue<List<String>> incompatibleTraits;

    public final ForgeConfigSpec.IntValue natureTraitModifier;
    public final ForgeConfigSpec.IntValue traitScalingModifier;
    public final ForgeConfigSpec.IntValue natureTraitCost;
    public final ForgeConfigSpec.IntValue traitRolls;
    public final ForgeConfigSpec.IntValue minTraits;
    public final ForgeConfigSpec.IntValue maxTraits;
    public final ForgeConfigSpec.IntValue noTraitWeight;
    public final ForgeConfigSpec.IntValue cursedEnergyNatureRarity;
    public final ForgeConfigSpec.IntValue curseRarity;
    public final ForgeConfigSpec.IntValue sixEyesWeight;
    public final ForgeConfigSpec.IntValue heavenlyRestrictionRarity;
    public final ForgeConfigSpec.IntValue vesselWeight;
    public final ForgeConfigSpec.IntValue rctOutputWeight;
    public final ForgeConfigSpec.IntValue perfectBodyWeight;
    public final ForgeConfigSpec.IntValue incarnatedWeight;
    public final ForgeConfigSpec.IntValue prodigyWeight;
    public final ForgeConfigSpec.IntValue cursedWombWeight;
    public final ForgeConfigSpec.IntValue deathPaintingWeight;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Progression").push("progression");
        this.cursedEnergyAmount = builder.comment("Cursed energy amount (scales with experience)")
                .translation("config.jujutsu_kaisen.cursed_energy_amount")
                .defineInRange("cursedEnergyAmount", 500.0F, 0.0F, 1000000.0F);
        this.cursedEnergyRegenerationAmount = builder.comment("Cursed energy regeneration amount (depends on food level)")
                .translation("config.jujutsu_kaisen.cursed_energy_regeneration_amount")
                .defineInRange("cursedEnergyRegenerationAmount", 0.6F, 0.0F, 100000.0F);
        this.cursedEnergyCostAmount = builder.comment("Drain Multiplier of Cursed Energy (recommended to maintain the ratio with regen)")
                .translation("config.jujutsu_kaisen.cursed_energy_cost_amount")
                .defineInRange("cursedEnergyCostAmount", 1.0F, 0.0F, 100000.0F);
        this.maximumExperienceAmount = builder.comment("The maximum amount of experience one can obtain")
                .translation("config.jujutsu_kaisen.maximum_experience_amount")
                .defineInRange("maximumExperienceAmount", 20000.0F, 1.0F, 1000000.0F);
        this.Grade4Exp = builder.comment("The experience required for Grade 4 (affects npcs)")
                .translation("config.jujutsu_kaisen.grade_4_exp")
                .defineInRange("Grade4Exp", 0.0F, 0.0F, 1000000.0F);
        this.Grade3Exp = builder.comment("The experience required for Grade 3 (affects npcs)")
                .translation("config.jujutsu_kaisen.grade_3_exp")
                .defineInRange("Grade3Exp", 500.0F, 0.0F, 1000000.0F);
        this.SemiGrade2Exp = builder.comment("The experience required for Semi Grade 2 (affects npcs)")
                .translation("config.jujutsu_kaisen.semi_grade_2_exp")
                .defineInRange("SemiGrade2Exp", 1000.0F, 0.0F, 1000000.0F);
        this.Grade2Exp = builder.comment("The experience required for Grade 2 (affects npcs)")
                .translation("config.jujutsu_kaisen.grade_2_exp")
                .defineInRange("Grade2Exp", 1500.0F, 0.0F, 1000000.0F);
        this.SemiGrade1Exp = builder.comment("The experience required for Semi Grade 1 (affects npcs)")
                .translation("config.jujutsu_kaisen.semi_grade_1_exp")
                .defineInRange("SemiGrade1Exp", 2000.0F, 0.0F, 1000000.0F);
        this.Grade1Exp = builder.comment("The experience required for Grade 1 (affects npcs)")
                .translation("config.jujutsu_kaisen.grade_1_exp")
                .defineInRange("Grade1Exp", 2500.0F, 0.0F, 1000000.0F);
        this.SpecialGrade1Exp = builder.comment("The experience required for Special Grade 1 (affects npcs)")
                .translation("config.jujutsu_kaisen.special_grade_1_exp")
                .defineInRange("SpecialGrade1Exp", 3000.0F, 0.0F, 1000000.0F);
        this.SpecialGradeExp = builder.comment("The experience required for Special Grade (affects npcs)")
                .translation("config.jujutsu_kaisen.special_grade_exp")
                .defineInRange("SpecialGradeExp", 4000.0F, 0.0F, 1000000.0F);
        this.cursedObjectEnergyForGrade = builder.comment("The amount of energy consuming cursed objects gives to curses (multiplied by the grade of the object)")
                .translation("config.jujutsu_kaisen.cursed_object_energy_for_grade")
                .defineInRange("cursedObjectEnergyForGrade", 100.0F, 1.0F, 1000.0F);
        this.bindingVowCooldown = builder.comment("Cooldown after removing a Binding Vow in seconds (default is 30 Minutes")
                .translation("config.jujutsu_kaisen.binding_vow_cooldown")
                .defineInRange("bindingVowCooldown", 0, 0, 1800);
        this.livesconfig = builder.comment("Max deaths before a player is rerolled (default 0 = disabled)")
                .translation("config.jujutsu_kaisen.lives_config")
                .defineInRange("livesconfig", 0, 0, 1000);
        this.reverseCursedTechniqueChance = builder.comment("The chance of unlocking reverse cursed technique when dying (smaller number equals bigger chance)")
                .translation("config.jujutsu_kaisen.reverse_cursed_technique_chance")
                .defineInRange("reverseCursedTechniqueChance", 20, 1, 1000);
        this.totemRCTChanceMult = builder.comment("The amount the chance is divided by when holding a totem (raises chances of obtaining rct the higher it is) ")
                .translation("config.jujutsu_kaisen.totem_rct_chance_mult")
                .defineInRange("totemRCTChanceMult", 4, 1, 1000);
        this.requiredExperienceForExperienced = builder.comment("The amount of experience required for a player to be classified as experienced (for now means they can use domain amplification during a domain expansion/wheel w domain amp)")
                .translation("config.jujutsu_kaisen.required_experience_for_experienced")
                .defineInRange("requiredExperienceForExperienced", 5000.0F, 1.0F, 1000000.0F);
        // this.domainExpReq = builder.comment("Experience required to learn a Domain Expansion")
        //         .defineInRange("domainExpReq", 3000.0F, 0.0F, 1000000.0F);
        this.wcsExpMahoReq = builder.comment("Experience required to learn WCS from Mahoraga")
                .translation("config.jujutsu_kaisen.wcs_exp_maho_req")
                .defineInRange("wcsExpMahoReq", 10000.0F, 0.0F, 1000000.0F);
        this.wcsExpOtherReq = builder.comment("Experience required to learn WCS from other sources")
                .translation("config.jujutsu_kaisen.wcs_exp_other_req")
                .defineInRange("wcsExpOtherReq", 10000.0F, 0.0F, 1000000.0F);
        this.sorcererFleshRarity = builder.comment("Rarity of sorcerers dropping flesh (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.sorcerer_flesh_rarity")
                .defineInRange("sorcererFleshRarity", 20, 0, 100000);
        this.curseFleshRarity = builder.comment("Rarity of curses dropping flesh (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.curse_flesh_rarity")
                .defineInRange("curseFleshRarity", 20, 0, 100000);
        this.curseVillageSpawnRate = builder.comment("Rarity of curses spawning in villages (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.curse_village_spawn_rate")
                .defineInRange("curseVillageSpawnRate", 8, 0, 100000);
        this.sorcererVillageSpawnRate = builder.comment("Rarity of sorcerers spawning in villages (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.sorcerer_village_spawn_rate")
                .defineInRange("sorcererVillageSpawnRate", 7, 0, 100000);
        this.displayCaseSpawnRate = builder.comment("Rarity of curses spawning from display cases (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.display_case_spawn_rate")
                .defineInRange("displayCaseRarity", 11, 0, 100000);
        this.displayCaseSpawnRange = builder.comment("Range in blocks curses can spawn from display cases")
                .translation("config.jujutsu_kaisen.display_case_spawn_range")
                .defineInRange("displayCaseSpawnRange", 64, 0, 100000);
        this.disasterCurseSpawnRate = builder.comment("Rarity of disaster curses (bigger value means more rare, 0 to disable)")
                .translation("config.jujutsu_kaisen.disaster_curse_spawn_rate")
                .defineInRange("disasterCurseSpawnRate", 12, 0, 100000);
        this.minimumSpawnDangerDistance = builder.comment("The minimum distance from spawn of dangerous things such as disaster curses")
                .translation("config.jujutsu_kaisen.minimum_spawn_danger_distance")
                .defineInRange("minimumSpawnDangerDistance", 1000, 0, 100000);
        this.experienceMultiplier = builder.comment("Scale of experience you gain")
                .translation("config.jujutsu_kaisen.experience_multiplier")
                .defineInRange("experienceMultiplier", 1.0F, 0.0F, 100.0F);
        this.minimumBodyStealEXP = builder.comment("Minimum EXP before a body can be stolen")
                .translation("config.jujutsu_kaisen.minimum_body_steal_exp")
                .defineInRange("minimumBodyStealEXP", 0.0F, 0.0F, 100000.0F);
        this.pointMultiplier = builder.comment("Scale of ability points you gain")
                .translation("config.jujutsu_kaisen.point_multiplier")
                .defineInRange("pointMultiplier", 0.13F, 0.0F, 100.0F);
        this.pointPenalty = builder.comment("Scale of points lost on death")
                .translation("config.jujutsu_kaisen.point_penalty")
                .defineInRange("pointPenalty", 0.0F, 0.0F, 100.0F);
        this.deathPenalty = builder.comment("Percentage of experience lost on death")
                .translation("config.jujutsu_kaisen.death_penalty")
                .defineInRange("deathPenalty", 0.0F, 0.0F, 1.0F);
        this.minPoints = builder.comment("Minimum points gained from battles")
                .translation("config.jujutsu_kaisen.min_points")
                .defineInRange("minPoints", 0, 0, 9999999);
        this.maxPoints = builder.comment("Maximum points gained from battles")
                .translation("config.jujutsu_kaisen.max_points")
                .defineInRange("maxPoints", 0, 0, 9999999);
        this.minEXP = builder.comment("Minimum experience gained from battles")
                .translation("config.jujutsu_kaisen.min_exp")
                .defineInRange("minExp", 1.0F, 0.0F, 9999999.0F);
        this.maxEXP = builder.comment("Maximum experience gained from battles (0 to disable)")
                .translation("config.jujutsu_kaisen.max_exp")
                .defineInRange("maxExp", 0.0F, 0.0F, 9999999.0F);
        this.pvpGain = builder.comment("Percentage of experience gained from player kills")
                .translation("config.jujutsu_kaisen.pvp_gain")
                .defineInRange("pvpGain", 1.0F, 0.0F, 999.0F);
        this.blackFlashChanceRNG = builder.comment("The chance of black flash (smaller number equals bigger chance)")
                .translation("config.jujutsu_kaisen.black_flash_chance_rng")
                .defineInRange("blackFlashChanceRNG", 200, 0, 1000);
        this.blackFlashPower = builder.comment("The multiplier a black flash ('power of' multiplier to bfs, canon by default)")
                .translation("config.jujutsu_kaisen.black_flash_power")
                .defineInRange("blackFlashPower", 2.5F, 1.0F, 1000.0F);
        this.blackFlashDmgCap = builder.comment("The maximum damage of a black flash attack (entire dmg not just the added dmg)")
                .translation("config.jujutsu_kaisen.black_flash_dmg_cap")
                .defineInRange("blackFlashDmgCap", 40.0F, 0.0F, 999999.0F);
        this.newShadowStyleForAll = builder.comment("When enabled anyone may learn advanced Simple Domain Techs (techniqueless is useless with this)")
                .translation("config.jujutsu_kaisen.new_shadow_style_for_all")
                .define("newShadowStyleForAll", false);
        this.incarnatedSimpleDomain = builder.comment("When enabled Incarnated Sorcerers may use Simple Domain")
                .translation("config.jujutsu_kaisen.incarnated_simple_domain")
                .define("incarnatedSimpleDomain", true);
        this.hwbForAll = builder.comment("When enabled all Sorcerers may learn Hollow Wicker Basket (for earlier age playthroughs)")
                .translation("config.jujutsu_kaisen.incarnated_simple_domain")
                .define("hwbForAll", false);
        this.realisticShikigami = builder.comment("When enabled Ten Shadows shikigami will die permanently")
                .translation("config.jujutsu_kaisen.realistic_shikigami")
                .define("realisticShikigami", false);
        this.realisticCurses = builder.comment("When enabled curses only take damage from jujutsu attacks")
                .translation("config.jujutsu_kaisen.realistic_curses")
                .define("realisticCurses", true);
        this.sorcererSaturation = builder.comment("When enabled Sorcerers will always have their hunger filled.")
                .translation("config.jujutsu_kaisen.sorcerer_saturation")
                .define("sorcererSaturation", true);
        this.curseSaturation = builder.comment("When enabled Curses will always have their hunger filled.")
                .translation("config.jujutsu_kaisen.curse_saturation")
                .define("curseSaturation", true);
        this.foodCERegen = builder.comment("When enabled Cursed Energy regeneration speed will scale off hunger.")
                .translation("config.jujutsu_kaisen.food_ce_regen")
                .define("foodCERegen", true);
        this.playerBodySteal = builder.comment("When enabled Body Steal only works on players")
                .translation("config.jujutsu_kaisen.player_body_steal")
                .define("playerBodySteal", false);
        this.playerMimicry = builder.comment("When enabled Mimicry only works on players")
                .translation("config.jujutsu_kaisen.player_mimicry")
                .define("playerMimicry", false);
        this.mimicryBodyStealCompat = builder.comment("When enabled Mimicry and Body Steal may steal from each other.")
                .translation("config.jujutsu_kaisen.mimicry_body_steal_compat")
                .define("mimicryBodyStealCompat", true);
        this.bodyStealTraits = builder.comment("Whether Body Steal should steal traits, besides RCT Output and Heavenly Restriction.")
                .translation("config.jujutsu_kaisen.body_steal_traits")
                .define("bodyStealTraits", true);
        this.bodyStealEXPReset = builder.comment("Whether Body Steal should reset the EXP of the stolen player")
                .translation("config.jujutsu_kaisen.body_steal_exp_reset")
                .define("bodyStealEXPReset", true);
        this.bodyStealReroll = builder.comment("Whether Body Steal should reroll the player.")
                .translation("config.jujutsu_kaisen.body_steal_reroll")
                .define("bodyStealReroll", false);
        this.MBADeath = builder.comment("Whether Mythical Beast Amber should kill the user after use")
                .translation("config.jujutsu_kaisen.mba_death")
                .define("MBADeath", true);
        this.MBAEXPReset = builder.comment("Whether Mythical Beast Amber should reset the EXP of the user after use")
                .translation("config.jujutsu_kaisen.mba_exp_reset")
                .define("MBAEXPReset", true);
        this.MBAReroll = builder.comment("Whether Mythical Beast Amber should reroll the player.")
                .translation("config.jujutsu_kaisen.mba_reroll")
                .define("MBAReroll", false);
        this.wcsCutAnything = builder.comment("Whether World Cutting Slash truly cuts the world (destroys indestructible blocks).")
                .translation("config.jujutsu_kaisen.wcs_cut_anything")
                .define("wcsCutAnything", true);
        this.hrRequiredForISOH = builder.comment("Whether Heavenly Restriction is required to use the Inverted Spear of Heaven")
                .translation("config.jujutsu_kaisen.hr_required_for_isoh")
                .define("hrRequiredForISOH", false);
        this.playerRequiredForRCT = builder.comment("Whether Players must kill you in order for you to unlock RCT")
                .translation("config.jujutsu_kaisen.player_required_for_rct")
                .define("playerRequiredForRCT", false);
        this.playerRequiredForGradeUp = builder.comment("Whether Players must kill other players in order to Rank Up ")
                .translation("config.jujutsu_kaisen.player_required_for_grade_up")
                .define("playerRequiredForGradeUp", false);
        builder.pop();
    

        builder.comment("Miscellaneous").push("misc");
        this.sorcererHealingAmount = builder.comment("The maximum amount of health sorcerers can heal per tick (scales with experience)")
                .translation("config.jujutsu_kaisen.sorcerer_healing_amount")
                .defineInRange("sorcererHealingAmount", 0.1F, 0.0F, 2.5F);
        this.curseHealingAmount = builder.comment("The maximum amount of health curses can heal per tick (scales with experience)")
                .translation("config.jujutsu_kaisen.curse_healing_amount")
                .defineInRange("curseHealingAmount", 0.15F, 0.0F, 2.5F);
        this.curseDamageMult = builder.comment("The multiplier on the damage NPC curses deal to you")
                .translation("config.jujutsu_kaisen.curse_damage_mult")
                .defineInRange("curseDamageMult", 0.7F, 0.0F, 9999.0F);
        this.curseDefenseMult = builder.comment("The multiplier on damage NPC curses take from you")
                .translation("config.jujutsu_kaisen.curse_defense_mult")
                .defineInRange("curseDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.sorcererDamageMult = builder.comment("The multiplier on the damage NPC sorcerers deal to you")
                .translation("config.jujutsu_kaisen.sorcerer_damage_mult")
                .defineInRange("sorcererDamageMult", 0.7F, 0.0F, 9999.0F);
        this.sorcererDefenseMult = builder.comment("The multiplier on damage NPC sorcerers take from you")
                .translation("config.jujutsu_kaisen.sorcerer_defense_mult")
                .defineInRange("sorcererDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.jujutsuDefenseMult = builder.comment("The multiplier to standard players' defense")
                .translation("config.jujutsu_kaisen.jujutsu_defense_mult")
                .defineInRange("jujutsuDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.hrDefenseMult = builder.comment("The multiplier to Heavenly Restriction players's defense (already higher outside of config)")
                .translation("config.jujutsu_kaisen.hr_defense_mult")
                .defineInRange("hrDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.playerDamageMult = builder.comment("The multiplier to all player attacks (includes summons of all kinds)")
                .translation("config.jujutsu_kaisen.player_damage_mult")
                .defineInRange("playerDamageMult", 1.0F, 0.0F, 9999.0F);
        this.npcvsnpcDamageMult = builder.comment("The multiplier to npc vs npc damage")
                .translation("config.jujutsu_kaisen.npc_vs_npc_damage_mult")
                .defineInRange("npcvsnpcDamageMult", 0.85F, 0.0F, 9999.0F);
        this.playerHPMult = builder.comment("The multiplier to player HP (scales by bars, so will move by 20 hp increments)")
                .translation("config.jujutsu_kaisen.player_hp_mult")
                .defineInRange("playerHPMult", 15.0F, 0.0F, 9999.0F);
        this.npcHPMult = builder.comment("The multiplier to npc HP (scales by bars, so will move by 20 hp increments)")
                .translation("config.jujutsu_kaisen.npc_hp_mult")
                .defineInRange("npcHPMult", 15.0F, 0.0F, 9999.0F);
        this.playerCEArmor = builder.comment("The multiplier to player armor with ce flow/hr (does not pass cap)")
                .translation("config.jujutsu_kaisen.player_ce_armor")
                .defineInRange("playerCEArmorMult", 8.0F, 0.0F, 9999.0F);
        this.playerCEArmorMin = builder.comment("The minimum boost to player armor with ce flow/hr (does not pass cap)")
                .translation("config.jujutsu_kaisen.player_ce_armor_min")
                .defineInRange("playerCEArmorMin", 12.0F, 0.0F, 9999.0F);
        this.playerCEArmorMax = builder.comment("The maximum boost to player armor with ce flow/hr (This is the cap, does not stack w reg armor)")
                .translation("config.jujutsu_kaisen.player_ce_armor_max")
                .defineInRange("playerCEArmorMax", 20.0F, 0.0F, 9999.0F);
        this.playerMaxSpeed = builder.comment("The maximum boost to player movement speed, scales to 0.32 at 20k exp by default (DOES NOT CAP PROJECTION OR SCALE SPEED HIGHER).")
                .translation("config.jujutsu_kaisen.player_max_speed")
                .defineInRange("playerMaxSpeed", 0.32F, 0.0F, 9999.0F);
        this.HRMaxSpeed = builder.comment("The maximum boost to Heavenly Restriction movement speed, scales to 0.8 at 20k by default (DOES NOT SCALE SPEED HIGHER)")
                .translation("config.jujutsu_kaisen.hr_max_speed")
                .defineInRange("HRMaxSpeed", 0.8F, 0.0F, 9999.0F);
        this.playerHPMin = builder.comment("The minimum health of a player.")
                .translation("config.jujutsu_kaisen.player_hp_min")
                .defineInRange("playerHPMin", 40, 1, 9999);
        this.hrHPMult = builder.comment("The multiplier to a heavenly restriction player's HP (scales by bars, so will move by 20 hp increments)")
                .translation("config.jujutsu_kaisen.hr_hp_mult")
                .defineInRange("hrHPMult", 15.0F, 0.0F, 9999.0F);
        this.hrHPMin = builder.comment("The minimum health of a Heavenly Restriction player.")
                .translation("config.jujutsu_kaisen.hr_hp_min")
                .defineInRange("hrHPMin", 40, 1, 9999);
        this.npcHPMin = builder.comment("The minimum health of the mod's NPCs.")
                .translation("config.jujutsu_kaisen.npc_hp_min")
                .defineInRange("npcHPMin", 40, 1, 9999);
        this.playerM1Mult = builder.comment("The multiplier to player M1 Hit Damage")
                .translation("config.jujutsu_kaisen.player_m1_mult")
                .defineInRange("playerM1Mult", 2.0F, 0.0F, 9999.0F);
        this.limitlessNoSixEyesMult = builder.comment("The multiplier to Limitless's costs without Six Eyes")
                .translation("config.jujutsu_kaisen.limitless_no_six_eyes_mult")
                .defineInRange("limitlessNoSixEyesMult", 1.0F, 0.0F, 9999.0F);
        this.sixEyesMult = builder.comment("The multiplier of drain decreases given to Six Eyes.")
                .translation("config.jujutsu_kaisen.six_eyes_mult")
                .defineInRange("sixEyesMult", 0.5F, 0.0F, 9999.0F);
        this.perfectBodyMult = builder.comment("The multiplier of Perfect Body's damage.")
                .translation("config.jujutsu_kaisen.perfect_body_mult")
                .defineInRange("perfectBodyMult", 1.5F, 0.0F, 9999.0F);

        this.uniqueTechniques = builder.comment("When enabled on servers every player will have a unique technique if any are available")
                .translation("config.jujutsu_kaisen.unique_techniques")
                .define("uniqueTechniques", true);
        this.uniqueTraits = builder.comment("When enabled on servers there can be only one Six Eyes and Perfect Body")
                .translation("config.jujutsu_kaisen.unique_traits")
                .define("uniqueTraits", true);
        this.uniqueTraitList = builder.comment("Traits that will be Unique under the config")
                .translation("config.jujutsu_kaisen.unique_trait_list")
                .defineList("uniqueTraitList", () -> List.of(
                                Trait.SIX_EYES.name(),
                                Trait.PERFECT_BODY.name()
                        ), ignored -> true);
        this.destruction = builder.comment("When enabled abilities break blocks")
                .translation("config.jujutsu_kaisen.destruction")
                .define("destruction", true);
        this.turboMode = builder.comment("When enabled abilities have no cooldowns for players")
                .translation("config.jujutsu_kaisen.turbo_mode")
                .define("turboMode", false);
        this.entitySlicing = builder.comment("When enabled entities are sliced by Dismantle (may cause shader/mod incompat)")
                .translation("config.jujutsu_kaisen.entity_slicing")
                .define("entitySlicing", false);
        this.chantRequiredForWCS = builder.comment("When enabled WCS must be chanted to 150%")
                .translation("config.jujutsu_kaisen.chant_required_for_wcs")
                .define("chantRequiredForWCS", true);
        builder.pop();

        builder.comment("Veils").push("veils");
        this.minimumVeilSize = builder.comment("Minimum size for a veil")
                .translation("config.jujutsu_kaisen.minimum_veil_size")
                .defineInRange("minimumVeilSize", 4, 4, 64);
        this.maximumVeilSize = builder.comment("Maximum size for a veil")
                .translation("config.jujutsu_kaisen.maximum_veil_size")
                .defineInRange("maximumVeilSize", 64, 32, 256);
        builder.pop();

        builder.comment("Domains").push("domains");
        this.minimumDomainSize = builder.comment("Minimum size for a domain")
                .translation("config.jujutsu_kaisen.minimum_domain_size")
                .defineInRange("minimumDomainSize", 0.5F, 0.2F, 1.0F);
        this.maximumDomainSize = builder.comment("Maximum size for a domain")
                .translation("config.jujutsu_kaisen.maximum_domain_size")
                .defineInRange("maximumDomainSize", 1.5F, 1.0F, 10.0F);
        builder.pop();

        builder.comment("Chants").push("chants");
        this.maximumChantCount = builder.comment("Maximum count for chants")
                .translation("config.jujutsu_kaisen.maximum_chant_count")
                .defineInRange("maximumChantCount", 5, 1, 16);
        this.minimumChantLength = builder.comment("Maximum length for a chant")
                .translation("config.jujutsu_kaisen.minimum_chant_length")
                .defineInRange("minimumChantLength", 2, 1, 256);
        this.maximumChantLength = builder.comment("Maximum length for a chant")
                .translation("config.jujutsu_kaisen.maximum_chant_length")
                .defineInRange("maximumChantLength", 24, 1, 256);
        this.chantSimilarityThreshold = builder.comment("Minimum difference between chants for them to be valid")
                .translation("config.jujutsu_kaisen.chant_similarity_threshold")
                .defineInRange("chantSimilarityThreshold", 0.25F, 0.0F, 1.0F);
        builder.pop();

        builder.comment("Abilities").push("abilities");
        this.simpleDomainCost = builder.comment("The amount of points simple domain costs to unlock")
                .translation("config.jujutsu_kaisen.simple_domain_cost")
                .defineInRange("simpleDomainCost", 25, 1, 10000);
        this.simpleDomainEnlargementCost = builder.comment("The amount of points simple domain enlargement costs to unlock")
                .translation("config.jujutsu_kaisen.simple_domain_enlargement_cost")
                .defineInRange("simpleDomainEnlargementCost", 50, 1, 10000);
        this.quickDrawCost = builder.comment("The amount of points quick draw costs to unlock")
                .translation("config.jujutsu_kaisen.quick_draw_cost")
                .defineInRange("simpleDomainCost", 25, 1, 10000);
        this.fallingBlossomEmotionCost = builder.comment("The amount of points falling blossom emotion costs to unlock")
                .translation("config.jujutsu_kaisen.falling_blossom_emotion_cost")
                .defineInRange("fallingBlossomEmotionCost", 25, 1, 10000);
        this.hollowWickerBasketCost = builder.comment("The amount of points hollow wicker basket costs to unlock")
                .translation("config.jujutsu_kaisen.hollow_wicker_basket_cost")
                .defineInRange("hollowWickerBasketCost", 25, 1, 10000);
        this.domainExpansionCost = builder.comment("The amount of points domain expansion costs to unlock")
                .translation("config.jujutsu_kaisen.domain_expansion_cost")
                .defineInRange("domainExpansionCost", 200, 1, 10000);
        this.domainAmplificationCost = builder.comment("The amount of points domain amplification costs to unlock")
                .translation("config.jujutsu_kaisen.domain_amplification_cost")
                .defineInRange("domainAmplificationCost", 50, 1, 10000);
        this.zeroPointTwoSecondDomainExpansionCost = builder.comment("The amount of points 0.2s domain expasnion costs to unlock")
                .translation("config.jujutsu_kaisen.zero_point_two_second_domain_expansion_cost")
                .defineInRange("zeroPointTwoSecondDomainExpansionCost", 75, 1, 10000);
        this.miniUzumakiCost = builder.comment("The amount of points Mini Uzumaki costs to unlock")
                .translation("config.jujutsu_kaisen.mini_uzumaki_cost")
                .defineInRange("miniUzumakiCost", 50, 1, 10000);
        this.maximumUzumakiCost = builder.comment("The amount of points Maximum: Uzumaki costs to unlock")
                .translation("config.jujutsu_kaisen.maximum_uzumaki_cost")
                .defineInRange("maximumUzumakiCost", 75, 1, 10000);
        this.maximumMeteorCost = builder.comment("The amount of points Maximum: Meteor costs to unlock")
                .translation("config.jujutsu_kaisen.maximum_meteor_cost")
                .defineInRange("maximumMeteorCost", 100, 1, 10000);
        this.ceBombCost = builder.comment("The amount of points Cursed Energy Bomb costs to unlock")
                .translation("config.jujutsu_kaisen.ce_bomb_cost")
                .defineInRange("ceBombCost", 25, 1, 10000);
        this.ceBlastCost = builder.comment("The amount of points Cursed Energy Blast costs to unlock")
                .translation("config.jujutsu_kaisen.ce_blast_cost")
                .defineInRange("ceBlastCost", 25, 1, 10000);
        this.rct2Cost = builder.comment("The amount of points tier 2 RCT costs to unlock")
                .translation("config.jujutsu_kaisen.rct2_cost")
                .defineInRange("rct2Cost", 50, 1, 10000);
        this.rct3Cost = builder.comment("The amount of points tier 3 RCT costs to unlock")
                .translation("config.jujutsu_kaisen.rct3_cost")
                .defineInRange("rct3Cost", 100, 1, 10000);
        this.outputRCTCost = builder.comment("The amount of points output RCT costs to unlock")
                .translation("config.jujutsu_kaisen.output_rct_cost")
                .defineInRange("outputRCTCost", 75, 1, 10000);
        this.maximumCopiedTechniques = builder.comment("The amount of techniques mimicry can copy")
                .translation("config.jujutsu_kaisen.maximum_copied_techniques")
                .defineInRange("maximumCopiedTechniques", 3, 1, 10000);
        this.maximumStolenTechniques = builder.comment("The amount of techniques that can be stolen")
                .translation("config.jujutsu_kaisen.maximum_stolen_techniques")
                .defineInRange("maximumStolenTechniques", 2, 1, 10000);
        this.unlockableSorcererTechniques = builder.comment("Techniques that are unlockable for sorcerers by default")
                .translation("config.jujutsu_kaisen.unlockable_sorcerer_techniques")
                .defineList("unlockableSorcererTechniques", () -> List.of(
                                CursedTechnique.CURSE_MANIPULATION.name(),
                                CursedTechnique.LIMITLESS.name(),
                                CursedTechnique.SHRINE.name(),
                                CursedTechnique.CURSED_SPEECH.name(),
                                CursedTechnique.MIMICRY.name(),
                                CursedTechnique.DISASTER_FLAMES.name(),
                                CursedTechnique.DISASTER_TIDES.name(),
                                CursedTechnique.DISASTER_PLANTS.name(),
                                CursedTechnique.ANGEL.name(),
                                CursedTechnique.BRAIN_TRANSPLANT.name(),
                                CursedTechnique.TEN_SHADOWS.name(),
                                CursedTechnique.BOOGIE_WOOGIE.name(),
                                CursedTechnique.PROJECTION_SORCERY.name(),
                                CursedTechnique.RATIO.name(),
                                CursedTechnique.MYTHICAL_BEAST_AMBER.name(),
                                CursedTechnique.CURSED_ENERGY_DISCHARGE.name(),
                                CursedTechnique.TECHNIQUELESS.name()
                        ),
                        ignored -> true
                );

        this.unlockableCursedSpiritTechniques = builder.comment("Techniques that are unlockable by Curses by default")
                .translation("config.jujutsu_kaisen.unlockable_cursed_spirit_techniques")
                .defineList("unlockableCursedSpiritTechniques", () -> List.of(
                                CursedTechnique.CURSE_MANIPULATION.name(),
                                CursedTechnique.LIMITLESS.name(),
                                CursedTechnique.SHRINE.name(),
                                CursedTechnique.CURSED_SPEECH.name(),
                                CursedTechnique.MIMICRY.name(),
                                CursedTechnique.DISASTER_FLAMES.name(),
                                CursedTechnique.DISASTER_TIDES.name(),
                                CursedTechnique.DISASTER_PLANTS.name(),
                                CursedTechnique.ANGEL.name(),
                                CursedTechnique.IDLE_TRANSFIGURATION.name(),
                                CursedTechnique.TEN_SHADOWS.name(),
                                CursedTechnique.BOOGIE_WOOGIE.name(),
                                CursedTechnique.PROJECTION_SORCERY.name(),
                                CursedTechnique.RATIO.name(),
                                CursedTechnique.MYTHICAL_BEAST_AMBER.name(),
                                CursedTechnique.CURSED_ENERGY_DISCHARGE.name(),
                                CursedTechnique.TECHNIQUELESS.name()
                        ),
                        ignored -> true
                );

           this.sorcererTraitList = builder.comment("Traits that can be rolled by Sorcerers (HR and Simurian may not be added here or else it might crash)")
                .translation("config.jujutsu_kaisen.sorcerer_trait_list")
                .defineList("sorcererTraitList", () -> List.of(
                                Trait.PRODIGY.name(),
                                Trait.PERFECT_BODY.name(),
                                Trait.RCT_OUTPUT.name(),
                                Trait.INCARNATED.name(),
                                Trait.VESSEL.name(),
                                Trait.SIX_EYES.name()
                        ), ignored -> true);
           this.curseTraitList = builder.comment("Traits that can be rolled by Curses (HR and Simurian may not be added here or else it might crash)")
                .translation("config.jujutsu_kaisen.curse_trait_list")
                .defineList("curseTraitList", () -> List.of(
                                Trait.PRODIGY.name(),
                                Trait.PERFECT_BODY.name(),
                                Trait.CURSED_WOMB.name(),
                                Trait.DEATH_PAINTING.name()
                        ), ignored -> true);
           this.incompatibleTraits = builder.comment("Incompatible traits, formatted as TRAIT1,TRAIT2. To add more, just add more comma separated traits to the list")
                .translation("config.jujutsu_kaisen.incompatible_traits")
                .define("incompatibleTraits", List.of("PERFECT_BODY,SIX_EYES", "CURSED_WOMB,DEATH_PAINTING"), ignored -> true);
        builder.pop();

        builder.comment("Rarity").push("rarity");
        this.traitRolls = builder.comment("How many rolls are done to give players traits? (each minimum guaranteed trait takes a trait roll)")
                .translation("config.jujutsu_kaisen.trait_rolls")
                .defineInRange("traitRolls", 3, 0, 1000000);
        this.minTraits = builder.comment("The minimum amount of traits a player will start with")
                .translation("config.jujutsu_kaisen.min_traits")
                .defineInRange("minTraits", 0 ,0, 1000000);
        this.maxTraits = builder.comment("The maximum amount of traits a player can start with")
                .translation("config.jujutsu_kaisen.max_traits")
                .defineInRange("maxTraits", 6 ,0, 1000000);
        this.natureTraitCost = builder.comment("Cursed Energy Nature trait count cost (affected by the minimum/max amount of traits)")
                .translation("config.jujutsu_kaisen.nature_trait_cost")
                .defineInRange("natureTraitCost", 1 ,0, 1000000);
        this.natureTraitModifier = builder.comment("The division to the chance of rolling a trait with a nature")
                .translation("config.jujutsu_kaisen.nature_trait_modifier")
                .defineInRange("natureTraitModifier", 2 ,0, 1000000);
        this.traitScalingModifier = builder.comment("The division to the chance of rolling an additional trait per trait rolled")
                .translation("config.jujutsu_kaisen.trait_scaling_modifier")
                .defineInRange("traitScalingModifier", 2 ,0, 1000000);
        this.noTraitWeight = builder.comment("Weight of receiving no trait")
                .translation("config.jujutsu_kaisen.no_trait_weight")
                .defineInRange("noTraitWeight", 800, 0, 1000000);
        this.cursedEnergyNatureRarity = builder.comment("Weight of a cursed energy nature other than basic (1/value chance, bigger value = rarer)")
                .translation("config.jujutsu_kaisen.cursed_energy_nature_rarity")
                .defineInRange("cursedEnergyNatureRarity", 15, 0, 1000000);
        this.curseRarity = builder.comment("Rarity of being a curse (1/value chance, bigger value = rarer")
                .translation("config.jujutsu_kaisen.curse_rarity")
                .defineInRange("curseRarity", 4, 0, 1000000);
        this.sixEyesWeight = builder.comment("Weight of having six eyes (lower value = rarer)")
                .translation("config.jujutsu_kaisen.six_eyes_weight")
                .defineInRange("sixEyesWeight", 4, 0, 1000000);
        this.heavenlyRestrictionRarity = builder.comment("Rarity of heavenly restriction (1/value chance, bigger value = rarer")
                .translation("config.jujutsu_kaisen.heavenly_restriction_rarity")
                .defineInRange("heavenlyRestrictionRarity", 30, 0, 1000000);
        this.vesselWeight = builder.comment("Weight of being a vessel (lower value = rarer)")
                .translation("config.jujutsu_kaisen.vessel_weight")
                .defineInRange("vesselWeight", 12, 0, 1000000);
        this.perfectBodyWeight = builder.comment("Weight of having a perfect body (lower value = rarer)")
                .translation("config.jujutsu_kaisen.perfect_body_weight")
                .defineInRange("perfectBodyWeight", 4, 0, 1000000);
        this.incarnatedWeight = builder.comment("Weight of being incarnated (lower value = rarer)")
                .translation("config.jujutsu_kaisen.incarnated_weight")
                .defineInRange("incarnatedWeight", 36, 0, 1000000);
        this.rctOutputWeight = builder.comment("Weight of being able to output your RCT (lower value = rarer)")
                .translation("config.jujutsu_kaisen.rct_output_weight")
                .defineInRange("rctOutputWeight", 30, 0, 1000000);
        this.prodigyWeight = builder.comment("Weight of having immense development potential (lower value = rarer)")
                .translation("config.jujutsu_kaisen.prodigy_weight")
                .defineInRange("prodigyWeight", 8, 0, 1000000);
        this.cursedWombWeight = builder.comment("Weight of forming as a Cursed Womb (lower value = rarer)")
                .translation("config.jujutsu_kaisen.cursed_womb_weight")
                .defineInRange("cursedWombWeight", 24, 0, 1000000);
        this.deathPaintingWeight = builder.comment("Weight of having been born as a Death Painting (lower value = rarer)")
                .translation("config.jujutsu_kaisen.death_painting_weight")
                .defineInRange("deathPaintingWeight", 16, 0, 1000000);
        builder.pop();
    }


    

    public List<CursedTechnique> getUnlockableTechniques(JujutsuType type) {
        if (type == JujutsuType.SORCERER) {
                return this.unlockableSorcererTechniques.get().stream()
                .map(CursedTechnique::valueOf)
                .collect(Collectors.toList());
        }
        else {
                return this.unlockableCursedSpiritTechniques.get().stream()
                .map(CursedTechnique::valueOf)
                .collect(Collectors.toList());
        }
    }

    public List<Trait> getUniqueTraits() {
        return this.uniqueTraitList.get().stream()
                .map(Trait::valueOf)
                .collect(Collectors.toList());
    }

    public Map<Trait, Integer> getTraits(JujutsuType type) {
        Map<Trait, Integer> traitWeights = new HashMap<>();

        List<Trait> traits;
        if (type == JujutsuType.SORCERER) {
                traits = this.sorcererTraitList.get().stream()
                        .map(Trait::valueOf)
                        .collect(Collectors.toList());
        } else {
                traits = this.curseTraitList.get().stream()
                        .map(Trait::valueOf)
                        .collect(Collectors.toList());
        }

        for (Trait t : traits) {
                switch (t) {
                case VESSEL -> traitWeights.put(t, vesselWeight.get());
                case SIX_EYES -> traitWeights.put(t, sixEyesWeight.get());
                case RCT_OUTPUT -> traitWeights.put(t, rctOutputWeight.get());
                case INCARNATED -> traitWeights.put(t, incarnatedWeight.get());
                case DEATH_PAINTING -> traitWeights.put(t, deathPaintingWeight.get());
                case CURSED_WOMB -> traitWeights.put(t, cursedWombWeight.get());
                case PERFECT_BODY -> traitWeights.put(t, perfectBodyWeight.get());
                case PRODIGY -> traitWeights.put(t, prodigyWeight.get());
                }
        }

        return traitWeights;
}
    
}

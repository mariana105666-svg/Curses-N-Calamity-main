package radon.jujutsu_kaisen.capability.data.ten_shadows;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import radon.jujutsu_kaisen.JJKConstants;
import radon.jujutsu_kaisen.ability.IAdditionalAdaptation;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.*;
import radon.jujutsu_kaisen.capability.data.ten_shadows.Adaptation.Type;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.ten_shadows.MahoragaEntity;
import radon.jujutsu_kaisen.entity.ten_shadows.WheelEntity;

import java.io.ObjectInputFilter.Config;
import java.util.*;

public class TenShadowsData implements ITenShadowsData {
    private final Set<ResourceLocation> tamed;
    private final Set<ResourceLocation> dead;
    private final List<ItemStack> shadowInventory;
    private final Map<Adaptation, Integer> adapted;
    private final Map<Adaptation, Integer> adapting;
    private final Map<Adaptation, Integer> adaptationCD = new HashMap<>();

    private TenShadowsMode mode;

    private LivingEntity owner;

    public TenShadowsData() {
        this.mode = TenShadowsMode.SUMMON;
        this.tamed = new HashSet<>();
        this.dead = new HashSet<>();
        this.adapted = new HashMap<>();
        this.adapting = new HashMap<>();
        this.shadowInventory = new ArrayList<>();
    }

    @Override
    public void resetAdaptations() {
        this.adapted.clear();
    }

    private void updateAdaptation() {
        ISorcererData cap = this.owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!cap.hasToggled(JJKAbilities.WHEEL.get()) || (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()) && cap.getExperience() < ConfigHolder.SERVER.requiredExperienceForExperienced.get().floatValue()) ) {
            this.adapting.clear();
            return;
        } else if (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()   ) && cap.getExperience() >= ConfigHolder.SERVER.requiredExperienceForExperienced.get().floatValue()) {
            return;
        }
        
        

        Iterator<Map.Entry<Adaptation, Integer>> iter = this.adapting.entrySet().iterator();
  
        while (iter.hasNext()) {
            Map.Entry<Adaptation, Integer> entry = iter.next();

            int timer = entry.getValue();

            int newtimer = timer+1;
            entry.setValue(newtimer);      
           
            //  if (newtimer >= JJKConstants.REQUIRED_ADAPTATION * 0.2 && timer < JJKConstants.REQUIRED_ADAPTATION * 0.2) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (newtimer >= JJKConstants.REQUIRED_ADAPTATION * 0.4 && timer < JJKConstants.REQUIRED_ADAPTATION * 0.4) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (newtimer >= JJKConstants.REQUIRED_ADAPTATION * 0.6 && timer < JJKConstants.REQUIRED_ADAPTATION * 0.6) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // } else  if (newtimer >= JJKConstants.REQUIRED_ADAPTATION * 0.8 && timer < JJKConstants.REQUIRED_ADAPTATION * 0.8) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // }
            if (++timer >= JJKConstants.REQUIRED_ADAPTATION) {
                iter.remove();
                  this.owner.level().playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.MASTER, 3.0F, 1.0F);

                //this.adapted.add(entry.getKey());

                // if (this.owner instanceof MahoragaEntity mahoraga) {
                //     if (!this.adapted.containsKey(entry.getKey())) {
                //         mahoraga.onAdaptation();
                //     }
                // }
                this.adapted.put(entry.getKey(), this.adapted.getOrDefault(entry.getKey(), 0) + 1);

                WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);

                if (wheel != null) {
                    wheel.spin();
                }
            } else {
                entry.setValue(timer);
            }
        }
        Iterator<Map.Entry<Adaptation, Integer>> cdIter = this.adaptationCD.entrySet().iterator();
        while (cdIter.hasNext()) {
            Map.Entry<Adaptation, Integer> cdEntry = cdIter.next();
            int cd = cdEntry.getValue() - 1;
            if (cd <= 0) {
                cdIter.remove();
            } else {
                cdEntry.setValue(cd);
            }
        }
    }

    @Override
    public void tick(LivingEntity owner) {
        if (this.owner == null) {
            this.owner = owner;
        }

      //  if (!this.owner.level().isClientSide) {
            this.updateAdaptation();
       // }
    }

    @Override
    public void init(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasTamed(Registry<EntityType<?>> registry, EntityType<?> entity) {
        return this.tamed.contains(registry.getKey(entity));
    }

    @Override
    public void tame(Registry<EntityType<?>> registry, EntityType<?> entity) {
        this.tamed.add(registry.getKey(entity));
    }

    @Override
    public void setTamed(Set<ResourceLocation> tamed) {
        this.tamed.clear();
        this.tamed.addAll(tamed);
    }

    @Override
    public Set<ResourceLocation> getTamed() {
        return this.tamed;
    }

    @Override
    public boolean isDead(Registry<EntityType<?>> registry, EntityType<?> entity) {
        return this.dead.contains(registry.getKey(entity));
    }

    @Override
    public Set<ResourceLocation> getDead() {
        return this.dead;
    }

    @Override
    public void setDead(Set<ResourceLocation> dead) {
        this.dead.clear();
        this.dead.addAll(dead);
    }

    @Override
    public void kill(Registry<EntityType<?>> registry, EntityType<?> entity) {
        this.dead.add(registry.getKey(entity));
    }

    @Override
    public void revive(boolean full) {
        this.dead.clear();
        this.adapting.clear();
        this.adapted.clear();
        if (full) {
            this.tamed.clear();
        }
    }

    @Override
    public Map<Adaptation, Integer> getAdapted() {
        return this.adapted;
    }

    @Override
    public void addAdapted(Map<Adaptation, Integer> adaptations) {
        this.adapted.putAll(adaptations);
    }

    @Override
    public Map<Adaptation, Integer> getAdapting() {
        return this.adapting;
    }

    @Override
    public void addAdapting(Map<Adaptation, Integer> adapting) {
        this.adapting.putAll(adapting);
    }

    @Override
    public void addShadowInventory(ItemStack stack) {
        this.shadowInventory.add(stack);
    }

    @Override
    public ItemStack getShadowInventory(int index) {
        return this.shadowInventory.get(index);
    }

    @Override
    public List<ItemStack> getShadowInventory() {
        return this.shadowInventory;
    }

    @Override
    public void removeShadowInventory(int index) {
        this.shadowInventory.remove(index);
    }

    @Override
    public int getAdaptation(Ability ability) {
        for (Map.Entry<Adaptation, Integer> entry : this.adapted.entrySet()) {
            Adaptation adapted = entry.getKey();

            Ability current = adapted.getAbility();

            if (current == null) continue;

            if (current == ability) return entry.getValue();

            Ability.Classification first = current.getClassification();
            Ability.Classification second = ability.getClassification();

            if (first == Ability.Classification.NONE || second == Ability.Classification.NONE) continue;
            if (first == second) return entry.getValue();
        }
        return 0;
    }

    @Override
    public float getAdaptationProgress(DamageSource source) {
        return this.getAdaptationProgress(this.getAdaptation(source));
    }

    @Override
    public float getAdaptationProgress(Adaptation adaptation) {
        return this.adapted.containsKey(adaptation) ? 1.0F : (float) this.adapting.getOrDefault(adaptation, 0) / JJKConstants.REQUIRED_ADAPTATION;
    }

    @Override
    public Adaptation.Type getAdaptationType(DamageSource source) {
        Adaptation adaptation = this.getAdaptation(source);
        return this.getAdaptationType(adaptation);
    }

    @Override
    public Adaptation.Type getAdaptationType(Adaptation adaptation) {
        RegistryAccess registry = this.owner.level().registryAccess();
        Registry<DamageType> types = registry.registryOrThrow(Registries.DAMAGE_TYPE);

        DamageType type = types.get(adaptation.getKey());

        if (type == types.get(DamageTypes.MOB_ATTACK) || type == types.get(DamageTypes.PLAYER_ATTACK)) {
            return Adaptation.Type.COUNTER;
        }
        return Adaptation.Type.DAMAGE;
    }

    // @Override
    // public Map<Adaptation.Type, Float> getAdaptationTypes() {
    //     Map<Adaptation.Type, Float> adaptations = new HashMap<>();

    //     for (Adaptation adaptation : this.adapting.keySet()) {
    //         adaptations.put(this.getAdaptationType(adaptation), this.getAdaptationProgress(adaptation));
    //     }
    //     // for (Adaptation adaptation : this.adapted.keySet()) {
    //     //     adaptations.put(this.getAdaptationType(adaptation), this.getAdaptationProgress(adaptation));
    //     // }
    //     return adaptations;
    // }

    // @Override
    // public boolean isAdaptedTo(DamageSource source) {
    //     Adaptation adaptation = this.getAdaptation(source);
    //     return this.adapted.contains(adaptation);
    // }

    private Adaptation getAdaptation(DamageSource source) {
        RegistryAccess registry = this.owner.level().registryAccess();
        Registry<DamageType> types = registry.registryOrThrow(Registries.DAMAGE_TYPE);
        return new Adaptation(types.getKey(source.type()),
                source instanceof JJKDamageSources.JujutsuDamageSource cap ? cap.getAbility() : null);
    }

     @Override
    public boolean isAdaptedTo(DamageSource source) {
        if (source instanceof JJKDamageSources.JujutsuDamageSource jujutsu) {
            Ability ability = jujutsu.getAbility();

            if (ability != null) {
                return this.isAdaptedTo(ability);
            }
        }
        Adaptation adaptation = this.getAdaptation(source);
        return this.adapted.containsKey(adaptation);
    }

    @Override
    public boolean isAdaptedTo(Ability ability) {
        for (Adaptation adapted : this.adapted.keySet() ) {
            Ability current = adapted.getAbility();

            if (current == null) continue;

            if (current == ability) return true;

            Ability.Classification first = current.getClassification();
            Ability.Classification second = ability.getClassification();

            if (first == Ability.Classification.NONE || second == Ability.Classification.NONE) continue;
            if (first == second) return true;
        }
        return false;
    }

    @Override
    public boolean isAdaptedTo(CursedTechnique technique) {
        for (Ability ability : technique.getAbilities()) {
            if (this.isAdaptedTo(ability)) return true;
        }
        return false;
    }


    @Override
    public void tryAdapt(DamageSource source) {
        RegistryAccess registry = this.owner.level().registryAccess();
        Registry<DamageType> types = registry.registryOrThrow(Registries.DAMAGE_TYPE);
        ISorcererData cap = this.owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
            return;
        }
        ResourceLocation key = types.getKey(source.type());
        Ability ability = source instanceof JJKDamageSources.JujutsuDamageSource jujutsu ? jujutsu.getAbility() : null;
        if (key == null && ability == null) return;

        Adaptation adaptation = new Adaptation(key != null ? key : JJKDamageSources.JUJUTSU.location(), ability);
        if (this.adaptationCD.containsKey(adaptation)) {
            return;
        }
        if (!this.adapting.containsKey(adaptation)) {
            this.adapting.put(adaptation, 0);    
        } else {
            int timer = this.adapting.get(adaptation); 
            //int oldtimer = timer;
               timer += JJKConstants.ADAPTATION_STEP;
             // ISorcererData cap = this.owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            //  if (timer >= JJKConstants.REQUIRED_ADAPTATION  *0.2 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.2) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.4 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.4) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.6 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.6) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.8 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.8) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // }
         
            this.adapting.put(adaptation, timer);
        }
         this.adaptationCD.put(adaptation, JJKConstants.ADAPT_CD_TIME);
    }

    @Override
    public void tryAdapt(Ability ability) {
        Adaptation adaptation = new Adaptation(JJKDamageSources.JUJUTSU.location(), ability);
        ISorcererData cap = this.owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
            return;
        }
        if (this.isAdaptedTo(ability) && (!(ability instanceof IAdditionalAdaptation additional) || (this.adapted != null &&  this.adapted.get(adaptation) != null && this.adapted.get(adaptation) >= additional.getAdditional() + 1)) )
            return;
        if (this.adaptationCD.containsKey(adaptation)) {
            return;
        }
        if (!this.adapting.containsKey(adaptation)) {
            this.adapting.put(adaptation, 0);
        } else {
            int timer = this.adapting.get(adaptation);
        //  int oldtimer = timer;
            timer += JJKConstants.ADAPTATION_STEP;
          
              //ISorcererData cap = this.owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                   
            //   if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.2 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.2) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.4 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.4) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.6 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.6) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // } else  if (timer >= JJKConstants.REQUIRED_ADAPTATION * 0.8 && oldtimer < JJKConstants.REQUIRED_ADAPTATION * 0.8) {
            //     WheelEntity wheel = cap.getSummonByClass(WheelEntity.class);
            //     if (wheel != null) {
            //         wheel.spin();
            //     }  
            // }
      
            this.adapting.put(adaptation, timer);
        }
           this.adaptationCD.put(adaptation, JJKConstants.ADAPT_CD_TIME);
    }

    @Override
    public TenShadowsMode getMode() {
        return this.mode;
    }

    @Override
    public void setMode(TenShadowsMode mode) {
        this.mode = mode;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("mode", this.mode.ordinal());

        ListTag tamedTag = new ListTag();

        for (ResourceLocation key : this.tamed) {
            tamedTag.add(StringTag.valueOf(key.toString()));
        }
        nbt.put("tamed", tamedTag);

        ListTag deadTag = new ListTag();

        for (ResourceLocation key : this.dead) {
            deadTag.add(StringTag.valueOf(key.toString()));
        }
        nbt.put("dead", deadTag);

        ListTag adaptedTag = new ListTag();

        for (Map.Entry<Adaptation, Integer> adaptation : this.adapted.entrySet()) {
            CompoundTag data = new CompoundTag();
            Adaptation key = adaptation.getKey();
            if (key == null || key.getAbility() == null || key.getKey() == null) continue;
            data.put("adaptation", key.serializeNBT());
            data.putInt("stage", adaptation.getValue());
            adaptedTag.add(data);
        }
        nbt.put("adapted", adaptedTag);

        // ListTag adaptingTag = new ListTag();

        // for (Map.Entry<Adaptation, Integer> entry : this.adapting.entrySet()) {
        //     CompoundTag data = new CompoundTag();
        //     data.put("adaptation", entry.getKey().serializeNBT());
        //     data.putInt("stage", entry.getValue());
        //     adaptingTag.add(data);
        // }
        //nbt.put("adapting", adaptingTag);

        ListTag shadowInventoryTag = new ListTag();

        for (ItemStack stack : this.shadowInventory) {
            shadowInventoryTag.add(stack.save(new CompoundTag()));
        }
        nbt.put("shadow_inventory", shadowInventoryTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.mode = TenShadowsMode.values()[nbt.getInt("mode")];

        this.tamed.clear();

        for (Tag key : nbt.getList("tamed", Tag.TAG_STRING)) {
            this.tamed.add(new ResourceLocation(key.getAsString()));
        }

        this.dead.clear();

        for (Tag key : nbt.getList("dead", Tag.TAG_STRING)) {
            this.dead.add(new ResourceLocation(key.getAsString()));
        }

        this.adapted.clear();

        
        for (Tag key : nbt.getList("adapted", Tag.TAG_COMPOUND)) {
            CompoundTag DATA = (CompoundTag) key;
            this.adapted.put(   new Adaptation(DATA.getCompound("adaptation")), DATA.getInt("stage"));
        }

        this.adapted.entrySet().removeIf(entry ->
            entry.getKey() == null ||
            entry.getKey().getAbility() == null && entry.getKey().getKey() == null
        );

        // this.adapting.clear();

        // for (Tag key : nbt.getList("adapting", Tag.TAG_COMPOUND)) {
        //     CompoundTag adaptation = (CompoundTag) key;
        //     this.adapting.put(new Adaptation(adaptation.getCompound("adaptation")), adaptation.getInt("stage"));
        // }

        this.shadowInventory.clear();

        for (Tag key : nbt.getList("shadow_inventory", Tag.TAG_COMPOUND)) {
            this.shadowInventory.add(ItemStack.of((CompoundTag) key));
        }
    }

    
}

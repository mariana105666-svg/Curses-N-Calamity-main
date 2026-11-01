package radon.jujutsu_kaisen.entity.sorcerer;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.AbilityHandler;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.JJKEntityDataSerializers;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.entity.ten_shadows.base.TenShadowsSummon;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.item.base.CursedObjectItem;
import radon.jujutsu_kaisen.util.EntityUtil;

import java.util.*;

public class SukunaEntity extends SorcererEntity {
    private static final EntityDataAccessor<String> DATA_ENTITY = SynchedEntityData.defineId(SukunaEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<CompoundTag>> DATA_PLAYER = SynchedEntityData.defineId(SukunaEntity.class, JJKEntityDataSerializers.OPTIONAL_COMPOUND_TAG.get());
    private static final int TAMING_CHANCE = 4 * 20;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;

    protected int fingers;
    private boolean vessel;

    @Nullable
    private GameType original;

    public static AttributeSupplier.Builder createAttributes() {
        return SorcererEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 140.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 20.0D)
                .add(Attributes.ARMOR, 40.0D);
    }
    public SukunaEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);

        Arrays.fill(this.armorDropChances, 0.0F);
        Arrays.fill(this.handDropChances, 0.0F);
    }

    public SukunaEntity(LivingEntity owner, int fingers, boolean vessel) {
        this(JJKEntities.SUKUNA.get(), owner.level());

        this.setOwner(owner);

        this.fingers = fingers;
        this.vessel = vessel;

        this.entityData.set(DATA_ENTITY, EntityType.getKey(owner.getType()).toString());

        if (owner instanceof Player player) {
            this.entityData.set(DATA_PLAYER, Optional.of(NbtUtils.writeGameProfile(new CompoundTag(), player.getGameProfile())));
        }
    }

     @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (stack.is(JJKItems.SUKUNA_FINGER.get())) {
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);

            int count = stack.getCount();

            stack.shrink(count);
            this.fingers = Math.max(20, this.fingers + count);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }
    
    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    protected boolean targetsSorcerers() {
        return true;
    }

    @Override
    protected boolean targetsShikigami() {
        return true;
    }

     @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.getTarget() != null) return;

        ISorcererData cap = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!cap.hasTechnique(CursedTechnique.TEN_SHADOWS )) {
            return;
        }

        for (Entity entity : cap.getSummons()) {
            if (entity instanceof TenShadowsSummon) return;
        }

        if (this.random.nextInt(TAMING_CHANCE) == 0) {
            Summon<?> mahoraga = JJKAbilities.MAHORAGA.get();

            

            for (Ability ability : CursedTechnique.TEN_SHADOWS.getAbilities()) {
                if (!(ability instanceof Summon<?> summon) || summon.isTamed(this)) continue;
                    AbilityHandler.trigger(this, ability);
                    return;
                }
                if (!mahoraga.isTamed(this)) {
                    AbilityHandler.trigger(this, mahoraga);
                    return;
                }
        }
    }
//  @Override
//     protected void customServerAiStep() {
//         super.customServerAiStep();
//         if (this.getTarget() != null && (this.getTarget() instanceof TenShadowsSummon summon ) && this.getTarget().isDeadOrDying()) {
//             this.setTarget(null);
//         }
//         else if (this.getTarget() != null && !(this.getTarget() instanceof TenShadowsSummon summon ) ) return;
        

//         ISorcererData cap = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

//         if (cap == null) return;


        
    


//         //if (cap.getSummonByClass(TenShadowsSummon.class) != null) return;

//         //  for (Entity entity : cap.getSummons()) {

//         //         if (entity instanceof TenShadowsSummon summon ) return;
//         // }
//         // this.setTarget(null);
//           for (Entity entity : cap.getSummons()) {

//                  if (entity instanceof TenShadowsSummon summon && !summon.isDeadOrDying() && !summon.isTame() ) {
//                     this.setTarget(summon);
//                     return;
//                  } 
//          }
//         this.setTarget(null);
//         if (this.random.nextInt(TAMING_CHANCE) == 0) {
            

//             for (Ability ability : CursedTechnique.TEN_SHADOWS.getAbilities()) {
//                 if (!(ability instanceof Summon<?> summon) || summon.isTamed(this)) continue;

//                 AbilityHandler.trigger(this, ability);
//                 TenShadowsSummon newshadow = cap.getSummonByClass(TenShadowsSummon.class);
//                 if (newshadow != null) {
//                     this.setTarget(newshadow);
//                     return;
//                 }
//             }
//             Summon<?> mahoraga = JJKAbilities.MAHORAGA.get();
//             if (!mahoraga.isTamed(this)) {
//                 AbilityHandler.trigger(this, mahoraga);
//                 TenShadowsSummon newshadow = cap.getSummonByClass(TenShadowsSummon.class);
//                 if (newshadow != null) {
//                     this.setTarget(newshadow);
//                     return;
//                 }
//             }
//         }
//     }

    public EntityType<?> getEntity() {
        return EntityType.byString(this.entityData.get(DATA_ENTITY)).orElseThrow();
    }

    public EntityType<?> getKey() {
        return EntityType.byString(this.entityData.get(DATA_ENTITY)).orElse(null);
    }

    public GameProfile getPlayer() {
        return NbtUtils.readGameProfile(this.entityData.get(DATA_PLAYER).orElseThrow());
    }

    public GameType getOriginal(ServerPlayer player) {
        return this.original == null ? player.server.getDefaultGameType() : this.original;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_ENTITY, "");
        this.entityData.define(DATA_PLAYER, Optional.empty());
    }

    @Override
    public boolean is(@NotNull Entity pEntity) {
        return this == pEntity || pEntity == this.getOwner();
    }

    @Override
    public void tick() {
        LivingEntity owner = this.getOwner();

        if ((!this.level().isClientSide && this.vessel && this.getKey() == EntityType.PLAYER && (owner == null || owner.isRemoved() || !owner.isAlive()))) {
            this.discard();
        } else {
            super.tick();

            if (this.level().isClientSide || this.isRemoved()) return;

            if (owner instanceof ServerPlayer player) {
                if (this.original == null) {
                    this.original = player.gameMode.getGameModeForPlayer();
                }
                player.setGameMode(GameType.SPECTATOR);
                player.setCamera(this);
            } else if (owner != null) {
                owner.discard();
            }
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public float getExperience() {
        float min = SorcererGrade.SPECIAL_GRADE.getRequiredExperience();
        float max = SorcererGrade.SPECIAL_GRADE.getRequiredExperience() * 3.5F;
        return min + (this.fingers * ((max - min) / 20));
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return CursedTechnique.SHRINE;
    }

    @Override
    public List<Ability> getUnlocked() {
        return List.of(JJKAbilities.SIMPLE_DOMAIN.get(), JJKAbilities.MALEVOLENT_SHRINE.get(), JJKAbilities.DOMAIN_AMPLIFICATION.get(),
                JJKAbilities.RCT1.get(),  JJKAbilities.RCT2.get(), JJKAbilities.RCT3.get(), JJKAbilities.QUICKDASH.get(), JJKAbilities.FIRE_ARROW.get() );
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        if (this.ownerUUID != null) {
            pCompound.putUUID("owner", this.ownerUUID);
        }

        pCompound.putInt("fingers", this.fingers);
        pCompound.putBoolean("vessel", this.vessel);

        if (this.original != null) {
            pCompound.putInt("original", this.original.ordinal());
        }

        pCompound.putString("entity",  this.entityData.get(DATA_ENTITY));
        this.entityData.get(DATA_PLAYER).ifPresent(player -> pCompound.put("player", player));
    }

     @Override
    public void init(ISorcererData data) {
        super.init(data);
        data.setAdditionalEnergy( (data.getMaxEnergy() * 0.8F) * (this.fingers / 20) );
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.hasUUID("owner")) {
            this.ownerUUID = pCompound.getUUID("owner");
        }

        this.fingers = pCompound.getInt("fingers");
        this.vessel = pCompound.getBoolean("vessel");

        if (pCompound.contains("original")) {
            this.original = GameType.values()[pCompound.getInt("original")];
        }

        this.entityData.set(DATA_ENTITY, pCompound.getString("entity"));

        if (pCompound.contains("player")) {
            this.entityData.set(DATA_PLAYER, Optional.of(pCompound.getCompound("player")));
        }
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = (LivingEntity) ((ServerLevel) this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public float getStepHeight() { return 5.0F; }


    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        LivingEntity owner = this.getOwner();

        if (owner != null) {
            ISorcererData sorcererSrc = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            ITenShadowsData tenShadowsSrc = owner.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

            ISorcererData sorcererDst = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            ITenShadowsData tenShadowsDst = this.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

            sorcererDst.setTraits(sorcererSrc.getTraits());
            sorcererDst.setAdditional(sorcererSrc.getTechnique());
            tenShadowsDst.setTamed(tenShadowsSrc.getTamed());
            tenShadowsDst.setDead(tenShadowsSrc.getDead());
            sorcererDst.setCopies(sorcererSrc.getCopied());
            sorcererDst.setStolen(sorcererSrc.getStolen());
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        LivingEntity owner = this.getOwner();

        if (owner != null) {
            if (owner instanceof ServerPlayer player) {
                player.setGameMode(this.original == null ? player.server.getDefaultGameType() : this.original);
            }

            //ITenShadowsData src = this.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
            //ITenShadowsData dst = owner.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();

            //dst.setTamed(src.getTamed());
            //dst.setDead(src.getDead());
        }
    }

    @Override
    public void die(@NotNull DamageSource pDamageSource) {
        super.die(pDamageSource);

        LivingEntity owner = this.getOwner();

        if (owner != null) {
            owner.kill();
        }

        if (!(this instanceof HeianSukunaEntity)) {
            if (!this.vessel) {
                EntityUtil.convertTo(this, new HeianSukunaEntity(this.level(), this.fingers), true, false);
            }
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);

        LivingEntity owner = (LivingEntity) this.level().getEntity(pPacket.getData());

        if (owner != null) {
            this.setOwner(owner);
        }
    }
}

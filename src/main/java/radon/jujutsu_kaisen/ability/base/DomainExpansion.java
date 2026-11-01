package radon.jujutsu_kaisen.ability.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JJKConstants;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.AbilityDisplayInfo;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.LivingHitByDomainEvent;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.block.entity.IDomain;
import radon.jujutsu_kaisen.block.entity.IDomainBarrier;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.DomainConfiguration;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.entity.ClosedDomainExpansionEntity;
import radon.jujutsu_kaisen.ability.misc.IncompleteDomain;
import radon.jujutsu_kaisen.entity.GenericOpenDomainEntity;
import radon.jujutsu_kaisen.entity.IncompleteDomainEntity;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.List;

public abstract class DomainExpansion extends Ability implements Ability.IToggled {
    public static final float OPEN_DOMAIN_RADIUS_MULTIPLIER = 1.5F;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    protected boolean isNotDisabledFromDA() {
        return true;
    }
    

    @Override
    public boolean canDisable() {
        return false;
    }



    @Override
    public boolean isDomain() {
        return true;
    }

    public boolean getHitEnvironment() {
        return false;
    }
    
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        boolean enemyDomain = false;
        DomainExpansionEntity selfDomain = null;
        // ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        for (IDomainBarrier domain : VeilHandler.getDomainBarriers((ServerLevel) owner.level(), owner.blockPosition())) {
            for (DomainExpansionEntity d : domain.getClashers() ) {
            if (d.getOwner() == owner) {
                selfDomain = d;
            }
            else if (d.getOwner() != owner) {
                enemyDomain = true;
            }
            }
        }

        if (enemyDomain == true) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (!cap.hasToggled(this)) {
                return true;
            }

            if (cap.hasToggled(this)) {
                return false;
            }
        }

        else if (selfDomain != null && enemyDomain != true) {
            if (target != null) {
                return (selfDomain.distanceTo(target) >= 60.0D);
            }

            if (target == null) {
                return HelperMethods.RANDOM.nextInt(15) == 0;
            }

        }
        return target != null && owner.distanceTo(target) <= 25.0D && owner.getHealth() / owner.getMaxHealth() < 0.9F && HelperMethods.RANDOM.nextInt(4) == 0;
    } 
    // @Override
    // public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
    //     ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

    //     if (cap.hasToggled(this)) {
    //         if (target != null) {
    //             DomainExpansionEntity domain = cap.getSummonByClass(DomainExpansionEntity.class);
    //             return domain != null && domain.isInsideBarrier(target.blockPosition());
    //         }
    //     } else {
    //         if (target == null) return false;

    //         // if (this instanceof DomainExpansion.IClosedDomain closed) {
    //         //      int radius = Math.round(closed.getRadius(owner));

    //         //     if (target.blockPosition().distSqr(owner.blockPosition()) >= (radius - 1) * (radius - 1)) {
    //         //         return false;
    //         //     }
    //         // }


    //         boolean result = cap.getType() == JujutsuType.CURSE || cap.isUnlocked(JJKAbilities.RCT1.get()) ? owner.getHealth() / owner.getMaxHealth() < 0.8F :
    //                 owner.getHealth() / owner.getMaxHealth() < 0.3F || (target.getHealth()/target.getMaxHealth()) > (owner.getHealth()/owner.getMaxHealth()) * 1.5;
    //         boolean isInDomain = false;
    //         for (IDomain ignored : VeilHandler.getDomains((ServerLevel) owner.level(), owner.blockPosition())) {
    //             if (owner.distanceTo(target) < 40.0D) {
    //                 isInDomain = true;
    //             }
    //             break;
    //         }

    //         if (result == true && isInDomain == false && (owner.level() != target.level() || owner.distanceTo(target) > 30.0D) ) return false;

    //         Status status = this.getStatus(owner);

    //         if (result && (status == Status.SUCCESS)) {
    //             if (cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get()) && cap.getExperience() < ConfigHolder.SERVER.requiredExperienceForExperienced.get()) {
    //                 cap.toggle(JJKAbilities.DOMAIN_AMPLIFICATION.get());
    //             }
    //         }
    //         return result;
    //     }
    //     return false;
    // }

    public static float getStrength(LivingEntity owner, boolean instant) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        float size = ((float) cap.getDomainConfig(DomainConfiguration.SIZE) * 0.5F) - 0.15F;
        return ((ConfigHolder.SERVER.maximumDomainSize.get().floatValue() + 0.1F) - size * (instant ? 0.1F : 1.0F));
    }

    
    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.hasSummonOfClass(DomainExpansionEntity.class)) return Status.FAILURE;

        return super.isTriggerable(owner);
    }
    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        if (owner instanceof ServerPlayer player) {
            return true;
        }
        return cap.getBrainDamage() < JJKConstants.MAX_BRAIN_DAMAGE && ((technique != null && technique.getDomain() == this ) || (cap.hasTechnique(CursedTechnique.BRAIN_TRANSPLANT) && cap.getLastStolen() != null && cap.getLastStolen().getDomain() == this  ) ) && (super.isValid(owner));
    }


    @Override
    public Status isStillUsable(LivingEntity owner) {
        if (!owner.level().isClientSide) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!cap.hasSummonOfClass(DomainExpansionEntity.class)) {
            return Status.FAILURE;
        }
    }
        return super.isStillUsable(owner);

    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.DOMAIN;
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        if (owner.level().isClientSide) return;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            DomainExpansionEntity domain;

            IncompleteDomainEntity standing = findIncompleteDomain(owner);

            if (standing != null) {
                domain = new IncompleteDomainEntity(owner, this,
                        new IncompleteDomain.Room(standing.getMin(), standing.getMax()), false);
                owner.level().addFreshEntity(domain);

                cap.addSummon(domain);
                return;
            }

            boolean openBarrier = cap.isOpenDomain();

            if (openBarrier && this instanceof IClosedDomain closedDomain) {
                // A technique with an open form already drawn for it uses that one
                domain = this.createOpenBarrier(owner);

                if (domain == null) {
                    float base = cap.isOpenDomain()
                            ? closedDomain.getSize() * (float) cap.getDomainConfig(DomainConfiguration.OPEN_DOMAIN_SIZE)
                            : closedDomain.getRadius(owner);

                    int radius = Math.min(GenericOpenDomainEntity.MAX_RADIUS,
                            Math.round(base * OPEN_DOMAIN_RADIUS_MULTIPLIER));
                    // getBounds() halves the height, so double it to match the spherical radius
                    int height = radius * 2;
                    domain = new GenericOpenDomainEntity(owner, this, radius, height);
                    owner.level().addFreshEntity(domain);
                }
            } else {
                // Normal path: use the technique's createBarrier
                domain = this.createBarrier(owner);
            }

            cap.addSummon(domain);
    }

    

    @Nullable
    private static IncompleteDomainEntity findIncompleteDomain(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return null;

        for (IDomainBarrier barrier : VeilHandler.getDomainBarriers(level, owner.blockPosition())) {
            for (DomainExpansionEntity current : barrier.getClashers()) {
                if (!(current instanceof IncompleteDomainEntity incomplete)) continue;
                if (incomplete.isRemoved() || !incomplete.isIncomplete()) continue;

                return incomplete;
            }
        }
        return null;
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        if (owner.level().isClientSide) return;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            cap.unsummonByClass(DomainExpansionEntity.class);
            if (owner instanceof ServerPlayer player) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
            }
        
    }

    @Override
    public void run(LivingEntity owner) {
        
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 2.5F;
    }


    public void onHitEntity(DomainExpansionEntity domain, LivingEntity owner, LivingEntity entity, boolean instant) {
         MinecraftForge.EVENT_BUS.post(new LivingHitByDomainEvent(entity, this, owner));
    }

    public abstract void onHitBlock(DomainExpansionEntity domain, LivingEntity owner, BlockPos pos);

    protected abstract DomainExpansionEntity createBarrier(LivingEntity owner);

    @Nullable
    protected DomainExpansionEntity createOpenBarrier(LivingEntity owner) {
        return null;
    }


    @Override
    public boolean shouldLog(LivingEntity owner) {
        return false;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.DOMAIN;
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return ((technique != null && technique.getDomain() == this ) || (cap.hasTechnique(CursedTechnique.BRAIN_TRANSPLANT) && cap.getLastStolen() != null && cap.getLastStolen().getDomain() == this  ) ) && (super.isDisplayed(owner));
    }

    @Override
    public AbilityDisplayInfo getDisplay(LivingEntity owner) {
        Vec2 coordinates = this.getDisplayCoordinates();
        return new AbilityDisplayInfo("domain_expansion", coordinates.x, coordinates.y);
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.INCOMPLETE_DOMAIN.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(3.0F, 0.0F);
    }

    @Override
    public int getPointsCost() {
        return ConfigHolder.SERVER.domainExpansionCost.get();
    }

    public List<Block> getBlocks() { 
        return List.of();
    }

        public List<Block> getFillBlocks() {
            return this.getBlocks();
        }

        public List<Block> getFloorBlocks() {
            return List.of();
        }

        public List<Block> getBottomFloorBlocks() {
            return List.of();
        }

        public List<Block> getDecorationBlocks() {
            return List.of();
        }

        public boolean canPlaceFloor(ClosedDomainExpansionEntity domain, BlockPos pos) {
            return true;
        }

        public boolean canPlaceDecoration(ClosedDomainExpansionEntity domain, BlockPos pos) {
            return true;
        }

        @Nullable
        public ParticleOptions getEnvironmentParticle() {
            return null;
        }

    public interface IClosedDomain {
        default int getSize() {
            return 26;
        }

        default float getRadius(LivingEntity owner) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            return this.getSize() * cap.getDomainSize();
        }
    }

    public interface IOpenDomain {
        int getWidth();

        int getHeight();
    }

     public interface IIncompleteDomain {
        int getWidth();

        int getHeight();
    }
}

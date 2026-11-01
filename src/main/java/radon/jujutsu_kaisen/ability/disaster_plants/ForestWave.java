package radon.jujutsu_kaisen.ability.disaster_plants;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.entity.effect.ForestWaveEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class ForestWave extends Ability implements Ability.IChannelened, Ability.IDurationable {
    private static final int DELAY = 1;
    private static final int SPEED = 5;
    private static final Map<UUID, Vec3> START_POSITIONS = new HashMap<>();
    private static final Map<UUID, Vec3> LAST_POSITIONS = new HashMap<>();
    private static final Map<UUID, Vec3> START_POSITIONSR = new HashMap<>();
    private static final Map<UUID, Vec3> LAST_POSITIONSR = new HashMap<>();


    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || !owner.hasLineOfSight(target)) return false;

        if (JJKAbilities.isChanneling(owner, this)) {
            return HelperMethods.RANDOM.nextInt(5) != 0;
        }
        return HelperMethods.RANDOM.nextInt(1) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    
    
     @Override
    public void onStart(LivingEntity owner) {
        LAST_POSITIONS.remove(owner.getUUID());
        START_POSITIONS.remove(owner.getUUID());
        LAST_POSITIONSR.remove(owner.getUUID());
        START_POSITIONSR.remove(owner.getUUID());
    }

    @Override
public void run(LivingEntity owner) {
    owner.swing(InteractionHand.MAIN_HAND);

    if (owner.level().isClientSide) return;

    int charge = this.getCharge(owner);
    double totalLength = charge;
    double segmentDistance = 0.75D;
    int segmentsPerRun = 3;

    float xRot = owner.getXRot();
    float yRot = owner.getYRot();

    Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner).normalize();

    Vec3 basePos = new Vec3(owner.getX(), owner.getY(), owner.getZ());


    Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();

    double sideOffset = 1.0;
    Vec3 leftOffset = flatLook.yRot((float) Math.toRadians(90)).scale(sideOffset);
    Vec3 rightOffset = flatLook.yRot((float) Math.toRadians(-90)).scale(sideOffset);

    Vec3 startPos = basePos.add(leftOffset);
    Vec3 startPosR = basePos.add(rightOffset);

    START_POSITIONS.put(owner.getUUID(), startPos);
    START_POSITIONSR.put(owner.getUUID(), startPosR);

        
        // Vec3 startPos = START_POSITIONS.computeIfAbsent(
        //     owner.getUUID(),
        //     //+ (owner.getBbHeight() / 2.0F)
        //     id -> new Vec3(owner.getX(), owner.getY() , owner.getZ())
        
        //             .add(look.yRot(90.0F).scale(0.6 * 1.5F))
        // );
    //            .add(look.scale(segmentDistance))
    Vec3 lastPos = LAST_POSITIONS.getOrDefault(owner.getUUID(), startPos);

    for (int i = 0; i < segmentsPerRun; i++) {
        Vec3 nextPos = lastPos.add(look.scale(segmentDistance));

        //double traveledDistance = nextPos.distanceTo(startPos);
        // if (traveledDistance >= totalLength) {
        //     LAST_POSITIONS.remove(owner.getUUID());
        //     START_POSITIONS.remove(owner.getUUID());
        //     break;
        // }
        // float scaleFactor = (float) (1.0 - (traveledDistance / totalLength));
        // scaleFactor = Math.max(scaleFactor, 0.1F);
        // Vec3 segmentScale = new Vec3(scaleFactor, scaleFactor, scaleFactor);
        ForestWaveEntity forest = new ForestWaveEntity(owner, this.getPower(owner));
        forest.moveTo(nextPos.x, nextPos.y, nextPos.z, yRot, xRot);
        forest.setDamage(charge >= DELAY);
        owner.level().addFreshEntity(forest);

        lastPos = nextPos;
    }
    LAST_POSITIONS.put(owner.getUUID(), lastPos);

    //  Vec3 startPosR = START_POSITIONSR.computeIfAbsent(
    //         owner.getUUID(),
    //         id -> new Vec3(owner.getX(), owner.getY(), owner.getZ())
    //                 .add(look.yRot((float) Math.toRadians(-90)).scale(0.6 * 1.5F))
    // );
    Vec3 lastPosR = LAST_POSITIONSR.getOrDefault(owner.getUUID(), startPosR);

    for (int i = 0; i < segmentsPerRun; i++) {
        Vec3 nextPos = lastPosR.add(look.scale(segmentDistance));

        ForestWaveEntity forest = new ForestWaveEntity(owner, this.getPower(owner));
        forest.moveTo(nextPos.x, nextPos.y, nextPos.z, yRot, xRot);
        forest.setDamage(charge >= DELAY);
        owner.level().addFreshEntity(forest);

        lastPosR = nextPos;
    }
    LAST_POSITIONSR.put(owner.getUUID(), lastPosR);
}


//     @Override 
//     public void onStop(LivingEntity owner) {
   
//         int charge = this.getCharge(owner);
//     double totalLength = charge;
//     double segmentDistance = 0.75D;
//     int segmentsPerRun = 10;

//     float xRot = owner.getXRot();
//     float yRot = owner.getYRot();

//     Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner).normalize();

    
//     Vec3 startPos = START_POSITIONS.computeIfAbsent(
//         owner.getUUID(),
//         //+ (owner.getBbHeight() / 2.0F)
//         id -> new Vec3(owner.getX(), owner.getY() , owner.getZ())
    
//                 .add(look.yRot(90.0F).scale(0.5 * 1.5F))
//     );
// //            .add(look.scale(segmentDistance))
//     Vec3 lastPos = LAST_POSITIONS.getOrDefault(owner.getUUID(), startPos);
//      owner.level().playSound(null,BlockPos.containing(lastPos), JJKSounds.FOREST_SPIKES.get(), SoundSource.MASTER, 1.0F, 1.0F);

//     for (int i = 0; i < segmentsPerRun; i++) {
//         Vec3 nextPos = lastPos.add(look.scale(segmentDistance));


//         // if (traveledDistance >= totalLength) {
//         //     LAST_POSITIONS.remove(owner.getUUID());
//         //     START_POSITIONS.remove(owner.getUUID());
//         //     break;
//         // }
//         float scaleFactor = (float) (1.0 - i / 10);
//         scaleFactor = Math.max(scaleFactor, 0.1F);
//         Vec3 segmentScale = new Vec3(scaleFactor, scaleFactor, 1.0);
//         ForestWaveEntity forest = new ForestWaveEntity(owner, this.getPower(owner), segmentScale);
//         forest.moveTo(nextPos.x, nextPos.y, nextPos.z, yRot, xRot);
//         forest.setDamage(charge >= DELAY);
//         owner.level().addFreshEntity(forest);

//         lastPos = nextPos;
//     }
//     }

    @Override
    public float getCost(LivingEntity owner) {
        return 5.0F;
    }

    @Override
    public int getCooldown() {
        return 10 * 20;
    }

    @Override
    public int getDuration() {
        return 4;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public Classification getClassification() {
        return Classification.PLANTS;
    }
}

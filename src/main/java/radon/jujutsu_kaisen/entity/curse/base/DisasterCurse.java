package radon.jujutsu_kaisen.entity.curse.base;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.util.SorcererUtil;

public abstract class DisasterCurse extends CursedSpirit {
    //private static final int RARITY = 12;

    protected DisasterCurse(EntityType<? extends TamableAnimal> pType, Level pLevel) {
        super(pType, pLevel);
    }


    @Override
    public boolean hasMeleeAttack() {
        return true;
    }

    @Override
    public boolean hasArms() {
        return true;
    }

    @Override
    public boolean canJump() {
        return true;
    }


    @Override
    public float getExperience() {
        return SorcererGrade.SPECIAL_GRADE.getRequiredExperience() * 2.0F;
    }

    public static boolean isFarEnoughFromSpawn(LevelAccessor level, BlockPos pos) {
        LevelData data = level.getLevelData();
        BlockPos relative = new BlockPos(data.getXSpawn(), pos.getY(), data.getZSpawn());
        return !relative.closerThan(pos, ConfigHolder.SERVER.minimumSpawnDangerDistance.get());
    }
    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor pLevel, @NotNull MobSpawnType pSpawnReason) {
        if (pSpawnReason == MobSpawnType.NATURAL || pSpawnReason == MobSpawnType.CHUNK_GENERATION) {
            if (ConfigHolder.SERVER.disasterCurseSpawnRate.get() == 0 || !isFarEnoughFromSpawn(pLevel, this.blockPosition() ) ) {
                return false;
            }
            if (this.random.nextInt(Math.max(1,Mth.floor(ConfigHolder.SERVER.disasterCurseSpawnRate.get()  * SorcererUtil.getPower(this.getExperience()) *
                    (this.level().isNight() ? 1.5F : 1.0F)))) != 0) return false;
        }

        if (this.getGrade().ordinal() >= SorcererGrade.GRADE_1.ordinal()) {
            if (!pLevel.getEntitiesOfClass(this.getClass(), AABB.ofSize(this.position(), 128.0D, 32.0D, 128.0D)).isEmpty())
                return false;
        }
        return this.getWalkTargetValue(this.blockPosition(), pLevel) >= 0.0F;
    }
}

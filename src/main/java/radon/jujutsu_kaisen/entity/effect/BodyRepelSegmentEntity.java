package radon.jujutsu_kaisen.entity.effect;


import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.entity.base.JJKPartEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BodyRepelSegmentEntity extends JJKPartEntity<BodyRepelEntity> implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_INDEX = SynchedEntityData.defineId(BodyRepelSegmentEntity.class, EntityDataSerializers.INT);
    public static final ResourceLocation RENDERER = new ResourceLocation(JujutsuKaisen.MOD_ID, "body_repel_segment");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BodyRepelSegmentEntity(BodyRepelEntity parent, int i) {
        super(parent);

        this.setSize(EntityDimensions.fixed(1.25F, 1.1875F));

        this.entityData.set(DATA_INDEX, i);
    }

    @Override
    public void tick() {
        super.tick();

        this.collideWithOthers();
    }

    @Override
    public ResourceLocation getRenderer() {
        return RENDERER;
    }

    private void collideWithOthers() {
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox());

        for (Entity entity : entities) {
            if (entity.isPushable()) {
                //this.collideWithEntity(entity);
            }
        }
    }

    private void collideWithEntity(Entity entity) {
        if (!(entity instanceof BodyRepelEntity)) {
            //entity.push(this);
        }
    }

    public int getIndex() {
        return this.entityData.get(DATA_INDEX);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_INDEX, 0);
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
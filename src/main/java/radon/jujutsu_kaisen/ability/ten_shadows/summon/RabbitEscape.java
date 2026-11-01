package radon.jujutsu_kaisen.ability.ten_shadows.summon;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Summon;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.effect.base.JJKEffectUtil;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.ten_shadows.RabbitEscapeEntity;

import java.util.List;

public class RabbitEscape extends Summon<RabbitEscapeEntity> {
    private boolean triggered;
    public RabbitEscape() {
        super(RabbitEscapeEntity.class);     
        this.triggered = false;   
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return target != null && !target.isDeadOrDying() && owner.tickCount - owner.getLastHurtByMobTimestamp() < 20;
    }

    @Override
    public List<EntityType<?>> getTypes() {
        return List.of(JJKEntities.RABBIT_ESCAPE.get());
    }

    @Override
    public void run(LivingEntity owner) {
       

        super.run(owner);
         
   
       // }
       
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return this.isTamed(owner) ? ActivationType.TOGGLED : ActivationType.INSTANT;
    }

    @Override
    protected RabbitEscapeEntity summon(LivingEntity owner) {
        return new RabbitEscapeEntity(owner, this.isTamed(owner));
    }

    @Override
    public boolean canDie() {
        return true;
    }

    @Override
    public boolean isTenShadows() {
        return true;
    }

    @Override
    protected boolean canTame() {
        return true;
    }

    @Override
    public boolean canDisable() {
        return false;
    }

     @Override
    public int getCooldown() {
        return 30 * 20;
    }

    @Override
    public float getCost(LivingEntity owner) {
        float normalcost = 0.2f;
        return this.isTamed(owner) ? normalcost : 10.0F;
    }


     @Override
public void onEnabled(LivingEntity owner) {
    if (!this.triggered) {
        this.triggered = true;
        JJKEffectUtil.addEffect(owner, new MobEffectInstance(MobEffects.INVISIBILITY, 10 * 20,
                0, false, false, false));
        // MobEffectInstance instance = new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 6 * 20, 0, false, false, false);

        // owner.addEffect(instance);

        // if (!owner.level().isClientSide && owner.level() instanceof ServerLevel serverLevel) {
        //     serverLevel.players().forEach(player -> {
        //         player.connection.send(new ClientboundUpdateMobEffectPacket(owner.getId(), instance));
        //     });
        // }
    }
    super.onEnabled(owner);
}



    @Override
    public void onDisabled(LivingEntity owner) {
        this.triggered = false;
        super.onDisabled(owner);
        
    }

}

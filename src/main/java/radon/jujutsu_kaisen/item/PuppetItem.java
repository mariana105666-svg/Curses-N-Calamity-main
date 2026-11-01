package radon.jujutsu_kaisen.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.PuppetEntity;

public class PuppetItem extends Item {
    public PuppetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();

        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        Player player = pContext.getPlayer();

        if (player == null) return InteractionResult.PASS;

        BlockPos pos = pContext.getClickedPos().relative(pContext.getClickedFace());

        PuppetEntity puppet = JJKEntities.PUPPET.get().create(server);

        if (puppet == null) return InteractionResult.PASS;

        puppet.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot(), 0.0F);
        puppet.finalizeSpawn(server, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null, null);

        server.addFreshEntity(puppet);

        ItemStack stack = pContext.getItemInHand();

        if (!player.getAbilities().instabuild) stack.shrink(1);

        return InteractionResult.CONSUME;
    }
}

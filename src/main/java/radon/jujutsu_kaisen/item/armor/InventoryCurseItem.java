package radon.jujutsu_kaisen.item.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.client.render.item.armor.InventoryCurseRenderer;
import radon.jujutsu_kaisen.entity.sorcerer.TojiFushiguroEntity;
import radon.jujutsu_kaisen.sound.JJKSounds;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class InventoryCurseItem extends ArmorItem implements GeoItem, MenuProvider, ICurioItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final int SIZE = 9;

    public InventoryCurseItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @NotNull
    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return slotContext.entity() instanceof Player ? ICurioItem.super.getDropRule(slotContext, source, lootingLevel, recentlyHit, stack) : ICurio.DropRule.DESTROY;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private InventoryCurseRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null) this.renderer = new InventoryCurseRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(String.format("%s.desc", this.getDescriptionId()));
    }   

    private static void ensureSize(ListTag list) {
        while (list.size() < SIZE) list.add(new CompoundTag());
    }

   private static ItemStack findEquippedCurse(Player player) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (chest.getItem() instanceof InventoryCurseItem) {
        return chest;
    }

    LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);
    if (!optional.isPresent()) return ItemStack.EMPTY;

    ICuriosItemHandler handler = optional.resolve().orElse(null);
    if (handler == null) return ItemStack.EMPTY;

    for (SlotResult result : handler.findCurios(item -> item.getItem() instanceof InventoryCurseItem)) {
        if (!result.stack().isEmpty()) {
            return result.stack();
        }
    }

    return ItemStack.EMPTY;
}

    public static void addItem(ItemStack inventory, int slot, ItemStack stack) {
        CompoundTag nbt = inventory.getOrCreateTag();
        ListTag itemsTag = nbt.getList("items", Tag.TAG_COMPOUND);
        ensureSize(itemsTag);

        itemsTag.set(slot, stack.save(new CompoundTag()));
        nbt.put("items", itemsTag);
    }

      public static void removeItem(ItemStack inventory, int slot) {
        CompoundTag nbt = inventory.getOrCreateTag();
        ListTag itemsTag = nbt.getList("items", Tag.TAG_COMPOUND);
        ensureSize(itemsTag);

        itemsTag.set(slot, new CompoundTag());
        nbt.put("items", itemsTag);
    }


    public static void clear(ItemStack inventory) {
        CompoundTag nbt = inventory.getOrCreateTag();
        ListTag list = new ListTag();
        ensureSize(list);
        nbt.put("items", list);
    }

    public static ItemStack getItem(ItemStack inventory, int slot) {
        CompoundTag nbt = inventory.getOrCreateTag();
        ListTag itemsTag = nbt.getList("items", Tag.TAG_COMPOUND);
        ensureSize(itemsTag);
        return ItemStack.of(itemsTag.getCompound(slot));
    }


    @Nullable
@Override
public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
    ItemStack inventoryItem = findEquippedCurse(player);

    if (inventoryItem.isEmpty()) return null;

    CompoundTag nbt = inventoryItem.getOrCreateTag();
    ListTag itemsTag = nbt.getList("items", Tag.TAG_COMPOUND);
    ensureSize(itemsTag);

    AtomicInteger previousCount = new AtomicInteger(itemsTag.size());

    SimpleContainer container = new SimpleContainer(SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            ListTag tag = new ListTag();
            for (int i = 0; i < getContainerSize(); i++) {
                ItemStack stack = getItem(i);
                tag.add(stack.isEmpty() ? new CompoundTag() : stack.save(new CompoundTag()));
            }

            nbt.put("items", tag);
            inventoryItem.setTag(nbt); 

            int current = tag.size();
            if (current > previousCount.get()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        JJKSounds.SWALLOW.get(), SoundSource.MASTER, 1.0F, 1.0F);
            }
            previousCount.set(current);
        }
    };

    for (int i = 0; i < SIZE; i++) {
        ItemStack stack = ItemStack.of(itemsTag.getCompound(i));
        container.setItem(i, stack);
    }

    return new ChestMenu(MenuType.GENERIC_9x1, id, playerInv, container, 1);
}
}
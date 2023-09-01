package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.utils.PlayerInventoryExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PlayerInventoryGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("player_inventory");
    private ListTag data;
    private List<ItemStack> remainingItems = new ArrayList<>();

    public PlayerInventoryGraveData(Inventory inventory, Collection<ItemStack> drops) {
        Inventory graveInv = new Inventory(inventory.player);
        graveInv.replaceWith(inventory);

        // Compare the inventory with the player drops
        // If an item from the inventory is not in drops,
        // that means that the item should not be put in the grave
        loop:
        for (int slot = 0; slot < graveInv.getContainerSize(); slot++) {
            ItemStack stack = graveInv.getItem(slot);

            Iterator<ItemStack> ite = drops.iterator();
            while (ite.hasNext()) {
                ItemStack drop = ite.next();
                if (ItemStack.matches(stack, drop)) {
                    ite.remove();
                    continue loop;
                }
            }
            graveInv.removeItemNoUpdate(slot);
        }

        for (int slot = 0; slot < graveInv.armor.size(); slot++) {
            ItemStack stack = graveInv.armor.get(slot);
            if (EnchantmentHelper.hasBindingCurse(stack)) {
                if (!PlayerInventoryExtensions.addItemToPlayerInventory(graveInv, -1, stack)) {
                    this.remainingItems.add(stack);
                }
                graveInv.armor.set(slot, ItemStack.EMPTY);
            }
        }

        this.data = graveInv.save(new ListTag());
    }

    public PlayerInventoryGraveData(CompoundTag nbt) {
        this.deserializeNBT(nbt);
    }

    public void addRemaining(Collection<ItemStack> remaining) {
        this.remainingItems.addAll(remaining);
    }

    @Override
    public void restore(Player player, boolean shouldReplace) {
        // If we are not replacing the inventory, simply load a new instance and add all items to the player inventory.
        if (!shouldReplace) {
            Inventory storedInventory = new Inventory(player);
            storedInventory.load(this.data);

            for (int slot = 0; slot < storedInventory.getContainerSize(); slot++) {
                ItemStack item = storedInventory.getItem(slot);
                if (item.isEmpty())
                    continue;

                PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, item);
            }

            for (ItemStack remainingStack : this.remainingItems) {
                PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, remainingStack);
            }
            return;
        }

        Inventory storedInventory = new Inventory(player);
        storedInventory.load(this.data);

        Inventory currentInventory = new Inventory(player);
        currentInventory.replaceWith(player.getInventory());

        player.getInventory().replaceWith(storedInventory);

        for (int slot = 0; slot < currentInventory.getContainerSize(); slot++) {
            ItemStack item = currentInventory.getItem(slot);
            if (item.isEmpty())
                continue;

            PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, item);
        }

        for (ItemStack remainingStack : this.remainingItems) {
            PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, remainingStack);
        }
    }

    @Override
    public CompoundTag write(CompoundTag nbt) {
        nbt.put("Data", this.data);
        if (this.remainingItems != null) {
            nbt.putInt("RemainingSize", this.remainingItems.size());
            NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, this.remainingItems.toArray(new ItemStack[this.remainingItems.size()]));
            nbt.put("Remaining", ContainerHelper.saveAllItems(new CompoundTag(), items));
        }
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        this.data = nbt.getList("Data", Tag.TAG_COMPOUND);
        if (nbt.contains("Remaining")) {
            int size = nbt.getInt("RemainingSize");
            NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(nbt.getCompound("Remaining"), items);
            this.remainingItems.addAll(items);
        }
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}

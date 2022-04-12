package dev.quarris.enigmaticgraves.utils;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerInventoryExtensions {

    public static void tryAddItemToPlayerInvElseDrop(Player player, int slot, ItemStack stack) {
        if (!PlayerInventoryExtensions.addItemToPlayerInventory(player.getInventory(), slot, stack)) {
            ItemEntity itemEntity = player.spawnAtLocation(stack);
            itemEntity.setDeltaMovement(0, 0, 0);
            itemEntity.hasImpulse = true;
        }
    }

    /**
     * Attempts to add item into the inventory, any overflows.
     * First if tries to add the stack to the specified slot in the player's inventory.
     * If that fails to fully insert, it will search for any available slot.
     * Returns {@code false} if it's not possible to place the entire stack in the inventory.
     */
    public static boolean addItemToPlayerInventory(Inventory inventory, int slot, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        // Try to find an empty slot in the inventory
        if (slot == -1) {
            slot = inventory.getFreeSlot();
            // If no empty slots exists, then it was not inserted
            if (slot == -1) {
                return false;
            }
        }
        int leftOver = addResource(inventory, slot, stack);
        stack.setCount(leftOver);

        if (leftOver > 0) {
            addItemToPlayerInventory(inventory, -1, stack);
        }

        return stack.isEmpty();
    }

    private static int addResource(Inventory inventory, int slot, ItemStack stack) {
        ItemStack itemstack = inventory.getItem(slot);

        if (!itemstack.isEmpty() && !(itemstack.sameItem(stack) && ItemStack.tagMatches(itemstack, stack))) {
            return stack.getCount();
        }

        if (itemstack.isEmpty()) {
            itemstack = stack.copy();
            itemstack.setCount(0);
            if (stack.hasTag()) {
                itemstack.setTag(stack.getTag().copy());
            }

            inventory.setItem(slot, itemstack);
        }

        int count = stack.getCount();
        int amountToInsert = count;
        if (count > itemstack.getMaxStackSize() - itemstack.getCount()) {
            amountToInsert = itemstack.getMaxStackSize() - itemstack.getCount();
        }

        if (amountToInsert > inventory.getMaxStackSize() - itemstack.getCount()) {
            amountToInsert = inventory.getMaxStackSize() - itemstack.getCount();
        }

        if (amountToInsert != 0) {
            count = count - amountToInsert;
            itemstack.grow(amountToInsert);
            itemstack.setPopTime(5);
        }
        return count;
    }

}

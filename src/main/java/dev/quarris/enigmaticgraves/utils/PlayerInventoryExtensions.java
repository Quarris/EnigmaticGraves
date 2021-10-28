package dev.quarris.enigmaticgraves.utils;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class PlayerInventoryExtensions {

    public static void tryAddItemToPlayerInvElseDrop(PlayerEntity player, int slot, ItemStack stack) {
        if (!PlayerInventoryExtensions.addItemToPlayerInventory(player.inventory, slot, stack)) {
            ItemEntity itemEntity = player.spawnAtLocation(stack);
            itemEntity.setDeltaMovement(0, 0, 0);
            itemEntity.hasImpulse = true;
        }
    }

    /**
     * Adds the stack to the specified slot in the player's inventory. Returns {@code false} if it's not possible to
     * place the entire stack in the inventory.
     */
    public static boolean addItemToPlayerInventory(PlayerInventory inventory, int slot, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (slot == -1) {
            slot = inventory.getFreeSlot();
        }
        int leftOver = addResource(inventory, slot, stack);
        stack.setCount(leftOver);

        return stack.isEmpty();
    }

    private static int addResource(PlayerInventory inventory, int slot, ItemStack stack) {
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

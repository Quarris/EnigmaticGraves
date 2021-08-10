package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.utils.PlayerInventoryExtensions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PlayerInventoryGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("player_inventory");
    private ListNBT data;
    private List<ItemStack> remainingItems = new ArrayList<>();

    public PlayerInventoryGraveData(PlayerInventory inventory, Collection<ItemStack> drops) {
        PlayerInventory graveInv = new PlayerInventory(inventory.player);
        graveInv.copyInventory(inventory);

        // Compare the inventory with the player drops
        // If an item from the inventory is not in drops,
        // that means that the item should not be put in the grave
        loop:
        for (int slot = 0; slot < graveInv.getSizeInventory(); slot++) {
            ItemStack stack = graveInv.getStackInSlot(slot);

            Iterator<ItemStack> ite = drops.iterator();
            while (ite.hasNext()) {
                ItemStack drop = ite.next();
                if (ItemStack.areItemStacksEqual(stack, drop)) {
                    ite.remove();
                    continue loop;
                }
            }
            graveInv.removeStackFromSlot(slot);
        }

        for (int slot = 0; slot < graveInv.armorInventory.size(); slot++) {
            ItemStack stack = graveInv.armorInventory.get(slot);
            if (EnchantmentHelper.hasBindingCurse(stack)) {
                if (!PlayerInventoryExtensions.addItemToPlayerInventory(graveInv, -1, stack)) {
                    this.remainingItems.add(stack);
                }
                graveInv.armorInventory.set(slot, ItemStack.EMPTY);
            }
        }

        this.data = graveInv.write(new ListNBT());
    }

    public PlayerInventoryGraveData(CompoundNBT nbt) {
        this.deserializeNBT(nbt);
    }

    public void addRemaining(Collection<ItemStack> remaining) {
        this.remainingItems.addAll(remaining);
    }

    @Override
    public void restore(PlayerEntity player) {
        PlayerInventory highPriority = new PlayerInventory(player);
        highPriority.read(this.data);

        PlayerInventory lowPriority = new PlayerInventory(player);
        lowPriority.copyInventory(player.inventory);

        player.inventory.copyInventory(highPriority);

        for (int slot = 0; slot < lowPriority.getSizeInventory(); slot++) {
            ItemStack item = lowPriority.getStackInSlot(slot);
            if (item.isEmpty())
                continue;

            PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, item);
        }

        for (ItemStack remainingStack : this.remainingItems) {
            if (!player.inventory.addItemStackToInventory(remainingStack)) {
                player.entityDropItem(remainingStack);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("Data", this.data);
        if (this.remainingItems != null) {
            nbt.putInt("RemainingSize", this.remainingItems.size());
            NonNullList<ItemStack> items = NonNullList.from(ItemStack.EMPTY, this.remainingItems.toArray(new ItemStack[this.remainingItems.size()]));
            nbt.put("Remaining", ItemStackHelper.saveAllItems(new CompoundNBT(), items));
        }
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.data = nbt.getList("Data", Constants.NBT.TAG_COMPOUND);
        if (nbt.contains("Remaining")) {
            int size = nbt.getInt("RemainingSize");
            NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(nbt.getCompound("Remaining"), items);
            this.remainingItems.addAll(items);
        }
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}

package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.ModRef;
import dev.quarris.enigmaticgraves.PlayerInventoryExtensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PlayerInventoryGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("player_inventory");
    private ListNBT data;

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

        this.data = graveInv.write(new ListNBT());
    }

    public PlayerInventoryGraveData(CompoundNBT nbt) {
        this.deserializeNBT(nbt);
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

            if (!PlayerInventoryExtensions.addItemToPlayerInventory(player.inventory, slot, item)) {
                player.inventory.addItemStackToInventory(item);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("Data", this.data);
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.data = nbt.getList("Data", Constants.NBT.TAG_COMPOUND);
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}

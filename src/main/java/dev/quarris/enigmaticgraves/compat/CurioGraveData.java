package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.ModRef;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CurioGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("curios");
    public final Map<String, NonNullList<ItemStack>> curioStacks = new HashMap<>();
    public final Map<String, NonNullList<ItemStack>> curioCosmeticStacks = new HashMap<>();

    public CurioGraveData(ICuriosItemHandler curios, Collection<ItemStack> drops) {
        for (Map.Entry<String, ICurioStacksHandler> entry : curios.getCurios().entrySet()) {
            String id = entry.getKey();
            ICurioStacksHandler curioItems = entry.getValue();
            NonNullList<ItemStack> curioStacksList = NonNullList.withSize(curioItems.getSlots(), ItemStack.EMPTY);
            NonNullList<ItemStack> curioCosmeticStacksList = NonNullList.withSize(curioItems.getSlots(), ItemStack.EMPTY);
            Iterator<ItemStack> ite = drops.iterator();
            loop:
            while (ite.hasNext()) {
                ItemStack drop = ite.next();
                for (int slot = 0; slot < curioItems.getSlots(); slot++) {
                    ItemStack stack = curioItems.getStacks().getStackInSlot(slot);
                    if (ItemStack.areItemStacksEqual(stack, drop)) {
                        curioStacksList.set(slot, drop);
                        ite.remove();
                        continue loop;
                    }
                }
                for (int slot = 0; slot < curioItems.getSlots(); slot++) {
                    ItemStack stack = curioItems.getCosmeticStacks().getStackInSlot(slot);
                    if (ItemStack.areItemStacksEqual(stack, drop)) {
                        curioCosmeticStacksList.set(slot, drop);
                        ite.remove();
                        continue loop;
                    }
                }
            }
            this.curioStacks.put(id, curioStacksList);
            this.curioCosmeticStacks.put(id, curioCosmeticStacksList);
        }
    }

    public CurioGraveData(CompoundNBT nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(PlayerEntity player) {
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        optional.ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioStacks.entrySet()) {
                ICurioStacksHandler stacks = curios.get(entry.getKey());
                NonNullList<ItemStack> graveItems = entry.getValue();
                for (int slot = 0; slot < graveItems.size(); slot++) {
                    // If the curios slots have shrunk since last time,
                    // then add the grave items to the player inventory instead
                    if (slot >= stacks.getSlots()) {
                        player.inventory.addItemStackToInventory(graveItems.get(slot));
                    }

                    ItemStack old = stacks.getStacks().getStackInSlot(slot);
                    stacks.getStacks().setStackInSlot(slot, graveItems.get(slot));
                    if (!old.isEmpty()) {
                        player.inventory.addItemStackToInventory(old);
                    }
                }
            }
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioCosmeticStacks.entrySet()) {
                ICurioStacksHandler stacks = curios.get(entry.getKey());
                NonNullList<ItemStack> graveItems = entry.getValue();
                for (int slot = 0; slot < graveItems.size(); slot++) {
                    // If the curios slots have shrunk since last time,
                    // then add the grave items to the player inventory instead
                    if (slot >= stacks.getSlots()) {
                        player.inventory.addItemStackToInventory(graveItems.get(slot));
                    }

                    ItemStack old = stacks.getCosmeticStacks().getStackInSlot(slot);
                    stacks.getCosmeticStacks().setStackInSlot(slot, graveItems.get(slot));
                    if (!old.isEmpty()) {
                        player.inventory.addItemStackToInventory(old);
                    }
                }
            }
        });
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT stacksNBT = new ListNBT();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioStacks.entrySet()) {
            CompoundNBT entryNBT = new CompoundNBT();
            entryNBT.putString("ID", entry.getKey());
            entryNBT.putInt("Size", entry.getValue().size());
            entryNBT.put("Stacks", ItemStackHelper.saveAllItems(new CompoundNBT(), entry.getValue()));
            stacksNBT.add(entryNBT);
        }
        nbt.put("Stacks", stacksNBT);

        ListNBT cosmeticStacksNBT = new ListNBT();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioCosmeticStacks.entrySet()) {
            CompoundNBT entryNBT = new CompoundNBT();
            entryNBT.putString("ID", entry.getKey());
            entryNBT.putInt("Size", entry.getValue().size());
            entryNBT.put("Stacks", ItemStackHelper.saveAllItems(new CompoundNBT(), entry.getValue()));
            cosmeticStacksNBT.add(entryNBT);
        }
        nbt.put("CosmeticStacks", cosmeticStacksNBT);

        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT stacksNBT = nbt.getList("Stacks", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : stacksNBT) {
            CompoundNBT entryNBT = (CompoundNBT) inbt;
            String id = entryNBT.getString("ID");
            NonNullList<ItemStack> stacks = NonNullList.withSize(entryNBT.getInt("Size"), ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(entryNBT.getCompound("Stacks"), stacks);
            this.curioStacks.put(id, stacks);
        }

        ListNBT cosmeticStacksNBT = nbt.getList("CosmeticStacks", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : cosmeticStacksNBT) {
            CompoundNBT entryNBT = (CompoundNBT) inbt;
            String id = entryNBT.getString("ID");
            NonNullList<ItemStack> stacks = NonNullList.withSize(entryNBT.getInt("Size"), ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(entryNBT.getCompound("Stacks"), stacks);
            this.curioCosmeticStacks.put(id, stacks);
        }
    }
}

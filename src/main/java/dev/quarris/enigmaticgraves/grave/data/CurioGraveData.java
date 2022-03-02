package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.utils.PlayerInventoryExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;

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
            Set<Integer> stackSlotsChecked = new HashSet<>();
            Set<Integer> cosmeticStacksSlotsChecked = new HashSet<>();

            loop:
            while (ite.hasNext()) {
                ItemStack drop = ite.next();
                for (int slot = 0; slot < curioItems.getSlots(); slot++) {
                    if (stackSlotsChecked.contains(slot))
                        continue;

                    ItemStack stack = curioItems.getStacks().getStackInSlot(slot);
                    if (ItemStack.isSame(stack, drop)) {
                        stackSlotsChecked.add(slot);
                        curioStacksList.set(slot, drop);
                        ite.remove();
                        continue loop;
                    }
                }
                for (int slot = 0; slot < curioItems.getSlots(); slot++) {
                    if (cosmeticStacksSlotsChecked.contains(slot))
                        continue;

                    ItemStack stack = curioItems.getCosmeticStacks().getStackInSlot(slot);
                    if (ItemStack.isSame(stack, drop)) {
                        cosmeticStacksSlotsChecked.add(slot);
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

    public CurioGraveData(CompoundTag nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(Player player) {
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        optional.ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioStacks.entrySet()) {
                ICurioStacksHandler stacks = curios.get(entry.getKey());
                NonNullList<ItemStack> graveItems = entry.getValue();
                for (int slot = 0; slot < graveItems.size(); slot++) {
                    if (graveItems.get(slot).isEmpty()) // Dont replace empty items
                        continue;

                    // If the curios slots have shrunk since last time,
                    // then add the grave items to the player inventory instead
                    if (slot >= stacks.getSlots()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, graveItems.get(slot));
                        return;
                    }

                    ItemStack old = stacks.getStacks().getStackInSlot(slot);
                    stacks.getStacks().setStackInSlot(slot, graveItems.get(slot));
                    if (!old.isEmpty()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, old);
                    }
                }
            }
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioCosmeticStacks.entrySet()) {
                ICurioStacksHandler stacks = curios.get(entry.getKey());
                NonNullList<ItemStack> graveItems = entry.getValue();
                for (int slot = 0; slot < graveItems.size(); slot++) {
                    if (graveItems.get(slot).isEmpty()) // Dont replace empty items
                        continue;
                    // If the curios slots have shrunk since last time,
                    // then add the grave items to the player inventory instead
                    if (slot >= stacks.getSlots()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, graveItems.get(slot));
                        return;
                    }

                    ItemStack old = stacks.getCosmeticStacks().getStackInSlot(slot);
                    stacks.getCosmeticStacks().setStackInSlot(slot, graveItems.get(slot));
                    if (!old.isEmpty()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, slot, old);
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
    public CompoundTag write(CompoundTag nbt) {
        ListTag stacksNBT = new ListTag();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioStacks.entrySet()) {
            CompoundTag entryNBT = new CompoundTag();
            entryNBT.putString("ID", entry.getKey());
            entryNBT.putInt("Size", entry.getValue().size());
            entryNBT.put("Stacks", ContainerHelper.saveAllItems(new CompoundTag(), entry.getValue()));
            stacksNBT.add(entryNBT);
        }
        nbt.put("Stacks", stacksNBT);

        ListTag cosmeticStacksNBT = new ListTag();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : this.curioCosmeticStacks.entrySet()) {
            CompoundTag entryNBT = new CompoundTag();
            entryNBT.putString("ID", entry.getKey());
            entryNBT.putInt("Size", entry.getValue().size());
            entryNBT.put("Stacks", ContainerHelper.saveAllItems(new CompoundTag(), entry.getValue()));
            cosmeticStacksNBT.add(entryNBT);
        }
        nbt.put("CosmeticStacks", cosmeticStacksNBT);

        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        ListTag stacksNBT = nbt.getList("Stacks", Constants.NBT.TAG_COMPOUND);
        for (Tag inbt : stacksNBT) {
            CompoundTag entryNBT = (CompoundTag) inbt;
            String id = entryNBT.getString("ID");
            NonNullList<ItemStack> stacks = NonNullList.withSize(entryNBT.getInt("Size"), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(entryNBT.getCompound("Stacks"), stacks);
            this.curioStacks.put(id, stacks);
        }

        ListTag cosmeticStacksNBT = nbt.getList("CosmeticStacks", Constants.NBT.TAG_COMPOUND);
        for (Tag inbt : cosmeticStacksNBT) {
            CompoundTag entryNBT = (CompoundTag) inbt;
            String id = entryNBT.getString("ID");
            NonNullList<ItemStack> stacks = NonNullList.withSize(entryNBT.getInt("Size"), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(entryNBT.getCompound("Stacks"), stacks);
            this.curioCosmeticStacks.put(id, stacks);
        }
    }
}

package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.utils.PlayerInventoryExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;

import java.util.*;

public class CurioGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("curios");
    private Tag data;

    public CurioGraveData(ICuriosItemHandler curios, Collection<ItemStack> drops) {
        this.data = curios.writeTag();

        // Remove the curios from the drops
        for (Map.Entry<String, ICurioStacksHandler> entry : curios.getCurios().entrySet()) {
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
        }
    }

    public CurioGraveData(CompoundTag nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(Player player, boolean shouldReplace) {
        if (this.data == null) return;

        if (!shouldReplace) {
            // If we are not replacing curios, create new inventory to load the stored data,
            // and add the loaded items into player inventory.
            ICuriosItemHandler newCurios = new CurioInventoryCapability.CurioInventoryWrapper(player);
            newCurios.readTag(this.data);
            newCurios.getCurios().values().forEach(curio -> {
                IDynamicStackHandler stacks = curio.getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, stack);
                    }
                }
            });
            return;
        }

        // Otherwise we want to place existing curios into the player inventory,
        // and then load the stored curios.
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        optional.ifPresent(handler -> {
            handler.getCurios().values().forEach(curio -> {
                IDynamicStackHandler stacks = curio.getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, stack);
                    }
                }
            });
            handler.readTag(this.data);
        });
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public CompoundTag write(CompoundTag nbt) {
        if (this.data != null) {
            nbt.put("Data", this.data);
        }
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        if (nbt.contains("Data")) {
            this.data = nbt.get("Data");
        }
    }

}

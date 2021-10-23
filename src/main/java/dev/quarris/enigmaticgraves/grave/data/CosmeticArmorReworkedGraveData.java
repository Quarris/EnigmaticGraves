package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.*;
import lain.mods.cos.api.*;
import lain.mods.cos.api.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;

import java.util.*;

public class CosmeticArmorReworkedGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("cosmeticarmorreworked");
    public final CAStacksBase caStacksBase = new CAStacksBase();

    public CosmeticArmorReworkedGraveData(CAStacksBase caStacksBase, Collection<ItemStack> drops) {
        this.caStacksBase.deserializeNBT(caStacksBase.serializeNBT());

        Iterator<ItemStack> ite = drops.iterator();
        while(ite.hasNext()){
            ItemStack drop = ite.next();

            for (int slot = 0; slot < 4; slot++){
                ItemStack stack = caStacksBase.getStackInSlot(slot);
                if (ItemStack.areItemStacksEqual(stack, drop)) {
                    ite.remove();
                }
            }
        }
    }

    public CosmeticArmorReworkedGraveData(CompoundNBT nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(PlayerEntity player) {
        CAStacksBase caStacksBase = CosArmorAPI.getCAStacks(player.getUniqueID());

        for (int slot = 0; slot < 4; slot++){
            ItemStack wearing = caStacksBase.getStackInSlot(slot);
            ItemStack looting = this.caStacksBase.getStackInSlot(slot);
            if(!wearing.isEmpty() && !looting.isEmpty()){
                // the player equipped cosmetic armor before claiming the grave,
                // here we de-equip any worn item in favor of what is inside the grave.
                PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, wearing);
            }else if(!wearing.isEmpty()){
                // if the player is wearing something in a cosmetic armor slot that is not in the grave,
                // then it gets put in the serializer so the final line of this function doesn't delete it.
                this.caStacksBase.setStackInSlot(slot, wearing);
            }
        }

        caStacksBase.deserializeNBT(this.caStacksBase.serializeNBT());
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("caStacksBase", caStacksBase.serializeNBT());
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        caStacksBase.deserializeNBT(nbt.getCompound("caStacksBase"));
    }
}

package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.utils.PlayerInventoryExtensions;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Iterator;

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
                if (ItemStack.isSame(stack, drop)) {
                    ite.remove();
                }
            }
        }
    }

    public CosmeticArmorReworkedGraveData(CompoundTag nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(Player player) {
        CAStacksBase lowPrio = CosArmorAPI.getCAStacks(player.getUUID());

        for (int slot = 0; slot < 4; slot++){
            ItemStack lowPrioItem = lowPrio.getStackInSlot(slot);
            ItemStack highPrioItem = this.caStacksBase.getStackInSlot(slot);
            if(!lowPrioItem.isEmpty() && !highPrioItem.isEmpty()){
                // the player equipped cosmetic armor before claiming the grave,
                // here we de-equip any worn item in favor of what is inside the grave.
                PlayerInventoryExtensions.tryAddItemToPlayerInvElseDrop(player, -1, lowPrioItem);
            }else if(!lowPrioItem.isEmpty()){
                // if the player is wearing something in a cosmetic armor slot that is not in the grave,
                // then it gets put in the serializer so the final line of this function doesn't delete it.
                this.caStacksBase.setStackInSlot(slot, lowPrioItem);
            }
        }

        lowPrio.deserializeNBT(this.caStacksBase.serializeNBT());
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public CompoundTag write(CompoundTag nbt) {
        nbt.put("caStacksBase", caStacksBase.serializeNBT());
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        caStacksBase.deserializeNBT(nbt.getCompound("caStacksBase"));
    }
}

package dev.quarris.enigmaticgraves.world;

import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.grave.data.PlayerInventoryGraveData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerGraveEntry implements INBTSerializable<CompoundNBT> {

    public PlayerInventory inventory;
    // Holds the UUID of the grave entity that this entry belongs to
    public UUID graveUUID;
    public List<IGraveData> dataList = new ArrayList<>();

    public PlayerGraveEntry(PlayerInventory inventory) {
        this.inventory = new PlayerInventory(inventory.player);
        this.inventory.copyInventory(inventory);
    }

    public PlayerGraveEntry(CompoundNBT nbt) {
        this.dataList = new ArrayList<>();
        this.deserializeNBT(nbt);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUniqueId("Grave", this.graveUUID);

        ListNBT dataNBT = new ListNBT();
        for (IGraveData data : this.dataList) {
            dataNBT.add(data.serializeNBT());
        }
        nbt.put("Data", dataNBT);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.graveUUID = nbt.getUniqueId("Grave");

        ListNBT dataNBT = nbt.getList("Data", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : dataNBT) {
            CompoundNBT graveNBT = (CompoundNBT) inbt;
            ResourceLocation name = new ResourceLocation(graveNBT.getString("Name"));
            IGraveData data = GraveManager.GRAVE_DATA_SUPPLIERS.get(name).apply(graveNBT);
            this.dataList.add(data);
        }
    }
}

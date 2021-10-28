package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlayerGraveEntry implements INBTSerializable<CompoundNBT> {

    public PlayerInventory inventory;
    // Holds the UUID of the grave entity that this entry belongs to
    public UUID graveUUID;
    public BlockPos gravePos;
    public Date timestamp;
    public List<IGraveData> dataList = new ArrayList<>();

    private boolean restored;

    public PlayerGraveEntry(PlayerInventory inventory) {
        this.inventory = new PlayerInventory(inventory.player);
        this.inventory.replaceWith(inventory);
        this.timestamp = new Date();
    }

    public PlayerGraveEntry(CompoundNBT nbt) {
        this.dataList = new ArrayList<>();
        this.deserializeNBT(nbt);
    }

    public String getEntryName(int id) {
        return String.format("death_%d_%s", id, GraveManager.TIMESTAMP_FORMAT.format(this.timestamp));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("Grave", this.graveUUID);
        nbt.put("Pos", NBTUtil.writeBlockPos(this.gravePos));
        nbt.putLong("Timestamp", this.timestamp.getTime());
        ListNBT dataNBT = new ListNBT();
        for (IGraveData data : this.dataList) {
            dataNBT.add(data.serializeNBT());
        }
        nbt.put("Data", dataNBT);
        nbt.putBoolean("Restored", this.restored);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.graveUUID = nbt.getUUID("Grave");
        this.gravePos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
        this.timestamp = new Date(nbt.getLong("Timestamp"));
        ListNBT dataNBT = nbt.getList("Data", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : dataNBT) {
            CompoundNBT graveNBT = (CompoundNBT) inbt;
            ResourceLocation name = new ResourceLocation(graveNBT.getString("Name"));
            IGraveData data = GraveManager.GRAVE_DATA_SUPPLIERS.get(name).apply(graveNBT);
            this.dataList.add(data);
        }
        this.restored = nbt.getBoolean("Restored");
    }

    public void setRestored() {
        this.restored = true;
    }

    public boolean isRestored() {
        return this.restored;
    }
}

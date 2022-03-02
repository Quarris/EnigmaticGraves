package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlayerGraveEntry implements INBTSerializable<CompoundTag> {

    public Inventory inventory;
    // Holds the UUID of the grave entity that this entry belongs to
    public UUID graveUUID;
    public BlockPos gravePos;
    public Date timestamp;
    public List<IGraveData> dataList = new ArrayList<>();

    private boolean restored;

    public PlayerGraveEntry(Inventory inventory) {
        this.inventory = new Inventory(inventory.player);
        this.inventory.replaceWith(inventory);
        this.timestamp = new Date();
    }

    public PlayerGraveEntry(CompoundTag nbt) {
        this.dataList = new ArrayList<>();
        this.deserializeNBT(nbt);
    }

    public String getEntryName(int id) {
        return String.format("death_%d_%s", id, GraveManager.TIMESTAMP_FORMAT.format(this.timestamp));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Grave", this.graveUUID);
        nbt.put("Pos", NbtUtils.writeBlockPos(this.gravePos));
        nbt.putLong("Timestamp", this.timestamp.getTime());
        ListTag dataNBT = new ListTag();
        for (IGraveData data : this.dataList) {
            dataNBT.add(data.serializeNBT());
        }
        nbt.put("Data", dataNBT);
        nbt.putBoolean("Restored", this.restored);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.graveUUID = nbt.getUUID("Grave");
        this.gravePos = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
        this.timestamp = new Date(nbt.getLong("Timestamp"));
        ListTag dataNBT = nbt.getList("Data", Constants.NBT.TAG_COMPOUND);
        for (Tag inbt : dataNBT) {
            CompoundTag graveNBT = (CompoundTag) inbt;
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

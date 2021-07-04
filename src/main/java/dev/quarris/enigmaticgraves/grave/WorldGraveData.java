package dev.quarris.enigmaticgraves.grave;

import com.google.common.collect.LinkedListMultimap;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class WorldGraveData extends WorldSavedData {

    public static final String NAME = ModRef.res("graves").toString();

    private final Map<UUID, LinkedList<PlayerGraveEntry>> playerGraveEntries = new HashMap<>();
    private final Set<UUID> restoredGraves = new HashSet<>();

    public WorldGraveData() {
        super(NAME);
    }

    public LinkedList<PlayerGraveEntry> getGraveEntriesForPlayer(UUID playerUUID) {
        return this.playerGraveEntries.get(playerUUID);
    }

    public void setGraveRestored(UUID graveUUID) {
        this.restoredGraves.add(graveUUID);
        this.markDirty();
    }

    public void removeGraveRestored(UUID graveUUID) {
        this.restoredGraves.remove(graveUUID);
        this.markDirty();
    }

    public boolean isGraveRestored(UUID graveUUID) {
        return this.restoredGraves.contains(graveUUID);
    }

    public void addGraveEntry(UUID playerUUID, PlayerGraveEntry entry) {
        LinkedList<PlayerGraveEntry> entries = this.playerGraveEntries.computeIfAbsent(playerUUID, k -> new LinkedList<>());
        if (entries.size() >= GraveConfigs.COMMON.graveEntryCount.get()) {
            entries.removeLast();
        }
        entries.addFirst(entry);
        this.markDirty();
    }

    public void clearGraveEntries(PlayerEntity player) {
        this.playerGraveEntries.remove(player.getUniqueID());
        this.markDirty();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT playerGraveEntriesNBT = new ListNBT();
        for (UUID uuid : this.playerGraveEntries.keySet()) {
            CompoundNBT playerGravesNBT = new CompoundNBT();
            playerGravesNBT.putUniqueId("UUID", uuid);
            List<PlayerGraveEntry> entries = this.playerGraveEntries.get(uuid);
            ListNBT entriesNBT = new ListNBT();
            for (PlayerGraveEntry entry : entries) {
                entriesNBT.add(entry.serializeNBT());
            }
            playerGravesNBT.put("Entries", entriesNBT);
            playerGraveEntriesNBT.add(playerGravesNBT);
        }
        compound.put("PlayerGraveEntries", playerGraveEntriesNBT);

        ListNBT restoredGravesNBT = new ListNBT();
        for (UUID restoredGraveUUID : this.restoredGraves) {
            restoredGravesNBT.add(NBTUtil.func_240626_a_(restoredGraveUUID));
        }
        compound.put("RestoredGraves", restoredGravesNBT);
        return compound;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.playerGraveEntries.clear();
        this.restoredGraves.clear();
        ListNBT playerGraveEntriesNBT = nbt.getList("PlayerGraveEntries", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : playerGraveEntriesNBT) {
            CompoundNBT playerGravesNBT = (CompoundNBT) inbt;
            UUID uuid = playerGravesNBT.getUniqueId("UUID");
            ListNBT entriesNBT = playerGravesNBT.getList("Entries", Constants.NBT.TAG_COMPOUND);
            LinkedList<PlayerGraveEntry> entries = this.playerGraveEntries.computeIfAbsent(uuid, k -> new LinkedList<>());
            for (int i = 0; i < entriesNBT.size(); i++) {
                CompoundNBT entryNBT = entriesNBT.getCompound(i);
                PlayerGraveEntry entry = new PlayerGraveEntry(entryNBT);
                entries.addLast(entry);
            }
        }
        ListNBT restoredGravesNBT = nbt.getList("RestoredGraves", Constants.NBT.TAG_COMPOUND);
        for (INBT uuidNBT : restoredGravesNBT) {
            UUID restoredGraveUUID = NBTUtil.readUniqueId(uuidNBT);
            this.restoredGraves.add(restoredGraveUUID);
        }
    }
}

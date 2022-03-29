package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class WorldGraveData extends SavedData {

    public static final String NAME = ModRef.res("graves").toString();

    private final Map<UUID, LinkedList<PlayerGraveEntry>> playerGraveEntries = new HashMap<>();
    private final Set<UUID> restoredGraves = new HashSet<>();

    public WorldGraveData() {
    }

    public LinkedList<PlayerGraveEntry> getGraveEntriesForPlayer(UUID playerUUID) {
        return this.playerGraveEntries.get(playerUUID);
    }

    public void setGraveRestored(UUID graveUUID) {
        this.restoredGraves.add(graveUUID);
        this.setDirty();
    }

    public void removeGraveRestored(UUID graveUUID) {
        this.restoredGraves.remove(graveUUID);
        this.setDirty();
    }

    public boolean isGraveRestored(UUID graveUUID) {
        return this.restoredGraves.contains(graveUUID);
    }

    public void addGraveEntry(Player player, PlayerGraveEntry entry) {
        LinkedList<PlayerGraveEntry> entries = this.playerGraveEntries.computeIfAbsent(player.getUUID(), k -> new LinkedList<>());
        if (entries.size() >= GraveConfigs.COMMON.graveEntryCount.get()) {
            ModRef.LOGGER.debug("Entries reached max values for " + player.getName());
            ModRef.LOGGER.debug("Removing oldest entry");
            entries.removeLast();
        }
        entries.addFirst(entry);
        this.setDirty();
    }

    public void clearGraveEntries(Player player) {
        this.playerGraveEntries.remove(player.getUUID());
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag playerGraveEntriesNBT = new ListTag();
        for (UUID uuid : this.playerGraveEntries.keySet()) {
            CompoundTag playerGravesNBT = new CompoundTag();
            playerGravesNBT.putUUID("UUID", uuid);
            List<PlayerGraveEntry> entries = this.playerGraveEntries.get(uuid);
            ListTag entriesNBT = new ListTag();
            for (PlayerGraveEntry entry : entries) {
                entriesNBT.add(entry.serializeNBT());
            }
            playerGravesNBT.put("Entries", entriesNBT);
            playerGraveEntriesNBT.add(playerGravesNBT);
        }
        compound.put("PlayerGraveEntries", playerGraveEntriesNBT);

        ListTag restoredGravesNBT = new ListTag();
        for (UUID restoredGraveUUID : this.restoredGraves) {
            restoredGravesNBT.add(NbtUtils.createUUID(restoredGraveUUID));
        }
        compound.put("RestoredGraves", restoredGravesNBT);
        return compound;
    }

    public static WorldGraveData load(CompoundTag tag) {
        WorldGraveData data = new WorldGraveData();
        data.loadInternal(tag);
        return data;
    }

    public void loadInternal(CompoundTag nbt) {
        this.playerGraveEntries.clear();
        this.restoredGraves.clear();
        ListTag playerGraveEntriesNBT = nbt.getList("PlayerGraveEntries", Tag.TAG_COMPOUND);
        for (Tag inbt : playerGraveEntriesNBT) {
            CompoundTag playerGravesNBT = (CompoundTag) inbt;
            UUID uuid = playerGravesNBT.getUUID("UUID");
            ListTag entriesNBT = playerGravesNBT.getList("Entries", Tag.TAG_COMPOUND);
            LinkedList<PlayerGraveEntry> entries = this.playerGraveEntries.computeIfAbsent(uuid, k -> new LinkedList<>());
            for (int i = 0; i < entriesNBT.size(); i++) {
                CompoundTag entryNBT = entriesNBT.getCompound(i);
                PlayerGraveEntry entry = new PlayerGraveEntry(entryNBT);
                entries.addLast(entry);
            }
        }
        ListTag restoredGravesNBT = nbt.getList("RestoredGraves", Tag.TAG_COMPOUND);
        for (Tag uuidNBT : restoredGravesNBT) {
            UUID restoredGraveUUID = NbtUtils.loadUUID(uuidNBT);
            this.restoredGraves.add(restoredGraveUUID);
        }
    }
}

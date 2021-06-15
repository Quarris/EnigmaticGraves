package dev.quarris.enigmaticgraves.grave;

import com.google.common.collect.LinkedListMultimap;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldGraveData extends WorldSavedData {

    public static final String NAME = ModRef.res("graves").toString();

    private final LinkedListMultimap<UUID, PlayerGraveEntry> playerGraveEntries = LinkedListMultimap.create();
    private final Set<UUID> restoredGraves = new HashSet<>();

    public WorldGraveData() {
        super(NAME);
    }

    public List<PlayerGraveEntry> getGraveEntriesForPlayer(UUID playerUUID) {
        return this.playerGraveEntries.get(playerUUID);
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
            for (INBT inbt1 : entriesNBT) {
                CompoundNBT entryNBT = (CompoundNBT) inbt1;
                PlayerGraveEntry entry = new PlayerGraveEntry(entryNBT);
                this.playerGraveEntries.put(uuid, entry);
            }
        }
        ListNBT restoredGravesNBT = nbt.getList("RestoredGraves", Constants.NBT.TAG_COMPOUND);
        for (INBT uuidNBT : restoredGravesNBT) {
            UUID restoredGraveUUID = NBTUtil.readUniqueId(uuidNBT);
            this.restoredGraves.add(restoredGraveUUID);
        }
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

    public void addGraveEntry(PlayerEntity player, UUID graveUUID, PlayerGraveEntry entry) {
        this.playerGraveEntries.put(player.getUniqueID(), entry);
        // TODO check the max amount of graves per player
        this.markDirty();
    }

    public void clearGraveEntries(PlayerEntity player) {
        this.playerGraveEntries.removeAll(player.getUniqueID());
        this.markDirty();
    }


}

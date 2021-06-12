package dev.quarris.enigmaticgraves.world;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import dev.quarris.enigmaticgraves.ModRef;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.UUID;

public class WorldGraveData extends WorldSavedData {

    public static final String NAME = ModRef.res("graves").toString();

    private final LinkedListMultimap<UUID, PlayerGraveEntry> playerGraveEntries = LinkedListMultimap.create();

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
        return compound;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.playerGraveEntries.clear();
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
    }

    public void createAndInsertGraveEntry(PlayerEntity player, UUID graveUUID, PlayerGraveEntry entry) {
        this.playerGraveEntries.put(player.getUniqueID(), entry);
        // TODO check the max amount of graves per player
    }
}

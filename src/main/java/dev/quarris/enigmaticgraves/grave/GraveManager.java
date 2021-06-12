package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.config.GraveConfigs.Common.ExperienceHandling;
import dev.quarris.enigmaticgraves.entity.GraveEntity;
import dev.quarris.enigmaticgraves.grave.data.ExperienceGraveData;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.grave.data.PlayerInventoryGraveData;
import dev.quarris.enigmaticgraves.world.PlayerGraveEntry;
import dev.quarris.enigmaticgraves.world.WorldGraveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.function.Function;

public class GraveManager {

    public static final HashMap<ResourceLocation, Function<CompoundNBT, IGraveData>> GRAVE_DATA_SUPPLIERS = new HashMap<>();
    public static final HashMap<UUID, PlayerGraveEntry> LATEST_GRAVE_ENTRY = new HashMap<>();

    public static void init() {
        GRAVE_DATA_SUPPLIERS.put(PlayerInventoryGraveData.NAME, PlayerInventoryGraveData::new);
        GRAVE_DATA_SUPPLIERS.put(ExperienceGraveData.NAME, ExperienceGraveData::new);
    }

    public static WorldGraveData getWorldGraveData(IWorld world) {
        if (world instanceof ServerWorld) {
            MinecraftServer server = ((ServerWorld) world).getServer();
            return server.getWorld(World.OVERWORLD).getSavedData().getOrCreate(WorldGraveData::new, WorldGraveData.NAME);
        }

        return null;
    }

    public static boolean shouldSpawnGrave(PlayerEntity player) {
        return !player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) &&
                !player.isSpectator();

    }

    public static void prepPlayerGrave(PlayerEntity player) {
        if (!shouldSpawnGrave(player))
            return;

        PlayerGraveEntry entry = new PlayerGraveEntry(player.inventory);
        LATEST_GRAVE_ENTRY.put(player.getUniqueID(), entry);
    }

    public static void populatePlayerGrave(PlayerEntity player, Collection<ItemStack> drops) {
        if (!LATEST_GRAVE_ENTRY.containsKey(player.getUniqueID()))
            return;

        PlayerGraveEntry entry = LATEST_GRAVE_ENTRY.get(player.getUniqueID());
        generateGraveDataList(player, entry, drops);
    }

    public static void spawnPlayerGrave(PlayerEntity player) {
        if (!LATEST_GRAVE_ENTRY.containsKey(player.getUniqueID()))
            return;

        WorldGraveData worldData = getWorldGraveData(player.world);
        PlayerGraveEntry entry = LATEST_GRAVE_ENTRY.get(player.getUniqueID());
        GraveEntity grave = GraveEntity.createGrave(player, entry.dataList);
        entry.graveUUID = grave.getUniqueID();
        player.world.addEntity(grave);
        worldData.createAndInsertGraveEntry(player, grave.getUniqueID(), entry);

        LATEST_GRAVE_ENTRY.remove(player.getUniqueID());
    }

    public static void generateGraveDataList(PlayerEntity player, PlayerGraveEntry entry, Collection<ItemStack> drops) {
        List<IGraveData> dataList = new ArrayList<>();
        PlayerInventoryGraveData playerInvData = new PlayerInventoryGraveData(entry.inventory, drops);
        dataList.add(playerInvData);

        ExperienceHandling xpHandling = GraveConfigs.COMMON.experienceGraveHandling.get();
        if (xpHandling != ExperienceHandling.DROP) {
            int xp = 0; // if (xpHandling == ExperienceHandling.REMOVE)
            if (xpHandling == ExperienceHandling.KEEP_VANILLA) {
                // The 'player' param is not used for PlayerEntity, using ourselves to prevent random NPE crashes from possible mixins
                xp = player.getExperiencePoints(player);
                xp = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(player, player, xp);
            } else if (xpHandling == ExperienceHandling.KEEP_ALL) {
                xp += player.experience * player.xpBarCap();
                while (player.experienceLevel > 0) {
                    player.experienceLevel--;
                    xp += player.xpBarCap();
                }
            }

            ExperienceGraveData xpData = new ExperienceGraveData(xp);
            dataList.add(xpData);
        }
        // TODO Add curios grave data

        entry.dataList = dataList;
    }
}

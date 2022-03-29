package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.compat.CosmeticArmorReworkedCompat;
import dev.quarris.enigmaticgraves.compat.CurioCompat;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.config.GraveConfigs.Common.ExperienceHandling;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.grave.data.*;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class GraveManager {

    public static final HashMap<ResourceLocation, Function<CompoundTag, IGraveData>> GRAVE_DATA_SUPPLIERS = new HashMap<>();
    public static PlayerGraveEntry latestGraveEntry;
    public static List<ItemStack> droppedItems;
    public static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static void init() {
        GRAVE_DATA_SUPPLIERS.put(PlayerInventoryGraveData.NAME, PlayerInventoryGraveData::new);
        GRAVE_DATA_SUPPLIERS.put(ExperienceGraveData.NAME, ExperienceGraveData::new);
        if (CompatManager.isCuriosLoaded()) {
            GRAVE_DATA_SUPPLIERS.put(CurioGraveData.NAME, CurioGraveData::new);
        }
        if (CompatManager.isCosmeticArmorReworkedLoaded()) {
            GRAVE_DATA_SUPPLIERS.put(CosmeticArmorReworkedGraveData.NAME, CosmeticArmorReworkedGraveData::new);
        }
    }

    public static WorldGraveData getWorldGraveData(LevelAccessor world) {
        if (world instanceof ServerLevel) {
            MinecraftServer server = ((ServerLevel) world).getServer();
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            return overworld.getDataStorage().computeIfAbsent(WorldGraveData::load, WorldGraveData::new, WorldGraveData.NAME);
        }

        return null;
    }

    public static boolean shouldSpawnGrave(Player player) {
        return !player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator();

    }

    public static void prepPlayerGrave(Player player) {
        if (!shouldSpawnGrave(player)) {
            ModRef.LOGGER.info("Cannot spawn grave. Player is spectator is the KEEP_INVENTORY gamerule is enabled");
            return;
        }

        ModRef.LOGGER.info("Preparing grave for " + player.getName().getString());
        PlayerGraveEntry entry = new PlayerGraveEntry(player.getInventory());
        latestGraveEntry = entry;
    }

    public static void populatePlayerGrave(Player player, Collection<ItemStack> drops) {
        if (latestGraveEntry == null)
            return;

        ModRef.LOGGER.debug("Populating grave for " + player.getName().getString());
        generateGraveDataList(player, latestGraveEntry, drops);
    }

    public static void spawnPlayerGrave(Player player) {
        if (latestGraveEntry == null)
            return;

        WorldGraveData worldData = getWorldGraveData(player.level);
        GraveEntity grave = GraveEntity.createGrave(player, latestGraveEntry.dataList);
        ModRef.LOGGER.debug("Attempting to spawn grave for " + player.getName().getString() + " at " + grave.blockPosition());
        latestGraveEntry.graveUUID = grave.getUUID();
        latestGraveEntry.gravePos = grave.blockPosition();
        if (!player.level.addFreshEntity(grave)) {
            ModRef.LOGGER.warn("Could not spawn grave for " + player.getName().getString());
        } else {
            ModRef.LOGGER.info("Spawned grave for " + player.getName().getString() + " at " + grave.blockPosition());
        }
        worldData.addGraveEntry(player, latestGraveEntry);
        ModRef.LOGGER.info("Added grave entry to player " + player.getName().getString());

        latestGraveEntry = null;
        droppedItems = null;
    }

    public static void generateGraveDataList(Player player, PlayerGraveEntry entry, Collection<ItemStack> drops) {
        List<IGraveData> dataList = new ArrayList<>();
        PlayerInventoryGraveData playerInvData = new PlayerInventoryGraveData(entry.inventory, drops);
        dataList.add(playerInvData);

        ExperienceHandling xpHandling = GraveConfigs.COMMON.experienceGraveHandling.get();
        if (xpHandling != ExperienceHandling.DROP) {
            int xp = 0; // if (xpHandling == ExperienceHandling.REMOVE)
            if (xpHandling == ExperienceHandling.KEEP_VANILLA) {
                // The 'player' param is not used for Player, using ourselves to prevent random NPE crashes from possible mixins
                xp = player.getExperienceReward(player);
                xp = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(player, player, xp);
            } else if (xpHandling == ExperienceHandling.KEEP_ALL) {
                xp += player.experienceProgress * player.getXpNeededForNextLevel();
                while (player.experienceLevel > 0) {
                    player.experienceLevel--;
                    xp += player.getXpNeededForNextLevel();
                }
            }

            ExperienceGraveData xpData = new ExperienceGraveData(xp);
            dataList.add(xpData);
        }

        if (false && CompatManager.isCuriosLoaded()) {
            IGraveData curiosData = CurioCompat.generateCurioGraveData(player, drops);
            if (curiosData != null) {
                dataList.add(curiosData);
            }
        }

        if (CompatManager.isCosmeticArmorReworkedLoaded()) {
            IGraveData cosmeticArmorReworkedsData = CosmeticArmorReworkedCompat.generateCosmeticArmorReworkedGraveData(player, drops);
            if (cosmeticArmorReworkedsData != null) {
                dataList.add(cosmeticArmorReworkedsData);
            }
        }

        playerInvData.addRemaining(drops);
        playerInvData.addRemaining(droppedItems);
        entry.dataList = dataList;
    }

    public static void setGraveRestored(UUID player, GraveEntity grave) {
        LinkedList<PlayerGraveEntry> entries = getWorldGraveData(grave.level).getGraveEntriesForPlayer(player);
        // The entry may not be present after death if the clear command is used before the retrieval of the grave.
        if (entries != null) {
            entries.stream()
                .filter(entry -> entry.graveUUID.equals(grave.getUUID()))
                .findFirst()
                .ifPresent(PlayerGraveEntry::setRestored);
        }
    }

    /**
     * Finds a position to spawn the grave at.
     * @param outPos The block position to place the grave at.
     * @return true to also spawn a block below the grave.
     */
    public static boolean getSpawnPosition(Level world, Vec3 deathPos, BlockPos.MutableBlockPos outPos) {
        GraveConfigs.Common configs = GraveConfigs.COMMON;
        // First, try to find the first non-air block below the death point
        // and return the air block above that.
        for (BlockPos.MutableBlockPos pos = new BlockPos(deathPos.x, Math.round(deathPos.y), deathPos.z).mutable(); pos.getY() > 0; pos = pos.move(Direction.DOWN)) {
            BlockPos belowPos = new BlockPos(pos).below();
            BlockState belowState = world.getBlockState(belowPos);
            if (blocksMovement(belowState)) {
                outPos.set(pos);
                return false;
            }
        }

        // If there are no non-air blocks below the death point,
        // then scan the range around the backup position
        BlockPos pos = new BlockPos(deathPos.x, configs.scanHeight.get(), deathPos.z);
        for (int scan = 0; scan < configs.scanRange.get(); scan++) {
            // First check above the scan
            BlockPos scanPos = new BlockPos(pos).above(scan);
            if (!blocksMovement(world.getBlockState(scanPos.above())) &&
                !blocksMovement(world.getBlockState(scanPos)) &&
                 blocksMovement(world.getBlockState(scanPos.below()))) {

                outPos.set(scanPos);
                return false;
            }

            if (scan > 0) {
                // Else check below the scan
                scanPos = new BlockPos(pos).below(scan);
                if (!blocksMovement(world.getBlockState(scanPos.above())) &&
                    !blocksMovement(world.getBlockState(scanPos)) &&
                     blocksMovement(world.getBlockState(scanPos.below()))) {

                    outPos.set(scanPos);
                    return false;
                }
            }
        }

        // The scan is filled with air
        if (!blocksMovement(world.getBlockState(pos)) && !blocksMovement(world.getBlockState(pos.above()))) {
            outPos.set(pos);
            return true;
        }

        // If no position was selected, drop the grave at the bottom
        outPos.set(deathPos.x, 1, deathPos.z);
        return !world.getBlockState(new BlockPos(deathPos.x, 0, deathPos.z)).getMaterial().blocksMotion();
    }

    private static boolean blocksMovement(BlockState state) {
        return state.getMaterial().blocksMotion();
    }
}

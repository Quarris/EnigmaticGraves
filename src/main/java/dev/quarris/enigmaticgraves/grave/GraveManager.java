package dev.quarris.enigmaticgraves.grave;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.compat.CurioCompat;
import dev.quarris.enigmaticgraves.compat.CosmeticArmorReworkedCompat;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.config.GraveConfigs.Common.ExperienceHandling;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.grave.data.CurioGraveData;
import dev.quarris.enigmaticgraves.grave.data.ExperienceGraveData;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.grave.data.PlayerInventoryGraveData;
import dev.quarris.enigmaticgraves.grave.data.CosmeticArmorReworkedGraveData;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class GraveManager {

    public static final HashMap<ResourceLocation, Function<CompoundNBT, IGraveData>> GRAVE_DATA_SUPPLIERS = new HashMap<>();
    public static final HashMap<UUID, PlayerGraveEntry> LATEST_GRAVE_ENTRY = new HashMap<>();
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

    public static WorldGraveData getWorldGraveData(IWorld world) {
        if (world instanceof ServerWorld) {
            MinecraftServer server = ((ServerWorld) world).getServer();
            ServerWorld overworld = server.getLevel(World.OVERWORLD);
            return overworld.getDataStorage().computeIfAbsent(WorldGraveData::new, WorldGraveData.NAME);
        }

        return null;
    }

    public static boolean shouldSpawnGrave(PlayerEntity player) {
        return !player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator();

    }

    public static void prepPlayerGrave(PlayerEntity player) {
        if (!shouldSpawnGrave(player)) {
            ModRef.LOGGER.info("Cannot spawn grave. Player is spectator is the KEEP_INVENTORY gamerule is enabled");
            return;
        }

        ModRef.LOGGER.info("Preparing grave for " + player.getName().getString());
        PlayerGraveEntry entry = new PlayerGraveEntry(player.inventory);
        LATEST_GRAVE_ENTRY.put(player.getUUID(), entry);
    }

    public static void populatePlayerGrave(PlayerEntity player, Collection<ItemStack> drops) {
        if (!LATEST_GRAVE_ENTRY.containsKey(player.getUUID()))
            return;

        ModRef.LOGGER.debug("Populating grave for " + player.getName().getString());
        PlayerGraveEntry entry = LATEST_GRAVE_ENTRY.get(player.getUUID());
        generateGraveDataList(player, entry, drops);
    }

    public static void spawnPlayerGrave(PlayerEntity player) {
        if (!LATEST_GRAVE_ENTRY.containsKey(player.getUUID()))
            return;

        WorldGraveData worldData = getWorldGraveData(player.level);
        PlayerGraveEntry entry = LATEST_GRAVE_ENTRY.get(player.getUUID());
        GraveEntity grave = GraveEntity.createGrave(player, entry.dataList);
        ModRef.LOGGER.debug("Attempting to spawn grave for " + player.getName().getString() + " at " + grave.blockPosition());
        entry.graveUUID = grave.getUUID();
        entry.gravePos = grave.blockPosition();
        if (!player.level.addFreshEntity(grave)) {
            ModRef.LOGGER.warn("Could not spawn grave for " + player.getName().getString());
        } else {
            ModRef.LOGGER.info("Spawned grave for " + player.getName().getString() + " at " + grave.blockPosition());
        }
        worldData.addGraveEntry(player, entry);
        ModRef.LOGGER.info("Added grave entry to player " + player.getName().getString());

        LATEST_GRAVE_ENTRY.remove(player.getUUID());
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

        if (CompatManager.isCuriosLoaded()) {
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
    public static boolean getSpawnPosition(World world, Vector3d deathPos, BlockPos.Mutable outPos) {
        GraveConfigs.Common configs = GraveConfigs.COMMON;
        // First, try to find the first non-air block below the death point
        // and return the air block above that.
        for (BlockPos.Mutable pos = new BlockPos(deathPos.x, Math.round(deathPos.y), deathPos.z).mutable(); pos.getY() > 0; pos = pos.move(Direction.DOWN)) {
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

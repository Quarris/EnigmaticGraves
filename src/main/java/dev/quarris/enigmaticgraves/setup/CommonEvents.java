package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.command.RestoreGraveCommand;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.PlayerGraveEntry;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedList;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class CommonEvents {

    // Start collecting dropped items from mods at the start of the player death event.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeathFirst(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getEntity().level.isClientSide)
            return;

        GraveManager.droppedItems = new ArrayList<>();
    }

    // Once everything is collected, check to see if someone has cancelled the event, if it was cancelled then the player has not actually died, and we have to ignore any items we have collected.
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlayerDeathLast(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getEntity().level.isClientSide)
            return;

        if (event.isCanceled()) {
            GraveManager.droppedItems = null;
            return;
        }

        Player player = (Player) event.getEntityLiving();
        GraveManager.prepPlayerGrave(player);
    }

    @SubscribeEvent
    public static void spawnGraveFinder(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered())
            return;

        if (!GraveConfigs.COMMON.spawnGraveFinder.get())
            return;

        ItemStack graveFinder = new ItemStack(Registry.GRAVE_FINDER_ITEM.get());
        LinkedList<PlayerGraveEntry> entries = GraveManager.getWorldGraveData(event.getPlayer().level).getGraveEntriesForPlayer(event.getPlayer().getUUID());

        if (entries == null || entries.isEmpty())
            return;

        PlayerGraveEntry latestEntry = entries.getFirst();
        CompoundTag nbt = graveFinder.getOrCreateTag();
        nbt.put("Pos", NbtUtils.writeBlockPos(latestEntry.gravePos));
        nbt.putUUID("GraveUUID", latestEntry.graveUUID);
        event.getPlayer().addItem(graveFinder);
    }

    @SubscribeEvent
    public static void addDroppedItems(EntityJoinWorldEvent event) {
        if (GraveManager.droppedItems != null && event.getEntity() instanceof ItemEntity) {
            GraveManager.droppedItems.add(((ItemEntity) event.getEntity()).getItem());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        RestoreGraveCommand.register(event.getDispatcher());
    }
}

package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.command.RestoreGraveCommand;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.PlayerGraveEntry;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class CommonEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity) || event.getEntity().level.isClientSide)
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
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
        CompoundNBT nbt = graveFinder.getOrCreateTag();
        nbt.put("Pos", NBTUtil.writeBlockPos(latestEntry.gravePos));
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

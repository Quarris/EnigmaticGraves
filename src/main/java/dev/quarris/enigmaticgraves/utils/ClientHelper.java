package dev.quarris.enigmaticgraves.utils;

import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.setup.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClientHelper {

    public static boolean shouldGlowOnClient(GraveEntity grave) {
        Player player = Minecraft.getInstance().player;

        // Try to find the grave item in one of the hands, prioritising the main hand
        ItemStack stack = null;
        if (player.getMainHandItem().getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
            stack = player.getMainHandItem();
        }
        if (stack == null && player.getOffhandItem().getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
            stack = player.getOffhandItem();
        }

        // If we don't have a grave finder is any hand, we do not glow the grave
        if (stack == null)
            return false;

        boolean hasTag = stack.hasTag() && stack.getTag().contains("GraveUUID");
        UUID graveUUID = hasTag ? stack.getTag().getUUID("GraveUUID") : null;

        // Can glow if:
        // In Creative or Spectator,
        // Or we are the Owner, and the grave finder points to this grave
        return player.isCreative() || player.isSpectator() || (player.getUUID().equals(grave.getOwnerUUID()) && grave.getUUID().equals(graveUUID));
    }

}

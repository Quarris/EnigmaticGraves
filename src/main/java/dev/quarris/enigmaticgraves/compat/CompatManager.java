package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ModList;

public class CompatManager {

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static boolean isCuriosLoaded() {
        return isModLoaded("curios");
    }

    public static void cacheModdedHandlers(PlayerEntity player) {
        ModRef.LOGGER.debug("Caching modded handlers for " + player.getName().getString());
        if (CompatManager.isCuriosLoaded()) {
            CurioCompat.cacheCurios(player);
        }
    }

}

package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class CompatManager {

    public static final String CURIOS_ID = "curios";
    public static final String TOP_ID = "theoneprobe";
    public static final String WAILA_ID = "waila";
    public static final String COSMETICARMORREWORKED_ID = "cosmeticarmorreworked";

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static boolean isCuriosLoaded() {
        return isModLoaded(CURIOS_ID);
    }

    public static boolean isWailaLoaded() {
        return isModLoaded(WAILA_ID);
    }

    public static boolean isTOPLoaded() {
        return isModLoaded(TOP_ID);
    }

    public static boolean isCosmeticArmorReworkedLoaded() {
        return isModLoaded(COSMETICARMORREWORKED_ID);
    }


    public static void cacheModdedHandlers(Player player) {
        ModRef.LOGGER.debug("Caching modded handlers for " + player.getName().getString());
        if (CompatManager.isCuriosLoaded()) {
            ModRef.LOGGER.debug("Caching Curios");
            CurioCompat.cacheCurios(player);
        }
        if(CompatManager.isCosmeticArmorReworkedLoaded()){
            ModRef.LOGGER.debug("Caching CosmeticArmor");
            CosmeticArmorReworkedCompat.cacheCosmeticArmorReworkeds(player);
        }
    }

    public static void interModComms() {
        if (isTOPLoaded()) {
            InterModComms.sendTo(TOP_ID, "getTheOneProbe", TOPCompat::new);
        }
    }


}

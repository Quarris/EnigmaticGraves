package dev.quarris.enigmaticgraves.compat;

import net.minecraftforge.fml.ModList;

public class CompatManager {

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static boolean isCuriosLoaded() {
        return isModLoaded("curios");
    }

}

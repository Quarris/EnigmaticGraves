package dev.quarris.enigmaticgraves;

import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.registry.Registry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ModRef.ID)
public class EnigmaticGraves {

    public EnigmaticGraves() {
        Registry.init();
        GraveManager.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GraveConfigs.SPEC);
    }
}

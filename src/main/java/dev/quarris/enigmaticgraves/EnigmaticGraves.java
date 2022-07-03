package dev.quarris.enigmaticgraves;

import dev.quarris.enigmaticgraves.command.GraveEntryType;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.setup.Registry;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ModRef.ID)
public class EnigmaticGraves {

    public EnigmaticGraves() {
        ArgumentTypeInfos.registerByClass(GraveEntryType.class, SingletonArgumentInfo.contextFree(GraveEntryType::new));
        Registry.init();
        GraveManager.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GraveConfigs.SPEC);
    }
}

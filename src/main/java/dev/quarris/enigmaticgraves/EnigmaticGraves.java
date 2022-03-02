package dev.quarris.enigmaticgraves;

import dev.quarris.enigmaticgraves.command.GraveEntryType;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.setup.Registry;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ModRef.ID)
public class EnigmaticGraves {

    public EnigmaticGraves() {
        ArgumentTypes.register("grave_entry", GraveEntryType.class, new EmptyArgumentSerializer<>(GraveEntryType::new));
        Registry.init();
        GraveManager.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GraveConfigs.SPEC);
    }
}

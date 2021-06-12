package dev.quarris.enigmaticgraves;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModRef {

    public static final String ID = "enigmaticgraves";
    public static final Logger LOGGER = LogManager.getLogger(EnigmaticGraves.class);

    public static ResourceLocation res(String res) {
        return new ResourceLocation(ID, res);
    }

}

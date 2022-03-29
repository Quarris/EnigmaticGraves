
package dev.quarris.enigmaticgraves.compat;


import dev.quarris.enigmaticgraves.content.GraveEntity;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.TextStyleClass;

import java.util.function.Function;

public class TOPCompat implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerEntityDisplayOverride((probeMode, iProbeInfo, playerEntity, world, entity, iProbeHitEntityData) -> {
            if (entity instanceof GraveEntity) {
                GraveEntity grave = (GraveEntity) entity;
                iProbeInfo.horizontal().entity(entity)
                    .vertical().text(CompoundText.create().name(grave.getOwnerName()))
                    .text(CompoundText.create().style(TextStyleClass.MODNAME).text("Enigmatic Graves"));
                return true;
            }
            return false;
        });
        return null;
    }
}

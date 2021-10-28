package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.content.*;
import dev.quarris.enigmaticgraves.utils.*;
import journeymap.client.waypoint.*;

import java.util.*;

public class JourneymapCompat {

    public static void restored(GraveEntity grave){
        WaypointStore.INSTANCE.getAll().stream()
        .filter(waypoint -> waypoint.getType() == Waypoint.Type.Death)
        .filter(Waypoint::isInPlayerDimension)
        .filter(waypoint -> {
            double x = grave.getX() - waypoint.getBlockCenteredX();
            double z = grave.getZ() - waypoint.getBlockCenteredZ();

            // filter matching column
            return 1f > x * x + z * z;
        })
        .min(Comparator.comparingDouble(waypoint -> {
            double x = grave.getX() - waypoint.getBlockCenteredX();
            double y = grave.getY() - waypoint.getBlockCenteredY();
            double z = grave.getZ() - waypoint.getBlockCenteredZ();

            // find the closest waypoint
            return x * x + y * y + z * z;
        }))
        .ifPresent(waypoint -> {
            WaypointStore.INSTANCE.remove(waypoint);
            ModRef.LOGGER.info("Removed waypoint \"" + waypoint.getName() + "\" at " + waypoint.getBlockPos());
        });
    }
}

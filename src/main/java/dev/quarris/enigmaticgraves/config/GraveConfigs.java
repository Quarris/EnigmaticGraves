package dev.quarris.enigmaticgraves.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraftforge.common.ForgeConfigSpec.*;

public class GraveConfigs {

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> specPair = new Builder().configure(Common::new);
        SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {

        public EnumValue<ExperienceHandling> experienceGraveHandling;
        public IntValue graveEntryCount;

        public Common(Builder builder) {
            this.experienceGraveHandling = builder.comment(
                    "Defines how the experience should be handled when the player dies.",
                    "DROP: Drops the xp as normal.",
                    "REMOVE: Doesn't drop or restore the xp. Effectively removes it on death.",
                    "KEEP_VANILLA: Stores the same amount of xp that would've been dropped normally into the grave.",
                    "KEEP_ALL: Stores all the players xp in the graves to restore it."
            ).defineEnum("experienceHandling", ExperienceHandling.DROP);

            this.graveEntryCount = builder.comment(
                    "Defines the amount of entries per player that can be stored to retrieve using commands.",
                    "Once the entry count overflows, the oldest entries will be removed."
            ).defineInRange("graveEntryCount", 10, 1, 99);
        }

        public enum ExperienceHandling {
            DROP, REMOVE, KEEP_VANILLA, KEEP_ALL
        }

    }
}

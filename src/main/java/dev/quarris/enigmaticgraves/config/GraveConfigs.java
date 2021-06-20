package dev.quarris.enigmaticgraves.config;

import net.minecraft.block.Blocks;
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

        // Data
        public EnumValue<ExperienceHandling> experienceGraveHandling;
        public IntValue graveEntryCount;

        // Position
        public IntValue scanHeight;    // The y height to start scanning from
        public ConfigValue<String> graveFloorBlock; // Block to spawn below the grave if there is none
        public IntValue scanRange;    // Scan range (+- from start height)

        public Common(Builder builder) {
            builder.comment("How the grave data is handled").push("data");
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
            builder.pop();

            builder.comment("Grave Spawn Positioning").push("position");
            this.scanHeight = builder.comment(
                    "The scanning start position for a valid place to spawn"
            ).defineInRange("scanHeight", 60, 0, 255);
            this.graveFloorBlock = builder.comment(
                    "The block that should spawn below the grave if there is none"
            ).define("floorBlock", Blocks.DIRT.getRegistryName().toString());
            this.scanRange = builder.comment(
                    "The vertical range (up/down) from the initial position to scan for a valid spot to place a grave"
            ).defineInRange("scanRange", 10, 0, 255);
            builder.pop();
        }

        public enum ExperienceHandling {
            DROP, REMOVE, KEEP_VANILLA, KEEP_ALL
        }

    }
}

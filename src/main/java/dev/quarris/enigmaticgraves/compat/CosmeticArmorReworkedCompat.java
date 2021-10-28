package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.grave.data.*;
import lain.mods.cos.api.*;
import lain.mods.cos.api.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;

import java.util.*;

public class CosmeticArmorReworkedCompat{

    public static final Map<UUID, CAStacksBase> CACHED_COSMETICARMORREWORKEDS = new HashMap<>();

    public static void cacheCosmeticArmorReworkeds(PlayerEntity player) {
        try {
            CAStacksBase cached = new CAStacksBase();
            cached.deserializeNBT(CosArmorAPI.getCAStacks(player.getUUID()).serializeNBT());
            CACHED_COSMETICARMORREWORKEDS.put(player.getUUID(), cached);
        } catch (Exception ignored) {}
    }


    public static IGraveData generateCosmeticArmorReworkedGraveData(PlayerEntity player, Collection<ItemStack> drops) {
        if (CACHED_COSMETICARMORREWORKEDS.containsKey(player.getUUID())) {
            IGraveData data = new CosmeticArmorReworkedGraveData(CACHED_COSMETICARMORREWORKEDS.get(player.getUUID()), drops);
            CACHED_COSMETICARMORREWORKEDS.remove(player.getUUID());
            return data;
        }
        return null;
    }
}

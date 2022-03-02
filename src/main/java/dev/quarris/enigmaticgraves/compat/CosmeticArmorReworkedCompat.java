package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.grave.data.CosmeticArmorReworkedGraveData;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticArmorReworkedCompat{

    public static final Map<UUID, CAStacksBase> CACHED_COSMETICARMORREWORKEDS = new HashMap<>();

    public static void cacheCosmeticArmorReworkeds(Player player) {
        try {
            CAStacksBase cached = new CAStacksBase();
            cached.deserializeNBT(CosArmorAPI.getCAStacks(player.getUUID()).serializeNBT());
            CACHED_COSMETICARMORREWORKEDS.put(player.getUUID(), cached);
        } catch (Exception ignored) {}
    }


    public static IGraveData generateCosmeticArmorReworkedGraveData(Player player, Collection<ItemStack> drops) {
        if (CACHED_COSMETICARMORREWORKEDS.containsKey(player.getUUID())) {
            IGraveData data = new CosmeticArmorReworkedGraveData(CACHED_COSMETICARMORREWORKEDS.get(player.getUUID()), drops);
            CACHED_COSMETICARMORREWORKEDS.remove(player.getUUID());
            return data;
        }
        return null;
    }
}

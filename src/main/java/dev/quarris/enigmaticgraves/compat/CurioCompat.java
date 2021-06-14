package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability.CurioInventoryWrapper;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CurioCompat {

    public static final Map<UUID, ICuriosItemHandler> CACHED_CURIOS = new HashMap<>();
    private static final Constructor<CurioInventoryWrapper> CURIO_CTOR = ObfuscationReflectionHelper.findConstructor(CurioInventoryWrapper.class, PlayerEntity.class);

    public static void cacheCurios(PlayerEntity player) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            try {
                ICuriosItemHandler cached = createCurioItemHandler(player);
                CuriosCapability.INVENTORY.readNBT(cached, null, CuriosCapability.INVENTORY.writeNBT(handler, null));
                CACHED_CURIOS.put(player.getUniqueID(), cached);
            } catch (Exception ignored) {}
        });
    }

    public static IGraveData generateCurioGraveData(PlayerEntity player, Collection<ItemStack> drops) {
        if (CACHED_CURIOS.containsKey(player.getUniqueID())) {
            IGraveData data = new CurioGraveData(CACHED_CURIOS.get(player.getUniqueID()), drops);
            CACHED_CURIOS.remove(player.getUniqueID());
            return data;
        }
        return null;
    }

    public static ICuriosItemHandler createCurioItemHandler(PlayerEntity player) {
        try {
            return CURIO_CTOR.newInstance(player);
        } catch (Exception ignored) {}
        return null;
    }
}

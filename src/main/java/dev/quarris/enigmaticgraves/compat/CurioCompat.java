package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.grave.data.CurioGraveData;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability.CurioInventoryWrapper;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CurioCompat {

    public static final Map<UUID, ICuriosItemHandler> CACHED_CURIOS = new HashMap<>();
    private static final Constructor<CurioInventoryWrapper> CURIO_CTOR = ObfuscationReflectionHelper.findConstructor(CurioInventoryWrapper.class, Player.class);

    public static void cacheCurios(Player player) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            try {
                ICuriosItemHandler cached = createCurioItemHandler(player);
                cached.readTag(handler.writeTag());
                CACHED_CURIOS.put(player.getUUID(), cached);
            } catch (Exception ignored) {}
        });
    }

    public static IGraveData generateCurioGraveData(Player player, Collection<ItemStack> drops) {
        if (CACHED_CURIOS.containsKey(player.getUUID())) {
            IGraveData data = new CurioGraveData(CACHED_CURIOS.get(player.getUUID()), drops);
            CACHED_CURIOS.remove(player.getUUID());
            return data;
        }
        return null;
    }

    public static ICuriosItemHandler createCurioItemHandler(Player player) {
        try {
            return CURIO_CTOR.newInstance(player);
        } catch (Exception ignored) {}
        return null;
    }
}

package dev.quarris.enigmaticgraves;

import dev.quarris.enigmaticgraves.grave.GraveManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity) || event.getEntity().world.isRemote)
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        GraveManager.prepPlayerGrave(player);
    }
}

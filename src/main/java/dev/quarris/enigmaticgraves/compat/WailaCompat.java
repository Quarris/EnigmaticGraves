package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.util.ModIdentification;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerEntityComponent(GraveComponentProvider.INSTANCE, GraveEntity.class);
    }

    static class GraveComponentProvider implements IEntityComponentProvider {

        private static final GraveComponentProvider INSTANCE = new GraveComponentProvider();
        private static final ResourceLocation UID = ModRef.res("grave");

        @Override
        public @Nullable IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
            return currentIcon;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig cfg) {
            if (accessor.getEntity() instanceof GraveEntity grave) {
                tooltip.add(Component.literal("RIP: ").append(grave.getOwnerName()));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }


}

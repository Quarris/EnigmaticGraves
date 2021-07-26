package dev.quarris.enigmaticgraves.compat;

import dev.quarris.enigmaticgraves.content.GraveEntity;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {
    @Override
    public void register(IRegistrar reg) {
        GraveComponentProvider componentProvider = new GraveComponentProvider();
        reg.registerComponentProvider(componentProvider, TooltipPosition.HEAD, GraveEntity.class);
        reg.registerComponentProvider(componentProvider, TooltipPosition.TAIL, GraveEntity.class);
    }

    static class GraveComponentProvider implements IEntityComponentProvider {

        @Override
        public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof GraveEntity) {
                GraveEntity grave = (GraveEntity) accessor.getEntity();
                tooltip.add(new StringTextComponent(String.format(
                    Waila.CONFIG.get().getFormatting().getEntityName(),
                    grave.getOwnerName())));
            }
        }

        @Override
        public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
            tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
        }
    }
}

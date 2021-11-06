package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.content.GraveEntityRenderer;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = ModRef.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Setup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registry.GRAVE_ENTITY_TYPE.get(), GraveEntityRenderer::new);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public static void interModComms(InterModEnqueueEvent event) {
        CompatManager.interModComms();
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            event.getGenerator().addProvider(new ItemModelProvider(event.getGenerator(), ModRef.ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    this.singleTexture(Registry.GRAVE_FINDER_ITEM.get().getRegistryName().getPath(), new ResourceLocation("item/generated"), "layer0", new ResourceLocation(ModRef.ID, "grave_finder"));
                }
            });
            event.getGenerator().addProvider(new LanguageProvider(event.getGenerator(), ModRef.ID, "en_us") {
                @Override
                protected void addTranslations() {
                    this.addItem(Registry.GRAVE_FINDER_ITEM, "Grave Finder");
                    this.add("enigmaticgraves.grave.not_found", "No bound grave");
                    this.addEntityType(Registry.GRAVE_ENTITY_TYPE, "Grave");
                    this.add("info.grave.remove_grave", "No Grave Location. Right click me (in creative) on a grave to remove it!");
                    this.add("grave.locate", "The grave is at %s");
                    this.add("info.grave.not_found", "Grave position not found!");
                }
            });
        }
    }

}

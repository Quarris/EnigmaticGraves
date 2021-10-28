package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.content.GraveFinderItem;
import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModRef.ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ModRef.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ModRef.ID);

    public static final RegistryObject<Item> GRAVE_FINDER_ITEM = ITEMS.register("grave_finder", GraveFinderItem::new);
    public static final RegistryObject<EntityType<GraveEntity>> GRAVE_ENTITY_TYPE = ENTITIES.register("grave", () -> EntityType.Builder.<GraveEntity>of(GraveEntity::new, EntityClassification.MISC).sized(14/16f, 14/16f).fireImmune().canSpawnFarFromPlayer().build("grave"));


    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        TILE_ENTITIES.register(bus);
        ENTITIES.register(bus);
    }
}

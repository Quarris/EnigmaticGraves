package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.command.GraveEntryArgument;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import dev.quarris.enigmaticgraves.content.GraveFinderItem;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModRef.ID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModRef.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModRef.ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, ModRef.ID);

    public static final RegistryObject<Item> GRAVE_FINDER_ITEM = ITEMS.register("grave_finder", GraveFinderItem::new);
    public static final RegistryObject<EntityType<GraveEntity>> GRAVE_ENTITY_TYPE = ENTITIES
        .register("grave", () ->
            EntityType.Builder.<GraveEntity>of(GraveEntity::new, MobCategory.MISC)
                .sized(14 / 16f, 14 / 16f)
                .fireImmune()
                .canSpawnFarFromPlayer()
                .build("grave"));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> GRAVE_ENTRY_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("grave_entry", () -> ArgumentTypeInfos.registerByClass(GraveEntryArgument.class, SingletonArgumentInfo.contextFree(GraveEntryArgument::new)));


    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        TILE_ENTITIES.register(bus);
        ENTITIES.register(bus);
        COMMAND_ARGUMENT_TYPES.register(bus);
    }
}

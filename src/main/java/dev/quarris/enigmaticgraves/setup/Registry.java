package dev.quarris.enigmaticgraves.setup;

import dev.quarris.enigmaticgraves.utils.ModRef;
import dev.quarris.enigmaticgraves.content.GraveEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModRef.ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ModRef.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ModRef.ID);

    //public static final RegistryObject<Block> GRAVE_BLOCK = BLOCKS.register("grave", GraveBlock::new);
    public static final RegistryObject<EntityType<GraveEntity>> GRAVE_ENTITY_TYPE = ENTITIES.register("grave", () -> EntityType.Builder.<GraveEntity>create(GraveEntity::new, EntityClassification.MISC).size(0.6f, 1f).func_225435_d().build("grave"));


    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        TILE_ENTITIES.register(bus);
        ENTITIES.register(bus);
    }
}

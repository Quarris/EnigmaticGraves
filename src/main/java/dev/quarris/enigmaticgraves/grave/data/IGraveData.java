package dev.quarris.enigmaticgraves.grave.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

public interface IGraveData extends INBTSerializable<CompoundTag> {

    void restore(Player player);
    ResourceLocation getName();

    CompoundTag write(CompoundTag nbt);
    void read(CompoundTag nbt);

    @Override
    default CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Name", this.getName().toString());
        return this.write(nbt);
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        this.read(nbt);
    }
}

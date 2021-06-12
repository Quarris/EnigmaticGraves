package dev.quarris.enigmaticgraves.grave.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface IGraveData extends INBTSerializable<CompoundNBT> {

    void restore(PlayerEntity player);
    ResourceLocation getName();

    CompoundNBT write(CompoundNBT nbt);
    void read(CompoundNBT nbt);

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Name", this.getName().toString());
        return this.write(nbt);
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {
        this.read(nbt);
    }
}

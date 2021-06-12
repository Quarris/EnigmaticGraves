package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.ModRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class ExperienceGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("experience");

    private int xp;

    public ExperienceGraveData(int xp) {
        this.xp = xp;
    }

    public ExperienceGraveData(CompoundNBT nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(PlayerEntity player) {
        player.giveExperiencePoints(this.xp);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("XP", this.xp);
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.xp = nbt.getInt("XP");
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}

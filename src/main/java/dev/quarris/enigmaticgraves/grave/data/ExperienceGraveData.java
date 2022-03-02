package dev.quarris.enigmaticgraves.grave.data;

import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ExperienceGraveData implements IGraveData {

    public static final ResourceLocation NAME = ModRef.res("experience");

    private int xp;

    public ExperienceGraveData(int xp) {
        this.xp = xp;
    }

    public ExperienceGraveData(CompoundTag nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public void restore(Player player) {
        player.giveExperiencePoints(this.xp);
    }

    @Override
    public CompoundTag write(CompoundTag nbt) {
        nbt.putInt("XP", this.xp);
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt) {
        this.xp = nbt.getInt("XP");
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}

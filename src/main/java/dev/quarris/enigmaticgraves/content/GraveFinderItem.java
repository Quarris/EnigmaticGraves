package dev.quarris.enigmaticgraves.content;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GraveFinderItem extends Item {

    public GraveFinderItem() {
        super(new Item.Properties().maxStackSize(1));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT nbt = stack.getTag();
        if (nbt.contains("Pos")) {
            BlockPos bp = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
            tooltip.add(new StringTextComponent("X: " + bp.getX() + ", Y: " + bp.getY() + ", Z: " + bp.getZ()));
        } else {
            tooltip.add(new TranslationTextComponent("enigmaticgraves.grave.not_found"));
        }
    }
}

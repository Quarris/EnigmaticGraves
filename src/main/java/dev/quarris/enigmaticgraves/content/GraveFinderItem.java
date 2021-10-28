package dev.quarris.enigmaticgraves.content;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class GraveFinderItem extends Item {

    public GraveFinderItem() {
        super(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (worldIn == null)
            return;

        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            tooltip.add(new TranslationTextComponent("info.grave.remove_grave"));
            return;
        }
        if (nbt.contains("Pos")) {
            BlockPos bp = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
            tooltip.add(new StringTextComponent("X: " + bp.getX() + ", Y: " + bp.getY() + ", Z: " + bp.getZ()));
        } else {
            tooltip.add(new TranslationTextComponent("info.grave.not_found"));
        }
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !player.isCreative()) {
            return ActionResult.pass(stack);
        }

        if (!stack.hasTag() || !stack.getTag().contains("Pos")) {
            return ActionResult.pass(stack);
        }

        player.startUsingItem(hand);
        if (level instanceof ServerWorld) {
            BlockPos pos = NBTUtil.readBlockPos(stack.getTag().getCompound("Pos"));
            ITextComponent result = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                .withStyle((style) -> style.withColor(TextFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip"))));
            player.sendMessage(new TranslationTextComponent("grave.locate", result), player.getUUID());
            player.swing(hand, true);

            return ActionResult.success(stack);
        }

        return ActionResult.consume(stack);
    }
}

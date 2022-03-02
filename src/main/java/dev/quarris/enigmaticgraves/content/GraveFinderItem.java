package dev.quarris.enigmaticgraves.content;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GraveFinderItem extends Item {

    public GraveFinderItem() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (worldIn == null)
            return;

        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            tooltip.add(new TranslatableComponent("info.grave.remove_grave"));
            return;
        }
        if (nbt.contains("Pos")) {
            BlockPos bp = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
            tooltip.add(new TextComponent("X: " + bp.getX() + ", Y: " + bp.getY() + ", Z: " + bp.getZ()));
        } else {
            tooltip.add(new TranslatableComponent("info.grave.not_found"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !player.isCreative()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!stack.hasTag() || !stack.getTag().contains("Pos")) {
            return InteractionResultHolder.pass(stack);
        }

        player.startUsingItem(hand);
        if (level instanceof ServerLevel) {
            BlockPos pos = NbtUtils.readBlockPos(stack.getTag().getCompound("Pos"));
            Component result = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                .withStyle((style) -> style.withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip"))));
            player.sendMessage(new TranslatableComponent("grave.locate", result), player.getUUID());
            player.swing(hand, true);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }
}

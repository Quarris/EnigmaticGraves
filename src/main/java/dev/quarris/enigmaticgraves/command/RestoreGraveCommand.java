package dev.quarris.enigmaticgraves.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.PlayerGraveEntry;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class RestoreGraveCommand {

    private static final ITextComponent GRAVE_ALREADY_RESTORED = new StringTextComponent("Warning: That grave has already been restored. Add 'true' at the end of the last command to restore again.");
    private static final ITextComponent SUCCESSFULLY_RESTORED = new StringTextComponent("Successfully restored the grave.");

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmd = dispatcher
                .register(Commands.literal("enigmatic_graves")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("restore")
                                        .then(Commands.argument("entry", new GraveEntryType())
                                                .executes(ctx -> {
                                                    ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
                                                    PlayerGraveEntry entry = GraveEntryType.getEntry(player.getUniqueID(), ctx, "entry");
                                                    if (!tryRestoreGrave(entry, player, false)) {
                                                        ctx.getSource().sendFeedback(GRAVE_ALREADY_RESTORED, true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFeedback(SUCCESSFULLY_RESTORED, true);
                                                    return 0;
                                                })
                                                .then(Commands.argument("forced", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            boolean forced = BoolArgumentType.getBool(ctx, "forced");
                                                            ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
                                                            PlayerGraveEntry entry = GraveEntryType.getEntry(player.getUniqueID(), ctx, "entry");
                                                            if (!tryRestoreGrave(entry, player, forced)) {
                                                                ctx.getSource().sendFeedback(GRAVE_ALREADY_RESTORED, true);
                                                                return 1;
                                                            }
                                                            ctx.getSource().sendFeedback(SUCCESSFULLY_RESTORED, true);
                                                            return 0;
                                                        }))))
                                .then(Commands.literal("clear")
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
                                            GraveManager.getWorldGraveData(player.world).clearGraveEntries(player);
                                            return 0;
                                        }))));


        dispatcher.register(Commands.literal("graves").redirect(cmd));
    }

    private static boolean tryRestoreGrave(PlayerGraveEntry graveEntry, PlayerEntity player, boolean forced) {
        // if !forced then check if the grave has already been recovered in world
        // if it has, then return false, else restore the graves and return true
        if (!forced && graveEntry.isRestored()) {
            return false;
        }
        for (IGraveData graveData : graveEntry.dataList) {
            graveData.restore(player);
        }
        if (!graveEntry.isRestored()) {
            GraveManager.getWorldGraveData(player.world).setGraveRestored(graveEntry.graveUUID);
        }
        return true;
    }
}

package dev.quarris.enigmaticgraves.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
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
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedList;
import java.util.List;

public class RestoreGraveCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_ENTRIES = (ctx, builder) -> {
        try {
            ServerPlayerEntity player = ctx.getSource().asPlayer();
            LinkedList<PlayerGraveEntry> entries = GraveManager.getWorldGraveData(player.world).getGraveEntriesForPlayer(player.getUniqueID());
            if (entries == null) {
                return Suggestions.empty();
            }
            for (int i = 0; i < entries.size(); i++) {
                PlayerGraveEntry entry = entries.get(i);
                builder.suggest(entry.getEntryName(i));
            }
        } catch (CommandSyntaxException e) { }
        return builder.buildFuture();
    };

    private static final ITextComponent GRAVE_ALREADY_RESTORED = new StringTextComponent("Warning: That grave has already been restored. Add 'true' at the end of the last command to restore again.");
    private static final ITextComponent SUCCESSFULLY_RESTORED = new StringTextComponent("Successfully restored the grave.");
    private static final ITextComponent HELP = new StringTextComponent(TextFormatting.RED +
        "Usage:\n" +
        "/enigmatic_graves <player> list\n" +
        "/enigmatic_graves <player> clear\n" +
        "/enigmatic_graves <player> restore [death_<id> [forced]]"
    );


    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmd = dispatcher
            .register(Commands.literal("enigmatic_graves")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(HELP, false);
                    return 0;
                })
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.literal("list").executes(ctx -> {
                        ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
                        List<PlayerGraveEntry> entries = GraveManager.getWorldGraveData(ctx.getSource().getWorld()).getGraveEntriesForPlayer(player.getUniqueID());
                        if (entries == null) {
                            ctx.getSource().sendFeedback(new StringTextComponent("The player has no deaths."), false);
                            return 0;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < entries.size(); i++) {
                            sb.append(entries.get(i).getEntryName(i));
                            if (i < entries.size() - 1) {
                                sb.append('\n');
                            }
                        }
                        ctx.getSource().sendFeedback(new StringTextComponent(sb.toString()), false);
                        return 0;
                    }))
                    .then(Commands.literal("restore")
                        .executes(ctx -> restoreGrave(ctx, false, false))
                        .then(Commands.argument("forced", BoolArgumentType.bool())
                            .executes(ctx -> restoreGrave(ctx, false, BoolArgumentType.getBool(ctx, "forced"))))
                        .then(Commands.argument("entry", new GraveEntryType())
                            .suggests(SUGGEST_ENTRIES)
                            .executes(ctx -> restoreGrave(ctx, true, false))
                            .then(Commands.argument("forced", BoolArgumentType.bool())
                                .executes(ctx -> restoreGrave(ctx, true, BoolArgumentType.getBool(ctx, "forced"))))))
                    .then(Commands.literal("clear")
                        .executes(ctx -> {
                            ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
                            int count = GraveManager.getWorldGraveData(player.world).getGraveEntriesForPlayer(player.getUniqueID()).size();
                            GraveManager.getWorldGraveData(player.world).clearGraveEntries(player);
                            ctx.getSource().sendFeedback(new StringTextComponent("Cleared " + count + " entries."), true);
                            return 0;
                        }))));


        dispatcher.register(Commands.literal("graves").requires(source -> source.hasPermissionLevel(2))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(HELP, false);
                return 0;
            }).redirect(cmd));
    }

    private static int restoreGrave(CommandContext<CommandSource> ctx, boolean useArg, boolean forced) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgument.getPlayer(ctx, "target");
        PlayerGraveEntry entry;
        if (useArg) {
            entry = GraveEntryType.getEntry(player.getUniqueID(), ctx, "entry");
        } else {
            entry = GraveManager.getWorldGraveData(ctx.getSource().getWorld())
                .getGraveEntriesForPlayer(player.getUniqueID()).getFirst();
        }
        if (!tryRestoreGrave(entry, player, forced)) {
            ctx.getSource().sendFeedback(GRAVE_ALREADY_RESTORED, true);
            return 1;
        }
        ctx.getSource().sendFeedback(SUCCESSFULLY_RESTORED, true);
        return 0;
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

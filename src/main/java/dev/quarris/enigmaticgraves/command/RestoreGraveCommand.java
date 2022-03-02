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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

public class RestoreGraveCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ENTRIES = (ctx, builder) -> {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            LinkedList<PlayerGraveEntry> entries = GraveManager.getWorldGraveData(player.level).getGraveEntriesForPlayer(player.getUUID());
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

    private static final Component GRAVE_ALREADY_RESTORED = new TextComponent("Warning: That grave has already been restored. Add 'true' at the end of the last command to restore again.");
    private static final Component SUCCESSFULLY_RESTORED = new TextComponent("Successfully restored the grave.");
    private static final Component HELP = new TextComponent(ChatFormatting.RED +
        "Usage:\n" +
        "/enigmatic_graves <player> list\n" +
        "/enigmatic_graves <player> clear\n" +
        "/enigmatic_graves <player> restore [death_<id> [forced]]"
    );


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmd = dispatcher
            .register(Commands.literal("enigmatic_graves")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(HELP, false);
                    return 0;
                })
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.literal("list").executes(ctx -> {
                        ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                        List<PlayerGraveEntry> entries = GraveManager.getWorldGraveData(ctx.getSource().getLevel()).getGraveEntriesForPlayer(player.getUUID());
                        if (entries == null) {
                            ctx.getSource().sendSuccess(new TextComponent("The player has no deaths."), false);
                            return 0;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < entries.size(); i++) {
                            sb.append(entries.get(i).getEntryName(i));
                            if (i < entries.size() - 1) {
                                sb.append('\n');
                            }
                        }
                        ctx.getSource().sendSuccess(new TextComponent(sb.toString()), false);
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
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                            int count = GraveManager.getWorldGraveData(player.level).getGraveEntriesForPlayer(player.getUUID()).size();
                            GraveManager.getWorldGraveData(player.level).clearGraveEntries(player);
                            ctx.getSource().sendSuccess(new TextComponent("Cleared " + count + " entries."), true);
                            return 0;
                        }))));


        dispatcher.register(Commands.literal("graves").requires(source -> source.hasPermission(2))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(HELP, false);
                return 0;
            }).redirect(cmd));
    }

    private static int restoreGrave(CommandContext<CommandSourceStack> ctx, boolean useArg, boolean forced) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
        PlayerGraveEntry entry;
        if (useArg) {
            entry = GraveEntryType.getEntry(player.getUUID(), ctx, "entry");
        } else {
            entry = GraveManager.getWorldGraveData(ctx.getSource().getLevel())
                .getGraveEntriesForPlayer(player.getUUID()).getFirst();
        }
        if (!tryRestoreGrave(entry, player, forced)) {
            ctx.getSource().sendSuccess(GRAVE_ALREADY_RESTORED, true);
            return 1;
        }
        ctx.getSource().sendSuccess(SUCCESSFULLY_RESTORED, true);
        return 0;
    }

    private static boolean tryRestoreGrave(PlayerGraveEntry graveEntry, Player player, boolean forced) {
        // if !forced then check if the grave has already been recovered in world
        // if it has, then return false, else restore the graves and return true
        if (!forced && graveEntry.isRestored()) {
            return false;
        }
        for (IGraveData graveData : graveEntry.dataList) {
            graveData.restore(player);
        }
        if (!graveEntry.isRestored()) {
            GraveManager.getWorldGraveData(player.level).setGraveRestored(graveEntry.graveUUID);
        }
        return true;
    }
}

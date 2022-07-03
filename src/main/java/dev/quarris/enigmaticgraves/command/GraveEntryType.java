package dev.quarris.enigmaticgraves.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.PlayerGraveEntry;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraveEntryType implements ArgumentType<Integer> {

    private static final Pattern PATTERN = Pattern.compile("death_(\\d){1,3}.*");

    public static PlayerGraveEntry getEntry(UUID playerUUID, CommandContext<CommandSourceStack> context, String name) {
        int deathId = context.getArgument(name, Integer.class);
        return GraveManager.getWorldGraveData(context.getSource().getLevel())
                .getGraveEntriesForPlayer(playerUUID)
                .get(deathId);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readString();
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.matches() || matcher.groupCount() <= 0) {
            Message msg = Component.literal("Input does not match pattern 'death_<id>...'");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            Message msg = Component.literal("Invalid death id");
            throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt(), msg, input, matcher.start(1));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof ClientSuggestionProvider) {
            try {
                return SuggestionProviders.ASK_SERVER.getSuggestions((CommandContext<SharedSuggestionProvider>) context, builder);
            } catch (CommandSyntaxException e) { }
        }
        if (context.getSource() instanceof CommandSourceStack) {
            try {
                ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
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
        }
        return Suggestions.empty();
    }
}

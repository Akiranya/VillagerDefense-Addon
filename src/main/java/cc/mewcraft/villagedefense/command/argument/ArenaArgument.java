package cc.mewcraft.villagedefense.command.argument;

import cc.mewcraft.villagedefense.VDA;
import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaRegistry;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
@NonnullByDefault
public class ArenaArgument extends CommandArgument<CommandSender, Arena> {

    public ArenaArgument(boolean required,
            @NonNull String name,
            @NonNull String defaultValue,
            @Nullable BiFunction<@NonNull CommandContext<CommandSender>, @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
            @NonNull ArgumentDescription defaultDescription) {
        super(required, name, new Parser(), defaultValue, Arena.class, suggestionsProvider, defaultDescription);
    }

    public static ArenaArgument of(final String name) {
        return builder(name).build();
    }

    public static ArenaArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static ArenaArgument.Builder builder(final String name) {
        return new Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, Arena> {
        @Override
        public ArgumentParseResult<Arena> parse(
                final CommandContext<CommandSender> commandContext,
                final Queue<String> inputQueue
        ) {
            String input = inputQueue.peek();
            Arena arena = ArenaRegistry.getArena(input);
            if (arena != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(arena);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(VDA.lang().legacy("error_arena_not_found", "arena", input)));
        }

        @Override
        public List<String> suggestions(
                final CommandContext<CommandSender> commandContext,
                final String input
        ) {
            return ArenaRegistry.getArenas().stream().map(Arena::getId).toList();
        }
    }

    public static final class Builder extends TypedBuilder<CommandSender, Arena, ArenaArgument.Builder> {
        private Builder(final String name) {
            super(Arena.class, name);
        }

        @Override
        public ArenaArgument build() {
            return new ArenaArgument(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

}

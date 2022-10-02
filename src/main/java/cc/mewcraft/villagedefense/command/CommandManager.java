package cc.mewcraft.villagedefense.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.command.DispatcherCommand;
import cc.mewcraft.villagedefense.command.command.InternalCommand;
import cc.mewcraft.villagedefense.command.command.InventoryCommand;
import cc.mewcraft.villagedefense.command.command.SmartLootCommand;
import cc.mewcraft.villagedefense.module.InventoryManager;
import cc.mewcraft.villagedefense.module.PlayerDispatcher;
import cc.mewcraft.villagedefense.module.SmartLoot;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.paper.PaperCommandManager;
import io.leangen.geantyref.TypeToken;
import lombok.CustomLog;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@CustomLog
public class CommandManager extends PaperCommandManager<CommandSender> {

    public static final CloudKey<VDA> PLUGIN = SimpleCloudKey.of("villagedefenseaddon:plugin", TypeToken.get(VDA.class));

    private final VDA plugin;
    private final Map<String, CommandFlag.Builder<?>> flagRegistry = new HashMap<>();

    public CommandManager(VDA plugin) throws Exception {
        super(
                plugin,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
        this.plugin = plugin;

        // ---- Register Brigadier ----
        if (hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            registerBrigadier();
            final @Nullable CloudBrigadierManager<CommandSender, ?> brigManager = brigadierManager();
            if (brigManager != null) {
                brigManager.setNativeNumberSuggestions(false);
            }
            LOG.info("Successfully registered Mojang Brigadier support for commands.");
        }

        // ---- Register Asynchronous Completion Listener ----
        if (hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            registerAsynchronousCompletions();
            LOG.info("Successfully registered asynchronous command completion listener.");
        }

        this.registerCommandPreProcessor(ctx -> ctx.getCommandContext().store(PLUGIN, plugin));

        // ---- Register all commands ----
        Stream.of(
                new InternalCommand(plugin, this),
                new DispatcherCommand(plugin, this, VDA.instance().getModule(PlayerDispatcher.class)),
                new InventoryCommand(plugin, this, VDA.instance().getModule(InventoryManager.class)),
                new SmartLootCommand(plugin, this, VDA.instance().getModule(SmartLoot.class))
        ).forEach(AbstractCommand::register);
    }

    public CommandFlag.Builder<?> getFlag(final String name) {
        return flagRegistry.get(name);
    }

    public void registerFlag(final String name, final CommandFlag.Builder<?> flagBuilder) {
        flagRegistry.put(name, flagBuilder);
    }

    public void register(final List<Command<CommandSender>> commands) {
        commands.forEach(this::command);
    }

}


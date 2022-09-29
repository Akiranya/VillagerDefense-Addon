package cc.mewcraft.villagedefense.command.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.AbstractCommand;
import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.command.argument.ArenaArgument;
import cc.mewcraft.villagedefense.module.PlayerDispatcher;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import org.bukkit.command.CommandSender;
import plugily.projects.villagedefense.arena.Arena;

import java.util.List;

public class DispatcherCommand extends AbstractCommand {

    private final PlayerDispatcher playerDispatcher;

    public DispatcherCommand(
            VDA plugin,
            CommandManager manager,
            PlayerDispatcher playerDispatcher) {
        super(plugin, manager);

        this.playerDispatcher = playerDispatcher;
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> forceLiteral = manager.commandBuilder("vde");

        Command<CommandSender> forceJoin = forceLiteral
                .permission("vde.command.forcejoin")
                .literal("forcejoin")
                .argument(ArenaArgument.of("arena"))
                .argument(MultiplePlayerSelectorArgument.of("player"))
                .handler(context -> {
                    Arena arena = context.get("arena");
                    MultiplePlayerSelector playerSelector = context.get("player");
                    playerSelector.getPlayers().forEach(p -> playerDispatcher.sendPlayerToArena(arena, p));
                    context.getSender().sendMessage(VDA.lang().component("msg_forced_join_player", "arena", arena.getId()));
                })
                .build();

        Command<CommandSender> forceLeave = forceLiteral
                .permission("vde.command.forceleave")
                .literal("forceleave")
                .argument(MultiplePlayerSelectorArgument.of("player"))
                .handler(context -> {
                    MultiplePlayerSelector playerSelector = context.get("player");
                    playerSelector.getPlayers().forEach(playerDispatcher::forcePlayerQuit);
                    context.getSender().sendMessage(VDA.lang().component("msg_forced_leave_player"));
                })
                .build();

        manager.register(List.of(forceJoin, forceLeave));
    }
}

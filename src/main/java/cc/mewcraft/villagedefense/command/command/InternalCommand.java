package cc.mewcraft.villagedefense.command.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.AbstractCommand;
import cc.mewcraft.villagedefense.command.CommandManager;
import cloud.commandframework.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InternalCommand extends AbstractCommand {

    public InternalCommand(
            VDA plugin,
            CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> reload = manager
                .commandBuilder("vde")
                .permission("vde.command.reload")
                .literal("reload")
                .handler(context -> {
                    plugin.reload();
                    context.getSender().sendMessage(VDA.lang().component("msg_plugin_reloaded",
                            "plugin", VDA.instance().getDescription().getName(),
                            "version", VDA.instance().getDescription().getVersion())
                    );
                })
                .build();

        manager.register(List.of(reload));
    }
}

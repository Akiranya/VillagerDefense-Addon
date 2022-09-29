package cc.mewcraft.villagedefense.command;

import cc.mewcraft.villagedefense.VDA;

public abstract class AbstractCommand {
    protected final VDA plugin;
    protected final CommandManager manager;

    public AbstractCommand(VDA plugin, CommandManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    abstract public void register();
}

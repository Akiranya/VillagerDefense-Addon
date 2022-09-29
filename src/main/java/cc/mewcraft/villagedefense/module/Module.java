package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Module implements Listener, Terminable {

    public void registerListener() {
        VDA.instance().registerListener(this);
    }

    @Override
    public void close() {
        HandlerList.unregisterAll(this);
    }

}

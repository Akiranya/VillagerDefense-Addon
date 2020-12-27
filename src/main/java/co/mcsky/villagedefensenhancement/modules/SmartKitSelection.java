package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.player.VillagePlayerChooseKitEvent;

import java.util.List;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class SmartKitSelection implements Listener {

    private List<String> blacklist;

    public SmartKitSelection() {
        try {
            blacklist = plugin.config.node("better-kit-selection", "blacklist")
                                     .getList(String.class, () -> List.of("骑士"));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void cancelSelection(VillagePlayerChooseKitEvent e) {
        String kit = e.getKit().getName();
        if (blacklist.contains(kit)) {
            e.setCancelled(true);
            plugin.getLogger().info("Kit selection cancelled!");
        }
    }

}

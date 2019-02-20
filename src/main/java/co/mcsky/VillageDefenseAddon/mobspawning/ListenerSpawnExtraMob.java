package co.mcsky.VillageDefenseAddon.mobspawning;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import pl.plajer.villagedefense.api.event.game.VillageGameStopEvent;
import pl.plajer.villagedefense.api.event.wave.VillageWaveStartEvent;

public class ListenerSpawnExtraMob implements Listener {

    private final VillageDefenseAddon plugin;
    private BukkitTask spawning;

    public ListenerSpawnExtraMob(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void spawnExtraMobs(final VillageWaveStartEvent event) {
        Bukkit.getLogger().info("Extra Spawning Task Started.");
        spawning = new TaskExtraMobSpawning(event)
                .runTaskTimer(plugin, 0, 10);
    }

    @EventHandler
    public void stopTaskWhenGameEnd(VillageGameStopEvent event) {
        if (!spawning.isCancelled()) {
            spawning.cancel();
            Bukkit.getLogger().info("Extra Spawning Task Cancelled.");
        }
    }

}

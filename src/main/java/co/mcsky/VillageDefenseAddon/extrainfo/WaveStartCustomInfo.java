package co.mcsky.VillageDefenseAddon.extrainfo;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.api.event.wave.VillageWaveStartEvent;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.options.ArenaOption;

public class WaveStartCustomInfo implements Listener {

    private final VillageDefenseAddon plugin;

    public WaveStartCustomInfo(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onWaveStart(final VillageWaveStartEvent event) {

        new BukkitRunnable() {
            public void run() {
                for (Player p : event.getArena().getPlayers()) {
                    Location loc = p.getLocation();
                    p.sendTitle(
                            ChatColor.GRAY + "他们来了...", "准备迎击!",
                            10, 80, 20
                    );
                    p.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 5, 1);
                }
            }
        }.runTask(this.plugin);

        Arena arena = event.getArena();
        for (ArenaOption o : ArenaOption.values()) {
            plugin.getServer().getLogger().info(o.name() + " " + arena.getOption(o));
        }

    }

}

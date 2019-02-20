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
import pl.plajer.villagedefense.api.event.wave.VillageWaveEndEvent;

public class WaveEndCustomInfo implements Listener {

    private final VillageDefenseAddon plugin;

    public WaveEndCustomInfo(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onWaveEnd(final VillageWaveEndEvent event) {
        new BukkitRunnable() {
            public void run() {
                int timeRemaining = event.getArena().getTimer();
                for (Player p : event.getArena().getPlayers()) {
                    Location loc = p.getLocation();
                    p.sendMessage(
                            ChatColor.GREEN
                            + "*** 辛苦各位勇士了! 下波将在 "
                            + timeRemaining +
                            " 秒后到来, 请抓紧战备."
                    );
                    p.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.PLAYERS, 3, 1);
                }
            }
        }.runTask(this.plugin);
    }

}

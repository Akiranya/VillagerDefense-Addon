package co.mcsky.VillageDefenseAddon.features;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.api.event.wave.VillageWaveEndEvent;
import pl.plajer.villagedefense.api.event.wave.VillageWaveStartEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExtraPlayerStatsUpdater implements Listener {

    private static Map<Player, ExtraPlayerStats> playerStats;
    private final VillageDefenseAddon plugin;
    private Set<EntityType> entityTypeBlacklist;

    public ExtraPlayerStatsUpdater(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        playerStats = new HashMap<>();
        entityTypeBlacklist = new HashSet<>();
        entityTypeBlacklist.add(EntityType.PLAYER);
        entityTypeBlacklist.add(EntityType.WOLF);
        entityTypeBlacklist.add(EntityType.VILLAGER);
        entityTypeBlacklist.add(EntityType.IRON_GOLEM);
    }

    @EventHandler
    public void startLoggingPlayerStats(final VillageWaveStartEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerStats.clear();
                for (Player p : event.getArena().getPlayers()) {
                    playerStats.put(p, new ExtraPlayerStats());
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void logDamageTest(EntityDamageByEntityEvent event) {
        if (playerStats.size() == 0) return;

        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if (victim instanceof Mob && !entityTypeBlacklist.contains(victim.getType())) {
            Player player = null;

            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                ProjectileSource shooter = projectile.getShooter();
                if (shooter instanceof Player) {
                    player = (Player) shooter;
                }
            } else if (damager instanceof Player) {
                player = (Player) damager;
            } else {
                return;
            }

            double damage = event.getFinalDamage();
            ExtraPlayerStats stats = playerStats.get(player);
            playerStats.put(player, stats.addDamage(damage));
            plugin.getServer().getLogger().info(player.getName() + " " + damage + " damage logged.");
        } else {
            return;
        }
    }

    @EventHandler
    public void logKills(final EntityDeathEvent event) {
        if (playerStats.size() == 0) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Entity entity = event.getEntity();

                // If the entity dies from a player, increase kills by 1 for that player
                if (entity instanceof Mob) {
                    Player player = ((Mob) entity).getKiller();
                    if (player == null || !playerStats.containsKey(player)) return;
                    ExtraPlayerStats stats = playerStats.get(player);
                    playerStats.put(player, stats.addKills());
                    plugin.getServer().getLogger().info(player.getName() + " kill logged.");
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void printStatsSummary(final VillageWaveEndEvent event) {
        if (playerStats.size() == 0) return;

        this.plugin.getLogger().info("Player | Kills | Damage");
        for (Player p : playerStats.keySet()) {
            this.plugin.getLogger().info(p.getName()
                    + " | " + playerStats.get(p).getKills()
                    + " | " + playerStats.get(p).getTotalDamageDone());
        }
        playerStats.clear();
    }

}

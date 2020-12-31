package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.wave.VillageWaveEndEvent;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;

import java.util.function.Consumer;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.api;
import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * Assume that there is always only one arena running.
 */
public class RewardManager implements Listener {

    private double divisor;
    private double totalDamage;

    public RewardManager() {
        divisor = plugin.config.node("reward-manager", "divisor").getDouble(1000);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public double getDivisor() {
        return divisor;
    }

    public void setDivisor(double divisor) {
        sync(() -> this.divisor = divisor, divisor, "reward-manager", "divisor");
    }

    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        totalDamage = 0;
    }

    @EventHandler
    public void onWaveEnd(VillageWaveEndEvent event) {
        // Take the ceil to ensure at least 1 exp bottle
        int bottleAmount = (int) Math.ceil(totalDamage / divisor);
        api.getChatManager().broadcastMessage(event.getArena(),
                                              plugin.getMessage(null, "reward-manager.damage-summary",
                                                                "wave-number", event.getWaveNumber() - 1,
                                                                "damage-done", (int) totalDamage,
                                                                "bottle-amount", bottleAmount));
        // Make villagers glow to help players find them
        for (Villager villager : event.getArena().getVillagers()) {
            villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25 * 20, 1));
        }

        // Create exp bottles!
        Bukkit.getScheduler().runTaskTimer(plugin, new Consumer<>() {
            int counter = bottleAmount;

            @Override
            public void accept(BukkitTask task) {
                if (--counter < 0) {
                    task.cancel();
                    return;
                }
                for (Villager villager : event.getArena().getVillagers()) {
                    villager.launchProjectile(ThrownExpBottle.class, Vector.getRandom());
                }
            }
            // Set the period so that all exp bottles are thrown within 30 sec
        }, 5L * 20L, (long) Math.max(1D, 25D / bottleAmount * 20D));
    }

    /**
     * Count damage done by the players to the zombie.
     */
    @EventHandler
    public void onZombieDamageByPlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getEntity() instanceof Zombie && (damager instanceof Player || damager instanceof Projectile || damager instanceof Wolf || damager instanceof IronGolem)) {
            totalDamage += event.getFinalDamage();
        }
    }

    /**
     * If the zombie is healed, invalidate equal amount from the total damage
     * done by players. This prevents the case where the player could farm coins
     * by healing & damaging, back and forth.
     */
    @EventHandler
    public void onZombieRegainHealthByPlayer(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Zombie) {
            totalDamage -= event.getAmount();
        }
    }

    /**
     * A convenience method to enforce updating values in both class fields and
     * the plugin config file.
     *
     * @param setter setter which sets the class fields
     * @param value  the value to be stored in the config file
     * @param path   the path to which the value to be stored in the config
     *               file
     */
    private void sync(Runnable setter, Object value, Object... path) {
        setter.run();
        try {
            plugin.config.node(path).set(value);
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        plugin.config.save();
    }

}

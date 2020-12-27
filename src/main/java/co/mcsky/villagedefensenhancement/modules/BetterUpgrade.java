package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import plugily.projects.villagedefense.api.event.player.VillagePlayerEntityUpgradeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class BetterUpgrade implements Listener {

    private final Map<Integer, AtomicInteger> countDown;
    private final int healPotionCount;

    public BetterUpgrade() {
        countDown = new HashMap<>();
        healPotionCount = plugin.config.node("better-upgrade", "heal-potion-count").getInt(5);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Heal the entity when it is being upgraded.
     */
    @EventHandler
    public void onEntityUpgrade(VillagePlayerEntityUpgradeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            ItemStack item = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setColor(Color.RED);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 10, 2), true);
            item.setItemMeta(meta);

            plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                // Check if we should cancel this task
                countDown.putIfAbsent(task.getTaskId(), new AtomicInteger(healPotionCount));
                if (countDown.get(task.getTaskId()).decrementAndGet() < 0) {
                    countDown.remove(task.getTaskId());
                    task.cancel();
                    return;
                }

                // Throw heal potion onto the entity
                ThrownPotion thrownPotion = ((LivingEntity) entity).launchProjectile(ThrownPotion.class);
                thrownPotion.setItem(item);
                // Throw the potion onto this feet
                Vector dir = entity.getLocation().toVector().subtract(((LivingEntity) entity).getEyeLocation().toVector());
                thrownPotion.setVelocity(dir);
            }, 20L, 20L);
        }
    }


}

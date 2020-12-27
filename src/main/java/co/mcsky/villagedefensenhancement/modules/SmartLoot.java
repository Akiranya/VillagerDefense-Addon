package co.mcsky.villagedefensenhancement.modules;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collection;
import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class SmartLoot implements Listener {

    private final Random random;

    @Getter private int meleeExp;
    @Getter private int rangeExp;
    @Getter private double damageLowerBound;

    public SmartLoot() {
        random = new Random();

        // Configuration values
        meleeExp = plugin.config.node("archer-enhancement", "melee-exp").getInt(1);
        rangeExp = plugin.config.node("archer-enhancement", "range-exp").getInt(2);
        damageLowerBound = plugin.config.node("archer-enhancement", "damage-lower-bound").getDouble(2D);

        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setMeleeExp(int meleeExp) {
        syncWithConfig(() -> this.meleeExp = meleeExp);
    }

    public void setRangeExp(int rangeExp) {
        syncWithConfig(() -> this.rangeExp = rangeExp);
    }

    public void setDamageLowerBound(double damageLowerBound) {
        syncWithConfig(() -> this.damageLowerBound = damageLowerBound);
    }

    @EventHandler
    public void giveExpToAttacker(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof Monster && event.getDamage() > damageLowerBound) {
            if (damager instanceof Player) {
                // Give exp to melee attacker

                Player player = (Player) damager;
                player.giveExp(meleeExp);
                player.sendActionBar(plugin.getMessage(damager, "smart-loot.exp-gained",
                                                       "exp", String.valueOf(meleeExp)));
            } else if (damager instanceof Projectile) {
                // Give exp to ranged attacker

                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    Player player = (Player) projectile.getShooter();
                    player.giveExp(rangeExp);
                    player.sendActionBar(plugin.getMessage(player, "smart-loot.exp-gained",
                                                           "exp", String.valueOf(rangeExp)));

                    // Give loots if the damaged zombie is dead
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (entity.isDead()) {
                            LootContext lootContext = new LootContext
                                    .Builder(entity.getLocation())
                                    .killer(player)
                                    .lootedEntity(entity)
                                    .build();
                            Collection<ItemStack> itemStacks = ((Monster) entity).getLootTable().populateLoot(random, lootContext);
                            itemStacks.forEach(item -> player.getInventory().addItem(item));
                        }
                    });
                }
            }
        }
    }

    private void syncWithConfig(Runnable update) {
        update.run();
        try {
            plugin.config.node("archer-enhancement", "melee-exp").set(meleeExp);
            plugin.config.node("archer-enhancement", "range-exp").set(rangeExp);
            plugin.config.node("archer-enhancement", "damage-lower-bound").set(damageLowerBound);
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        plugin.config.save();
    }


}

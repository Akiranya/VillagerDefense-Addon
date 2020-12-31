package co.mcsky.villagedefensenhancement.modules;

import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.api;
import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class SmartLoot implements Listener {

    private final Random random;
    private int meleeExp;
    private int rangeExp;
    private int meleeLevelMultiplier;
    private int rangeLevelMultiplier;
    private double damageLowerBound;

    public SmartLoot() {
        random = new Random();

        // Configuration values
        meleeExp = plugin.config.node("smart-loot", "melee-exp").getInt(1);
        rangeExp = plugin.config.node("smart-loot", "range-exp").getInt(2);
        meleeLevelMultiplier = plugin.config.node("smart-loot", "melee-level-multiplier").getInt(2);
        rangeLevelMultiplier = plugin.config.node("smart-loot", "range-level-multiplier").getInt(4);
        damageLowerBound = plugin.config.node("smart-loot", "damage-lower-bound").getDouble(2D);

        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public int getMeleeExp() {
        return meleeExp;
    }

    public void setMeleeExp(int meleeExp) {
        sync(() -> this.meleeExp = meleeExp, this.meleeExp,
             "smart-loot", "melee-exp");
    }

    public int getRangeExp() {
        return rangeExp;
    }

    public void setRangeExp(int rangeExp) {
        sync(() -> this.rangeExp = rangeExp, this.rangeExp,
             "smart-loot", "range-exp");
    }

    public int getMeleeLevelMultiplier() {
        return meleeLevelMultiplier;
    }

    public void setMeleeLevelMultiplier(int meleeLevelMultiplier) {
        sync(() -> this.meleeLevelMultiplier = meleeLevelMultiplier, this.meleeLevelMultiplier,
             "smart-loot", "melee-level-multiplier");
    }

    public int getRangeLevelMultiplier() {
        return rangeLevelMultiplier;
    }

    public void setRangeLevelMultiplier(int rangeLevelMultiplier) {
        sync(() -> this.rangeLevelMultiplier = rangeLevelMultiplier, this.rangeLevelMultiplier,
             "smart-loot", "range-level-multiplier");
    }

    public double getDamageLowerBound() {
        return damageLowerBound;
    }

    public void setDamageLowerBound(double damageLowerBound) {
        sync(() -> this.damageLowerBound = damageLowerBound, this.damageLowerBound,
             "smart-loot", "damage-lower-bound");
    }

    /**
     * Give exp & level to attacker on each damage.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof Monster && event.getDamage() > damageLowerBound) {
            if (damager instanceof Player) {
                // Give exp & level to melee attacker

                Player player = (Player) damager;
                player.giveExp(meleeExp);
                api.getUserManager().addExperience(player, meleeExp * meleeLevelMultiplier);
                player.sendActionBar(plugin.getMessage(damager, "smart-loot.exp-gained",
                                                       "exp", meleeExp));
            } else if (damager instanceof Projectile) {
                // Give exp & level to ranged attacker

                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    Player player = (Player) projectile.getShooter();
                    player.giveExp(rangeExp);
                    api.getUserManager().addExperience(player, rangeExp * rangeLevelMultiplier);
                    player.sendActionBar(plugin.getMessage(player, "smart-loot.exp-gained",
                                                           "exp", rangeExp));

                    // Give loots if the damaged monster is dead
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (entity.isDead()) {
                            LootContext lootContext = new LootContext
                                    .Builder(entity.getLocation())
                                    .killer(player)
                                    .lootedEntity(entity)
                                    .build();
                            LootTable lootTable = ((Monster) entity).getLootTable();
                            if (lootTable != null) {
                                for (ItemStack item : lootTable.populateLoot(random, lootContext)) {
                                    player.getInventory().addItem(item);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Don't merge exp orbs so that each player can get some exp
     */
    @EventHandler
    public void onExpMerge(ExperienceOrbMergeEvent event) {
        event.setCancelled(true);
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

package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import lombok.CustomLog;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@CustomLog
public class SmartLoot extends Module {

    @Getter private int meleeVanillaExp;
    @Getter private int rangedVanillaExp;
    @Getter private int meleeKitExp;
    @Getter private int rangedKitExp;
    @Getter private double minimumDamageRequirement;
    private EnumSet<Material> monsterDropWhitelist;

    public SmartLoot() {
        // Configuration values
        CommentedConfigurationNode root = VDA.config().node("smart-loot");
        meleeVanillaExp = root.node("melee-vanilla-exp").getInt(1);
        meleeKitExp = root.node("melee-kit-exp").getInt(1);
        rangedVanillaExp = root.node("ranged-vanilla-exp").getInt(2);
        rangedKitExp = root.node("ranged-kit-exp").getInt(2);
        minimumDamageRequirement = root.node("minimum-damage-requirement").getDouble(2D);

        CommentedConfigurationNode node1 = root.node("monster-drop-whitelist");
        try {
            monsterDropWhitelist = EnumSet.copyOf(node1.getList(Material.class, List.of(
                    Material.ROTTEN_FLESH
            )));
        } catch (SerializationException e) {
            monsterDropWhitelist = EnumSet.noneOf(Material.class);
            LOG.warn("Failed to read config: " + node1.path() + ". Nothing will drop from monsters!", e);
        }

        // Register this listener
        registerListener();
    }

    public void setMeleeVanillaExp(int meleeVanillaExp) {
        sync(() -> this.meleeVanillaExp = meleeVanillaExp, meleeVanillaExp, "smart-loot", "melee-vanilla-exp");
    }

    public void setRangedVanillaExp(int rangedVanillaExp) {
        sync(() -> this.rangedVanillaExp = rangedVanillaExp, rangedVanillaExp, "smart-loot", "range-vanilla-exp");
    }

    public void setMeleeKitExp(int meleeKitExp) {
        sync(() -> this.meleeKitExp = meleeKitExp, meleeKitExp, "smart-loot", "melee-kit-exp");
    }

    public void setRangedKitExp(int rangedKitExp) {
        sync(() -> this.rangedKitExp = rangedKitExp, rangedKitExp, "smart-loot", "ranged-kit-exp");
    }

    public void setMinimumDamageRequirement(double minimumDamageRequirement) {
        sync(() -> this.minimumDamageRequirement = minimumDamageRequirement, minimumDamageRequirement, "smart-loot", "minimum-damage-requirement");
    }

    /**
     * New Feature: give exp and level to attacker on each damage.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof Monster && event.getDamage() > minimumDamageRequirement) {

            if (damager instanceof Player player) {
                // Give exp & level to melee attacker

                player.giveExp(meleeVanillaExp);
                VDA.api().getUserManager().addExperience(player, meleeKitExp);
                player.sendActionBar(VDA.lang().component(player,
                        "msg_gained_experience",
                        "exp", Double.toString(meleeVanillaExp)));

            } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                // Give exp & level to ranged attacker

                player.giveExp(rangedVanillaExp);
                VDA.api().getUserManager().addExperience(player, rangedKitExp);
                player.sendActionBar(VDA.lang().component(player,
                        "msg_gained_experience",
                        "exp", Double.toString(rangedVanillaExp)));

                // Give loots if the damaged monster is dead
            }
        }
    }

    /**
     * <p>New Feature 1: drops go directly into player inventory.
     * <p>New Feature 2: monsters only drop items in whitelist.
     */
    @EventHandler
    public void onMonsterDeath(EntityDeathEvent event1) {
        if (event1.getEntity() instanceof Monster monster && monster.getLastDamageCause() instanceof EntityDamageByEntityEvent event2) {

            // Only drop items in whitelist
            event1.getDrops().removeIf(item -> !monsterDropWhitelist.contains(item.getType()));

            Player dropReceiver = null;
            if (event2.getDamager() instanceof Player player) {
                // Killed by melee attack
                dropReceiver = player;
            } else if (event2.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                // Killed by ranged attack
                dropReceiver = player;
            } else {
                // We don't know how it was killed, so pick the nearest player
                double minDistance = Double.MAX_VALUE;
                Collection<Player> nearby = monster.getLocation().getNearbyEntitiesByType(Player.class, 16);
                for (Player player : nearby) {
                    double distanceSquared = monster.getLocation().distanceSquared(player.getLocation());
                    if (distanceSquared < minDistance) {
                        dropReceiver = player;
                    }
                }
                EntityDamageEvent lastDamageCause = event1.getEntity().getLastDamageCause();
                if (lastDamageCause != null) {
                    LOG.info("Not specifically handled damage cause: " + lastDamageCause.getCause());
                }
            }

            if (dropReceiver != null) {
                for (ItemStack item : event1.getDrops()) {
                    dropReceiver.getInventory().addItem(item);
                }
                dropReceiver.giveExp(event1.getDroppedExp());
                event1.getDrops().clear();
                event1.setDroppedExp(0);
            }
        }
    }

    /**
     * A convenience method to enforce updating values in both class fields and the plugin config file.
     *
     * @param setter setter which sets the class fields
     * @param value  the value to be stored in the config file
     * @param path   the path to which the value to be stored in the config file
     */
    private void sync(Runnable setter, Object value, Object... path) {
        setter.run();
        try {
            VDA.config().node(path).set(value);
        } catch (SerializationException e) {
            LOG.reportException(e);
        }
        VDA.config().save();
    }
}

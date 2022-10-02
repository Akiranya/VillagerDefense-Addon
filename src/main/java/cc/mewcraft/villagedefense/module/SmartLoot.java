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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.handlers.language.Messages;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@CustomLog
public class SmartLoot extends Module {

    private final CommentedConfigurationNode root;
    @Getter private int meleeVanillaExp;
    @Getter private int rangedVanillaExp;
    @Getter private int meleeKitExp;
    @Getter private int rangedKitExp;
    @Getter private double minimumDamageRequirement;
    private EnumSet<Material> monsterDropWhitelist;

    public SmartLoot() {
        root = VDA.config().node("smart-loot");

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

        registerListener();
    }

    public void setMeleeVanillaExp(int meleeVanillaExp) {
        this.meleeVanillaExp = meleeVanillaExp;
    }

    public void setRangedVanillaExp(int rangedVanillaExp) {
        this.rangedVanillaExp = rangedVanillaExp;
    }

    public void setMeleeKitExp(int meleeKitExp) {
        this.meleeKitExp = meleeKitExp;
    }

    public void setRangedKitExp(int rangedKitExp) {
        this.rangedKitExp = rangedKitExp;
    }

    public void setMinimumDamageRequirement(double minimumDamageRequirement) {
        this.minimumDamageRequirement = minimumDamageRequirement;
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
            }

            if (dropReceiver != null) {
                // Transfer items
                for (ItemStack item : event1.getDrops()) {
                    dropReceiver.getInventory().addItem(item);
                }

                // Transfer exp & orbs
                dropReceiver.giveExp(event1.getDroppedExp());
                VDA.api().getUserManager().getUser(dropReceiver).addStat(StatsStorage.StatisticType.ORBS, event1.getDroppedExp());
                dropReceiver.sendMessage(VDA.api().getChatManager().colorMessage(Messages.ORBS_PICKUP).replace("%number%", Integer.toString(event1.getDroppedExp())));

                // Clear original drops
                event1.getDrops().clear();
                event1.setDroppedExp(0);
            }
        }
    }

    @Override
    public void saveConfig() {
        try {
            root.node("minimum-damage-requirement").set(minimumDamageRequirement);
            root.node("melee-vanilla-exp").set(meleeVanillaExp);
            root.node("melee-kit-exp").set(meleeKitExp);
            root.node("ranged-vanilla-exp").set(rangedVanillaExp);
            root.node("ranged-kit-exp").set(rangedKitExp);
        } catch (SerializationException e) {
            LOG.reportException(e);
        }
        VDA.config().save();
    }

}

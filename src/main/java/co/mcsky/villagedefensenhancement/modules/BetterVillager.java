package co.mcsky.villagedefensenhancement.modules;

import co.aikar.commands.ACFBukkitUtil;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.player.VillagePlayerEntityUpgradeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * A class improves the interaction with friendly creatures.
 */
public class BetterVillager implements Listener {

    private final int healPotionCount;
    private final int particleCount;
    private final int longestSightLine;
    private final float pushScalar;
    private Set<EntityType> noCollisionEntities;

    public BetterVillager() {
        pushScalar = plugin.config.node("better-villager", "push-scalar").getFloat(1.5F);
        // Spawn healing potions when upgrading a entity
        healPotionCount = plugin.config.node("better-villager", "heal-potion-count").getInt(10);
        // Allow players to control friendly creatures
        particleCount = plugin.config.node("better-villager", "particle-count").getInt(16);
        longestSightLine = plugin.config.node("better-villager", "longest-sight-line").getInt(32);
        // Projectiles from players can pass through friendly creatures
        try {
            noCollisionEntities = new HashSet<>(plugin.config.node("better-villager", "no-collision-entities")
                                                             .getList(EntityType.class, () ->
                                                                     List.of(EntityType.PLAYER,
                                                                             EntityType.VILLAGER,
                                                                             EntityType.IRON_GOLEM,
                                                                             EntityType.WOLF)));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
            return;
        }

        // Register this event
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Prompt players if there is any villager on damage.
     */
    @EventHandler
    public void onVillagerDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Villager) {
            // Only remind if the damager is a zombie or wither
            Entity damager = event.getDamager();
            if (damager instanceof Monster && event.getFinalDamage() > 1) {
                ((Villager) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 1));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(plugin.getMessage(player, "better-villager.cry-message", "villager", entity.getCustomName()));
                }
            }
        }
    }

    /**
     * Stop villagers from opening/passing doors.
     */
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Villager) {
            Villager v = (Villager) entity;
            // Make it unable to open doors
            v.getPathfinder().setCanPassDoors(false);
            v.getPathfinder().setCanOpenDoors(false);
        }
    }

    /**
     * Allow players to leash villagers (this mimics the vanilla behaviors).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashFriendlyCreature(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        Player player = event.getPlayer();
        if (rightClicked instanceof Villager) {
            ItemStack lead = new ItemStack(Material.LEAD);
            try {
                if (((Villager) rightClicked).getLeashHolder() == player) {
                    // Unleash and return

                    player.getOpenInventory().close(); // Close the shop menu
                    ((Villager) rightClicked).setLeashHolder(null);
                    rightClicked.getWorld().dropItem(rightClicked.getLocation(), lead);
                    return;
                }
            } catch (IllegalStateException ignored) {
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
                // Leash it

                player.getOpenInventory().close(); // Close the shop menu
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.getInventory().remove(lead);
                    ((Villager) rightClicked).setLeashHolder(player);
                });
            }
        }
    }

    /**
     * Allow players to control villagers/golems/wolves when mounting them!
     */
    @EventHandler
    public void onPlayerTarget(PlayerInteractEvent event) {
        if (event.getMaterial() == Material.SADDLE && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            if (player.isInsideVehicle()) {
                Entity mount = player.getVehicle();
                if (mount instanceof Villager || mount instanceof Wolf || mount instanceof IronGolem) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        // Push the mount

                        Vector direction = player.getLocation().getDirection();
                        mount.setVelocity(direction.multiply(pushScalar));

                    } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        // Walk the mount

                        Block targetBlock = player.getTargetBlock(longestSightLine);
                        if (targetBlock != null) {
                            // Set destination for path finder
                            boolean success = ((Mob) mount).getPathfinder().moveTo(targetBlock.getLocation());

                            // Prompt player the information about path finder
                            if (success) {
                                player.sendActionBar(plugin.getMessage(player, "better-villager.move-to-destination", "location", ACFBukkitUtil.formatLocation(targetBlock.getLocation())));
                            } else {
                                player.sendActionBar(plugin.getMessage(player, "better-villager.cannot-find-path"));
                            }

                            // Create particles on destination
                            player.getWorld().spawnParticle(Particle.LAVA, targetBlock.getLocation(), particleCount);
                        }
                    }
                }
            }
        }
    }

//    /**
//     * After the player mounts the villager, we temporarily enable the villager
//     * to pass/open doors.
//     */
//    @EventHandler
//    public void onPlayerMountVillager(EntityMountEvent event) {
//        Entity mount = event.getMount();
//        if (mount instanceof Villager && event.getEntity() instanceof Player) {
//            ((Villager) mount).setAware(false);
//        }
//    }
//
//    /**
//     * After the player dismounts the villager, we disable the villager to
//     * pass/open doors (back to our initial settings).
//     */
//    @EventHandler
//    public void onPlayerDismountVillager(EntityDismountEvent event) {
//        Entity dismounted = event.getDismounted();
//        if (dismounted instanceof Villager && event.getEntity() instanceof Player) {
//            ((Villager) dismounted).setAware(true);
//        }
//    }

    /**
     * Prevent wolves from sitting.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickWolf(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Wolf) {
            event.setCancelled(true);
        }
    }

    /**
     * Projectiles from players can pass through friendly creatures.
     */
    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event) {
        if (noCollisionEntities.contains(event.getCollidedWith().getType())) {
            event.setCancelled(true);
        }
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

            plugin.getServer().getScheduler().runTaskTimer(plugin, new Consumer<>() {
                private int count = healPotionCount;

                @Override
                public void accept(BukkitTask task) {
                    // Check if we should cancel this task
                    if (--count < 0) {
                        task.cancel();
                        return;
                    }
                    // Throw heal potion onto the entity
                    Vector feetV = entity.getLocation().toVector();
                    Vector eyeV = ((LivingEntity) entity).getEyeLocation().toVector();
                    ThrownPotion thrownPotion = ((LivingEntity) entity).launchProjectile(ThrownPotion.class, feetV.subtract(eyeV));
                    thrownPotion.setItem(item);
                }
            }, 20L, 30L);
        }
    }

}

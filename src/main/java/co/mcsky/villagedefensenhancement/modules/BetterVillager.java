package co.mcsky.villagedefensenhancement.modules;

import co.aikar.commands.ACFBukkitUtil;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;
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
    private final double pushScalar;
    private Set<EntityType> noCollisionEntities;

    // Work around to prevent executing onPlayerTarget() right after onPlayerDropSaddle()
    private boolean flag;

    public BetterVillager() {
        // Strength
        pushScalar = plugin.config.node("better-villager", "push-scalar").getDouble(1.0);
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
     * Allow players to leash villagers (this mimics the vanilla behavior).
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
     * Allow players to control villagers/golems/wolves using saddle when
     * mounting them! This listener assumes that the player ALREADY enters the
     * mount.
     */
    @EventHandler
    public void onPlayerTarget(PlayerInteractEvent event) {
        if (flag) {
            // Work around to prevent executing onPlayerTarget() right after onPlayerDropSaddle()

            flag = false;
            return;
        }

        if (event.getMaterial() == Material.SADDLE && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            if (player.isInsideVehicle()) {
                Entity mount = player.getVehicle();
                if (mount instanceof Mob && !(mount instanceof Monster)) {
                    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        // Push the mount

                        if (mount.isOnGround()) {
                            pushMount(player, mount);
                        }

                    } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        // Walk the mount

                        setGoal(player, (Mob) mount);
                    }
                }
            }
        }
    }

    /**
     * Allow the player to make the mount jump if the player tries to drop the
     * saddle in their hand.
     */
    @EventHandler
    public void onPlayerDropSaddle(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.SADDLE && event.getPlayer().getVehicle() instanceof LivingEntity) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof Villager) {
                flag = true;
                if (((Villager) vehicle).isAware()) {
                    ((Villager) vehicle).setAware(false);
                    vehicle.getVelocity().zero(); // Immediately stop this mount
                    vehicle.playEffect(EntityEffect.VILLAGER_ANGRY);
                    player.sendActionBar(plugin.getMessage(player, "better-villager.aware-status", "status", "关"));
                } else {
                    ((Villager) vehicle).setAware(true);
                    vehicle.playEffect(EntityEffect.VILLAGER_HAPPY);
                    player.sendActionBar(plugin.getMessage(player, "better-villager.aware-status", "status", "开"));
                }
            }
        }
    }

    /**
     * Convenience method.
     * <p>
     * Push the mount towards where the player is looking at.
     */
    private void pushMount(Player player, Entity mount) {
        Vector direction = player.getLocation().getDirection().multiply(pushScalar);
        player.playSound(player.getLocation(), Sound.BLOCK_SNOW_HIT, 1F, 0F);
        mount.setVelocity(direction);
    }

    /**
     * Convenience method.
     * <p>
     * Set the goal of the mount to where the player is looking at.
     */
    private void setGoal(Player player, Mob mount) {
        Block targetBlock = player.getTargetBlock(longestSightLine);
        if (targetBlock != null) {
            Pathfinder pathfinder = mount.getPathfinder();
            // Temporarily allow the mount to pass/open doors
//            pathfinder.setCanPassDoors(true);
//            pathfinder.setCanOpenDoors(true);
            boolean success = pathfinder.moveTo(targetBlock.getLocation());
            if (success) {
                player.sendActionBar(plugin.getMessage(player, "better-villager.move-to-destination", "location", ACFBukkitUtil.formatLocation(targetBlock.getLocation())));
            } else {
                player.sendActionBar(plugin.getMessage(player, "better-villager.cannot-find-path"));
            }
            // After the mob got its path, disable it to pass/open doors again
//            pathfinder.setCanPassDoors(false);
//            pathfinder.setCanOpenDoors(false);

            // Create particles on destination
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 0.5F, 0F);
            player.getWorld().spawnParticle(Particle.LAVA, targetBlock.getLocation(), particleCount);
        }
    }

    @EventHandler
    public void onPlayerMountVillager(EntityMountEvent event) {
        if (event.getMount() instanceof Villager) {
            ((Villager) event.getMount()).setAware(false);
        }
    }

    @EventHandler
    public void onPlayerDismountVillager(EntityDismountEvent event) {
        if (event.getDismounted() instanceof Villager) {
            ((Villager) event.getDismounted()).setAware(true);
        }
    }

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

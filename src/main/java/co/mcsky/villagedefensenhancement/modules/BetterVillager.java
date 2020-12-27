package co.mcsky.villagedefensenhancement.modules;

import co.aikar.commands.ACFBukkitUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.game.VillageGameStartEvent;

import java.util.List;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class BetterVillager implements Listener {

    private final int longestSightLine;
    private final int particleCount;

    private VillageGameStartEvent gameEvent;
    private List<String> cryMessages;
    private int currentMessageIndex;

    public BetterVillager() {
        // Configuration values
        particleCount = plugin.config.node("better-villager", "particle-count").getInt(16);
        longestSightLine = plugin.config.node("better-villager", "longest-sight-line").getInt(32);
        try {
            cryMessages = plugin.config.node("better-villager", "cry-messages")
                                       .getList(String.class, () ->
                                               List.of("村民 -> 有只僵尸在打我，快来人救救我！",
                                                       "村民 -> 我快被僵尸打死啦，快来人呐！",
                                                       "村民 -> 僵尸大军冲进来了，救命呐！"));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
            return;
        }

        // Register this event
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void initEvent(VillageGameStartEvent event) {
        gameEvent = event;
    }

    /**
     * Prompt players if there is any villager on damage.
     */
    @EventHandler
    public void remindOnDamage(EntityDamageByEntityEvent event) {
        if (gameEvent == null) return;

        Entity entity = event.getEntity();
        if (entity instanceof Villager) {
            // Only remind if the damager is a zombie or wither
            Entity damager = event.getDamager();
            if (damager instanceof Monster && event.getFinalDamage() > 1) {
                ((Villager) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 1));
                String message = cryMessages.get(currentMessageIndex++ % cryMessages.size());
                gameEvent.getArena().getPlayers().forEach(p -> p.sendActionBar(message));
            }
        }
    }

    /**
     * Stop villagers from opening doors.
     */
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Villager) {
            Villager v = (Villager) entity;
            // Make it unable to open doors
            v.getPathfinder().setCanOpenDoors(false);
        }
    }

    /**
     * Allow players to leash villagers.
     */
    @EventHandler
    public void onEntityLeash(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        Player player = event.getPlayer();
        if (rightClicked instanceof Villager) {
            try {
                if (((Villager) rightClicked).getLeashHolder() == player) {
                    // Unleash and return
                    ((Villager) rightClicked).setLeashHolder(null);
                    return;
                }
            } catch (IllegalStateException ignored) {
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
                // Leash it
                plugin.getServer().getScheduler().runTask(plugin, () -> ((Villager) rightClicked).setLeashHolder(player));
            }
        }
    }

    /**
     * Allow players to ride villagers/golems/wolves and control them!
     */
    @EventHandler
    public void onPlayerPoint(PlayerInteractEvent event) {
        if (event.getMaterial() == Material.SADDLE && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            if (player.isInsideVehicle()) {
                Entity vehicle = player.getVehicle();
                if (vehicle instanceof Mob) {
                    Block targetBlock = player.getTargetBlock(longestSightLine);
                    if (targetBlock != null) {
                        // Set destination for path finder
                        boolean success = ((Mob) vehicle).getPathfinder().moveTo(targetBlock.getLocation());

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

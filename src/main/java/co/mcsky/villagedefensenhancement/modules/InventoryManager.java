package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class InventoryManager implements Listener {

    private Set<Material> dropWhitelist;
    private Set<Material> pickupWhitelist;

    public InventoryManager() {
        // Configuration values
        try {
            dropWhitelist = new HashSet<>(plugin.config.node("inventory-manager", "drop-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
            pickupWhitelist = new HashSet<>(plugin.config.node("inventory-manager", "pickup-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() && !pickupWhitelist.contains(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
            player.sendActionBar(plugin.getMessage(player, "inventory-manager.cannot-pickup"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() && !dropWhitelist.contains(event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(true);
            player.sendActionBar(plugin.getMessage(player, "inventory-manager.cannot-drop"));
        }
    }

    @EventHandler
    public void onOpenHopper(PlayerInteractEvent event) {
        if (!event.getPlayer().isOp() && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.HOPPER) {
            event.setCancelled(true);
        }
    }

    public void dropAddWhitelist(Material mat) {
        sync(() -> dropWhitelist.add(mat), Material.class, dropWhitelist,
             "inventory-manager", "drop-whitelist");
    }

    public void dropRemoveWhitelist(Material mat) {
        sync(() -> dropWhitelist.remove(mat), Material.class, dropWhitelist,
             "inventory-manager", "drop-whitelist");
    }

    public void pickupAddWhitelist(Material mat) {
        sync(() -> pickupWhitelist.add(mat), Material.class, pickupWhitelist,
             "inventory-manager", "pickup-whitelist");
    }

    public void pickupRemoveWhitelist(Material mat) {
        sync(() -> pickupWhitelist.remove(mat), Material.class, pickupWhitelist,
             "inventory-manager", "pickup-whitelist");
    }

    public void dropListShow(CommandSender sender) {
        dropWhitelist.forEach(e -> sender.sendMessage(e.toString()));
    }

    public void pickupListShow(CommandSender sender) {
        pickupWhitelist.forEach(e -> sender.sendMessage(e.toString()));
    }

    public void enable() {
        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("InventoryManager is on!");
    }

    public void disable() {
        // Unregister this listener
        HandlerList.unregisterAll(this);
        plugin.getLogger().info("InventoryManager is off!");
    }

    /**
     * A convenience method to enforce updating values in both class fields and
     * the plugin config file.
     *
     * @param setter setter which sets the class fields
     * @param clazz  the element type of the list
     * @param value  the value to be stored in the config file
     * @param path   the path to which the value to be stored in the config
     *               file
     */
    private <E, C extends Collection<E>> void sync(Runnable setter, Class<E> clazz, C value, Object... path) {
        setter.run();
        try {
            plugin.config.node(path).setList(clazz, new ArrayList<>(value));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        plugin.config.save();
    }

}

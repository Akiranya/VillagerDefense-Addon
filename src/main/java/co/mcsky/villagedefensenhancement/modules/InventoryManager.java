package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/*
    @EventHandler
    public void onGameStart(VillageGameStartEvent event) {
        enable();
    }

    @EventHandler
    public void onGameEnd(VillageGameStopEvent event) {
        disable();
    }
*/

    @EventHandler
    public void onPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || !pickupWhitelist.contains(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
            player.sendActionBar(plugin.getMessage(player, "inventory-manager.cannot-pickup"));
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || !dropWhitelist.contains(event.getItemDrop().getItemStack().getType())) {
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
        syncWithConfig(() -> dropWhitelist.add(mat));
    }

    public void dropRemoveWhitelist(Material mat) {
        syncWithConfig(() -> dropWhitelist.remove(mat));
    }

    public void pickupAddWhitelist(Material mat) {
        syncWithConfig(() -> pickupWhitelist.add(mat));
    }

    public void pickupRemoveWhitelist(Material mat) {
        syncWithConfig(() -> pickupWhitelist.remove(mat));
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

    private void syncWithConfig(Runnable update) {
        update.run();
        try {
            plugin.config.node("inventory-manager", "pickup-whitelist").setList(Material.class, new ArrayList<>(pickupWhitelist));
            plugin.config.node("inventory-manager", "drop-whitelist").setList(Material.class, new ArrayList<>(dropWhitelist));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        plugin.config.save();
    }

}

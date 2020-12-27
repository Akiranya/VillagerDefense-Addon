package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.game.VillageGameStartEvent;
import plugily.projects.villagedefense.api.event.game.VillageGameStopEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class InventoryManager implements Listener {

    private final PlayerActionListener playerActionListener;
    private Set<Material> dropWhitelist;
    private Set<Material> pickupWhitelist;

    public InventoryManager() {
        playerActionListener = new PlayerActionListener();

        // Configuration values
        try {
            dropWhitelist = new HashSet<>(plugin.config.node("inventory-manager", "drop-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
            pickupWhitelist = new HashSet<>(plugin.config.node("inventory-manager", "pickup-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGameStart(VillageGameStartEvent event) {
        enable();
    }

    @EventHandler
    public void onGameEnd(VillageGameStopEvent event) {
        disable();
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
        plugin.getServer().getPluginManager().registerEvents(playerActionListener, plugin);
        plugin.getLogger().info("InventoryManager is on!");
    }

    public void disable() {
        // Unregister this listener
        HandlerList.unregisterAll(playerActionListener);
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

    private class PlayerActionListener implements Listener {

        @EventHandler
        public void onPickupItem(PlayerAttemptPickupItemEvent event) {
            if (!pickupWhitelist.contains(event.getItem().getItemStack().getType())) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.sendActionBar(plugin.getMessage(player, "inventory-manager.cannot-pickup"));
            }
        }

        @EventHandler
        public void onDropItem(PlayerDropItemEvent event) {
            if (!dropWhitelist.contains(event.getItemDrop().getItemStack().getType())) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.sendActionBar(plugin.getMessage(player, "inventory-manager.cannot-drop"));
            }
        }

    }

}

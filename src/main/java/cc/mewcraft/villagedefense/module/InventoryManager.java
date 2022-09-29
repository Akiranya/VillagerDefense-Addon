package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import lombok.CustomLog;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@CustomLog
@ParametersAreNonnullByDefault
public class InventoryManager extends Module {

    private EnumSet<Material> dropWhitelist;
    private EnumSet<Material> pickupWhitelist;

    public InventoryManager() {
        // Configuration values
        CommentedConfigurationNode root = VDA.config().node("inventory-manager");
        try {
            dropWhitelist = EnumSet.copyOf(root.node("drop-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
            pickupWhitelist = EnumSet.copyOf(root.node("pickup-whitelist").getList(Material.class, List.of(Material.ROTTEN_FLESH)));
        } catch (SerializationException e) {
            dropWhitelist = EnumSet.noneOf(Material.class);
            pickupWhitelist = EnumSet.noneOf(Material.class);
            LOG.error("Failed to read config: " + root.path() + ". Inventory Manager will not work properly!", e);
        }

        registerListener();
    }

    public void enable() {
        VDA.instance().registerListener(this);
        LOG.info("InventoryManager is on!");
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        LOG.info("InventoryManager is off!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() &&
            !pickupWhitelist.contains(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
            player.sendActionBar(VDA.lang().component(player,
                    "error_cannot_pickup_this_item"
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() &&
            !dropWhitelist.contains(event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(true);
            player.sendActionBar(VDA.lang().component(player,
                    "error_cannot_drop_this_item"
            ));
        }
    }

    @EventHandler
    public void onOpenHopper(PlayerInteractEvent event) {
        if (!event.getPlayer().isOp() &&
            event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.HOPPER) {
            event.setCancelled(true);
        }
    }

    public List<Material> getWhitelist(Action actionType) {
        return switch (actionType) {
            case PICKUP -> List.copyOf(pickupWhitelist);
            case DROP -> List.copyOf(dropWhitelist);
        };
    }

    public void addWhitelist(Action actionType, Material mat) {
        switch (actionType) {
            case DROP -> sync(
                    () -> dropWhitelist.add(mat), dropWhitelist,
                    "inventory-manager", "drop-whitelist"
            );
            case PICKUP -> sync(
                    () -> pickupWhitelist.add(mat), pickupWhitelist,
                    "inventory-manager", "pickup-whitelist"
            );
        }
    }

    public void removeWhitelist(Action actionType, Material mat) {
        switch (actionType) {
            case DROP -> sync(
                    () -> dropWhitelist.remove(mat), dropWhitelist,
                    "inventory-manager", "drop-whitelist"
            );
            case PICKUP -> sync(
                    () -> pickupWhitelist.remove(mat), pickupWhitelist,
                    "inventory-manager", "pickup-whitelist"
            );
        }
    }

    public void showWhitelist(Action actionType, CommandSender sender) {
        sender.sendMessage(text());
        switch (actionType) {
            case DROP -> dropWhitelist.forEach(e -> sender.sendMessage(text()
                    .append(text(" ▸ "))
                    .append(translatable(e.translationKey()))));
            case PICKUP -> pickupWhitelist.forEach(e -> sender.sendMessage(text()
                    .append(text(" ▸ "))
                    .append(translatable(e.translationKey()))));
        }
    }

    /**
     * A convenience method to enforce updating values in both class fields and the plugin config file.
     *
     * @param setter setter which sets the class fields
     * @param value  the value to be stored in the config file
     * @param path   the path to which the value to be stored in the config file
     */
    private <C extends Collection<Material>> void sync(Runnable setter, C value, Object... path) {
        setter.run();
        try {
            VDA.config().node(path).setList(Material.class, new ArrayList<>(value));
        } catch (SerializationException e) {
            LOG.reportException(e);
        }
        VDA.config().save();
    }

    public enum Action {
        DROP, PICKUP
    }
}

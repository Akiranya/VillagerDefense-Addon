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
import java.util.EnumSet;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@CustomLog
@ParametersAreNonnullByDefault
public class InventoryManager extends Module {

    private final CommentedConfigurationNode root;
    private boolean enabled;
    private EnumSet<Material> dropWhitelist;
    private EnumSet<Material> pickupWhitelist;

    public InventoryManager() {
        root = VDA.config().node("inventory-manager");

        enabled = root.node("enabled").getBoolean();
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
        enabled = true;
        VDA.instance().registerListener(this);
        LOG.info("InventoryManager is on!");
    }

    public void disable() {
        enabled = false;
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

    @EventHandler
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
            case DROP -> dropWhitelist.add(mat);
            case PICKUP -> pickupWhitelist.add(mat);
        }
    }

    public void removeWhitelist(Action actionType, Material mat) {
        switch (actionType) {
            case DROP -> dropWhitelist.remove(mat);
            case PICKUP -> pickupWhitelist.remove(mat);
        }
    }

    public void showWhitelist(Action actionType, CommandSender sender) {
        sender.sendMessage(text());
        switch (actionType) {
            case DROP -> dropWhitelist.forEach(e -> sender.sendMessage(text(" ▸ ").append(translatable(e.translationKey()))));
            case PICKUP -> pickupWhitelist.forEach(e -> sender.sendMessage(text(" ▸ ").append(translatable(e.translationKey()))));
        }
    }

    @Override
    public void saveConfig() {
        try {
            root.node("enabled").set(enabled);
            root.node("drop-whitelist").setList(Material.class, dropWhitelist.stream().toList());
            root.node("pickup-whitelist").setList(Material.class, dropWhitelist.stream().toList());
        } catch (SerializationException e) {
            LOG.error("Failed to save config: " + root.path());
        }
        VDA.config().save();
    }

    public enum Action {
        DROP, PICKUP
    }
}

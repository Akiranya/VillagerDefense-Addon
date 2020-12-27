package co.mcsky.villagedefensenhancement.modules;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class CollisionFixer implements Listener {

    private Set<EntityType> noCollisionEntities;

    public CollisionFixer() {
        // Configuration values
        try {
            noCollisionEntities = new HashSet<>(plugin.config.node("no-collision-entities", "list")
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

    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event) {
        // Don't cancel collision for wither skull
        if (event.getEntity().getType() == EntityType.WITHER_SKULL) return;

        if (noCollisionEntities.contains(event.getCollidedWith().getType())) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Penetrated!");
        }
    }

}

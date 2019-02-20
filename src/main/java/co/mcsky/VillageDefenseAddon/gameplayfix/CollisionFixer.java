package co.mcsky.VillageDefenseAddon.gameplayfix;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class CollisionFixer implements Listener {
    private final VillageDefenseAddon plugin;

    public CollisionFixer(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onProjectileCollide(final ProjectileCollideEvent event) {
        EntityType type = event.getCollidedWith().getType();
        Set<EntityType> types = new HashSet<>();
        types.add(EntityType.PLAYER);
        types.add(EntityType.VILLAGER);
        types.add(EntityType.IRON_GOLEM);
        types.add(EntityType.WOLF);
        if (types.contains(type)) {
            if (event.getEntity().getType() == EntityType.WITHER_SKULL) return;
            event.setCancelled(true);
            Bukkit.getLogger().info("穿透!");
        }
    }
}

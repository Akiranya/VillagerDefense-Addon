package co.mcsky.VillageDefenseAddon.gameplayfix;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import pl.plajer.villagedefense.api.event.game.VillageGameStartEvent;

import java.util.List;

public class VillagerNoMovement implements Listener {
    private final VillageDefenseAddon plugin;

    public VillagerNoMovement(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void cancelVillagerMovement(final VillageGameStartEvent event) {
        List<Villager> villagers = event.getArena().getVillagers();

        // 把村民的移速都降到最低
        for (Villager v : villagers) {
            v.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
        }

        // 给村民施加一个随机向量，避免他们挤在一起
        for (Villager v : villagers) {
            v.setVelocity(Vector.getRandom().normalize());
        }
    }
}

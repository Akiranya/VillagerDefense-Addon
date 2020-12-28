package co.mcsky.villagedefensenhancement.modules;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class InfiniteAnvil implements Listener {

    private final boolean unbroken;
    private final boolean noCost;

    public InfiniteAnvil() {
        // Configuration values
        unbroken = plugin.config.node("inf-anvil", "unbroken").getBoolean(true);
        noCost = plugin.config.node("inf-anvil", "no-cost").getBoolean(true);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAnvilDamage(AnvilDamagedEvent event) {
        if (!unbroken) return;
        event.setDamageState(AnvilDamagedEvent.DamageState.FULL);
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        if (!noCost) return;
        if (event.getPlayer().getOpenInventory().getType() == InventoryType.ANVIL) {
            int oldLevel = event.getOldLevel();
            if (event.getNewLevel() < oldLevel) {
                event.getPlayer().setLevel(oldLevel);
            }
        }
    }

}

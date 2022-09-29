package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;

/**
 * New Feature: don't consume levels when using anvil and unbroken anvil
 */
public class CustomAnvil extends Module {

    private final boolean unbreakable;
    private final boolean withoutCost;

    public CustomAnvil() {
        unbreakable = VDA.config().node("custom-anvil", "unbreakable").getBoolean(true);
        withoutCost = VDA.config().node("custom-anvil", "without-cost").getBoolean(true);

        registerListener();
    }

    @EventHandler
    public void onAnvilDamage(AnvilDamagedEvent event) {
        if (!unbreakable) return;
        event.setDamageState(AnvilDamagedEvent.DamageState.FULL);
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        if (!withoutCost) return;
        if (event.getPlayer().getOpenInventory().getType() == InventoryType.ANVIL) {
            int oldLevel = event.getOldLevel();
            if (event.getNewLevel() < oldLevel) {
                event.getPlayer().setLevel(oldLevel);
            }
        }
    }
}

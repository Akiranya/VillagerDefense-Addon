package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;
import plugily.projects.villagedefense.arena.Arena;

import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * This class modifies the mechanism of spawning zombies.
 */
public class MoreZombies implements Listener {

    private final Random rd;

    public MoreZombies() {
        rd = new Random();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Spawn more zombies
     */
    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        Arena arena = event.getArena();
        int extraAmount = Math.max(32, event.getWaveNumber() * event.getWaveNumber() - 100);
        for (int i = 0; i < extraAmount; i++) {
            switch (rd.nextInt(4)) {
                case 0:
                    arena.spawnFastZombie(rd);
                    break;
                case 1:
                    arena.spawnHardZombie(rd);
                    break;
                case 2:
                    arena.spawnSoftHardZombie(rd);
                    break;
                case 3:
                    arena.spawnKnockbackResistantZombies(rd);
                    break;
                default:
                    break;
            }
        }
    }

}

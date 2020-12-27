package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;
import plugily.projects.villagedefense.arena.Arena;

import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class MoreZombies implements Listener {

    private final Random rd;

    public MoreZombies() {
        rd = new Random();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        Arena arena = event.getArena();
        int more = Math.max(64, event.getWaveNumber() * event.getWaveNumber() - 100);
        for (int i = 0; i < more; i++) {
            switch (rd.nextInt(10)) {
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
                case 4:
                    arena.spawnHalfInvisibleZombie(rd);
                    break;
                case 5:
                    arena.spawnBabyZombie(rd);
                    break;
                default:
                    break;
            }
        }
    }

}

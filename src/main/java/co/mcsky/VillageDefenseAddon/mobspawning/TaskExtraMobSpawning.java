package co.mcsky.VillageDefenseAddon.mobspawning;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.api.event.wave.VillageWaveStartEvent;
import pl.plajer.villagedefense.arena.Arena;

import java.util.List;

public class TaskExtraMobSpawning extends BukkitRunnable {

    private Arena arena;
    private World world;
    private List<Location> locations;
    private int count;
    private int spawnPoint;
    private ZombieHandler zombieHandler;

    TaskExtraMobSpawning(VillageWaveStartEvent event) {
        this.arena = event.getArena();
        this.world = arena.getZombieSpawns().get(0).getWorld();
        this.locations = arena.getZombieSpawns();
        this.spawnPoint = 0;
        this.zombieHandler = new ZombieHandler();
        this.count = 0;
    }

    @Override
    public void run() {
        // Some variables to determine how many zombie to be spawned
        int playersLeft = arena.getPlayersLeft().size();
//        int zombiesLeft = arena.getZombiesLeft();
        int wave = arena.getWave();
        int randomType = (int) (Math.random() * SPECIAL_ZOMBIE.values().length);

        // Equip it!
        Zombie mob = (Zombie) world.spawnEntity(locations.get(spawnPoint), EntityType.ZOMBIE);
        zombieHandler.equipMob(mob, SPECIAL_ZOMBIE.values()[randomType]);

        // Set a random target for zombie
        List<Villager> villagers = arena.getVillagers();
        Villager villager = villagers.get((int) (Math.random() * villagers.size()));
        mob.setTarget(villager);

        Bukkit.getLogger().info("An extra mob spawned!");
        count++; // Amount of zombie

        // Loop spawn points
        spawnPoint++;
        if (spawnPoint >= locations.size() - 1) spawnPoint = 0;

        // Stops spawning extra zombies when it comes
        if (count > playersLeft * wave * wave / 20) {
            this.cancel();
            Bukkit.getLogger().info("Extra Spawning Task Cancelled.");
        }
    }

}

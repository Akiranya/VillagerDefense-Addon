package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.entity.Player;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaManager;
import plugily.projects.villagedefense.arena.ArenaRegistry;

import java.util.Set;

public class PlayerDispatcher {

    public PlayerDispatcher() {
    }

    public void sendPlayerToArena(Arena arena, Set<Player> players) {
        players.forEach(p -> ArenaManager.joinAttempt(p, arena));
    }

    public void forcePlayerQuit(Set<Player> players) {
        players.forEach(p -> {
            Arena arena = ArenaRegistry.getArena(p);
            if (arena != null)
                ArenaManager.leaveAttempt(p, arena);
        });
    }

}

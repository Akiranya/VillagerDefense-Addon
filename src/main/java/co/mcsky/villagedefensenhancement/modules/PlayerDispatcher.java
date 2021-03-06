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
        for (Player player : players) {
            Arena tempArena = ArenaRegistry.getArena(player);
            if (tempArena != null && tempArena != arena) {
                // The player is in another arena, let he leave that one first
                ArenaManager.leaveAttempt(player, tempArena);
            }
            ArenaManager.joinAttempt(player, arena);
        }
    }

    public void forcePlayerQuit(Set<Player> players) {
        for (Player player : players) {
            Arena arena = ArenaRegistry.getArena(player);
            if (arena != null) {
                ArenaManager.leaveAttempt(player, arena);
            }
        }
    }

}

package cc.mewcraft.villagedefense.module;

import org.bukkit.entity.Player;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaManager;
import plugily.projects.villagedefense.arena.ArenaRegistry;

public class PlayerDispatcher extends Module {

    public PlayerDispatcher() {
    }

    public void sendPlayerToArena(Arena arena, Player player) {
        Arena oldArena = ArenaRegistry.getArena(player);
        if (oldArena != null && oldArena != arena) {
            // The player is in another arena, let he leave that one first
            ArenaManager.leaveAttempt(player, oldArena);
        }
        ArenaManager.joinAttempt(player, arena);
    }

    public void forcePlayerQuit(Player player) {
        Arena arena = ArenaRegistry.getArena(player);
        if (arena != null) {
            ArenaManager.leaveAttempt(player, arena);
        }
    }

    @Override
    public void saveConfig() {

    }

}

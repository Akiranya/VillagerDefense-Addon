package co.mcsky.VillageDefenseAddon.features;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaManager;
import pl.plajer.villagedefense.arena.ArenaRegistry;

import java.util.HashSet;
import java.util.Set;

public class ACTManager implements CommandExecutor {

    private final VillageDefenseAddon plugin;

    public ACTManager(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("actmanager").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Not enough arguments!");
            return false;
        }

        if (args.length > 3) {
            sender.sendMessage("Too many arguments!");
            return false;
        }

        Arena arena = ArenaRegistry.getArena(args[0]);
        if (arena == null) {
            sender.sendMessage("Arena not found.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("Not enough arguments!");
            return false;
        }

        Player player;
        if (args[1].equalsIgnoreCase("join")) {
            if (args.length != 3) return false;
            if (args[2].equalsIgnoreCase("all")) {
                // Force all players to join
                Set<Player> onlinePlayersSet = new HashSet<>(Bukkit.getOnlinePlayers());

                if (onlinePlayersSet.size() == 0) {
                    sender.sendMessage("No players online.");
                    return true;
                }

                onlinePlayersSet.removeAll(arena.getPlayers());
                if (onlinePlayersSet.size() == 0) {
                    sender.sendMessage("All players are in arena.");
                    return true;
                }

                for (Player p : onlinePlayersSet) {
                    sendPlayerToArena(arena, p);
                }
                return true;
            } else {
                // Force a player to join
                player = Bukkit.getPlayer(args[2]);
                if (player != null && !arena.getPlayers().contains(player)) sendPlayerToArena(arena, player);
                else sender.sendMessage("Player is not online, or in that arena already!");
                return true;
            }
        } else if (args[1].equalsIgnoreCase("leave")) {
            if (args.length != 3) return false;
            if (args[2].equalsIgnoreCase("all")) {
                // Force all players to leave
                Set<Player> players = arena.getPlayers();
                if (players.size() > 0) {
                    for (Player p : players) {
                        forcePlayerQuit(p);
                    }
                    return true;
                } else {
                    sender.sendMessage("No players in that arena.");
                    return true;
                }
            } else {
                // Force a player to leave
                player = Bukkit.getPlayer(args[2]);
                if (player != null && arena.getPlayers().contains(player)) {
                    forcePlayerQuit(player);
                }
                else sender.sendMessage("Player is not online, or not in that arena!");
                return true;
            }
        } else {
            return false;
        }
    }

    private void sendPlayerToArena(Arena arena, Player player){
        ArenaManager.joinAttempt(player, arena);
    }

    private void forcePlayerQuit(Player player){
        Arena arena = ArenaRegistry.getArena(player);
        ArenaManager.leaveAttempt(player, arena);
    }
}

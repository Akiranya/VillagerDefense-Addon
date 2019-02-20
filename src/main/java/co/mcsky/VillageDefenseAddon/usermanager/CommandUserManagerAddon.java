package co.mcsky.VillageDefenseAddon.usermanager;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.plajer.villagedefense.api.StatsStorage;
import pl.plajer.villagedefense.user.User;
import pl.plajer.villagedefense.user.UserManager;

public class CommandUserManagerAddon implements CommandExecutor {
    private final VillageDefenseAddon plugin;
    private final UserManager userManager;

    public CommandUserManagerAddon(VillageDefenseAddon plugin, UserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.plugin.getCommand("usermanager").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 2) {
            sender.sendMessage("Too many arguments!");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage("Not enough arguments!");
            return false;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            Player player;
            if (args.length != 2) {
                sender.sendMessage("Not enough arguments!");
                return false;
            } else {
                player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Specified player is not online!");
                    return false;
                } else {
                    User user = userManager.getUser(player);
                    for (StatsStorage.StatisticType s : StatsStorage.StatisticType.values()) {
                        user.setStat(s, 0);
                        userManager.saveStatistic(user, s);
                        sender.sendMessage(s + " deleted.");
                    }
                    return true;
                }
            }
        }

        if (args[0].equalsIgnoreCase("view")) {
            Player player;
            if (args.length != 2) {
                sender.sendMessage("Not enough arguments!");
                return false;
            } else {
                player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Specified player is not online!");
                    return false;
                } else {
                    User user = userManager.getUser(player);
                    for (StatsStorage.StatisticType s : StatsStorage.StatisticType.values()) {
                        userManager.loadStatistic(user, s);
                        sender.sendMessage(s + " : " + user.getStat(s));
                    }
                    return true;
                }
            }
        }

        return false;
    }
}

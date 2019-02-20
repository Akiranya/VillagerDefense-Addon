package co.mcsky.VillageDefenseAddon.mobspawning;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CommandSpawnSpecialMob implements CommandExecutor {

    private final VillageDefenseAddon plugin;

    public CommandSpawnSpecialMob(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("specialmob").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only in-game player can use this command.");
            return true;
        }

        if (args.length > 2 || args.length < 1) {
            sender.sendMessage("Not enough arguments.");
            return false;
        }

        Player player = (Player) sender;

        try {
            if (args.length == 1) {
                spawnSpecificMob(player, args[0]);
                return true;
            }

            int amount = Math.min(Integer.valueOf(args[1]), 10);
            for (int i = 0; i < amount; i++) {
                spawnSpecificMob(player, args[0]);
            }
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(args[0] + " is not a valid number.");
            return false;
        } catch (IllegalArgumentException | NullPointerException e) {
            player.sendMessage(e.getMessage());
            return false;
        }
    }

    private void spawnSpecificMob(Player player, String arg) {
        Location loc;
        Block block = player.getTargetBlock(32);
        if (block == null) {
            player.sendMessage("Your target block is too far.");
            return;
        } else {
            loc = block.getLocation();
        }

        // In case mob stuck underground
        loc.setY(loc.getY() + 2);

        SPECIAL_ZOMBIE type = SPECIAL_ZOMBIE.valueOf(arg.toUpperCase());
        LivingEntity livingEntity = (LivingEntity) player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        ZombieHandler specialZombie = new ZombieHandler();
        specialZombie.equipMob(livingEntity, type);
    }
}

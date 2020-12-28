package co.mcsky.villagedefensenhancement;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.mcsky.villagedefensenhancement.modules.InventoryManager;
import co.mcsky.villagedefensenhancement.modules.PlayerDispatcher;
import co.mcsky.villagedefensenhancement.modules.SmartLoot;
import co.mcsky.villagedefensenhancement.objects.VillagerPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugily.projects.villagedefense.arena.Arena;

@CommandAlias("vde")
@CommandPermission("vde.mod")
public class CommandHandler extends BaseCommand {

    @Dependency SmartLoot smartLoot;
    @Dependency PlayerDispatcher gameManager;
    @Dependency InventoryManager invManager;
    @Dependency VillageDefenseEnhancement plugin;

    @HelpCommand
    public void help(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage("已重新载入!");
    }

    @Subcommand("loot")
    public class SmartLootCommand extends BaseCommand {

        @Subcommand("melee")
        public void viewMelee(CommandSender sender) {
            sender.sendMessage("当前近战攻击获得经验: " + smartLoot.getMeleeExp());
        }

        @Subcommand("melee set")
        @CommandCompletion("@nothing")
        public void setMeleeExp(CommandSender sender, int exp) {
            smartLoot.setMeleeExp(exp);
            sender.sendMessage("已设置近战攻击获得经验: " + exp);
        }

        @Subcommand("range set")
        @CommandCompletion("@nothing")
        public void setRangeExp(CommandSender sender, int exp) {
            smartLoot.setRangeExp(exp);
            sender.sendMessage("已设置远程攻击获得经验: " + exp);
        }

        @Subcommand("bound set")
        @CommandCompletion("@nothing")
        public void setDamageLowerBound(CommandSender sender, int lowerBound) {
            smartLoot.setDamageLowerBound(lowerBound);
            sender.sendMessage("已设置获得经验所需最小伤害: " + lowerBound);
        }

    }

    @Subcommand("inv")
    public class InvManagerCommand extends BaseCommand {

        @Subcommand("on")
        public void dropOn(CommandSender sender) {
            invManager.enable();
            sender.sendMessage("控制物品拾起/丢弃: 开");
        }

        @Subcommand("off")
        public void dropOff(CommandSender sender) {
            invManager.disable();
            sender.sendMessage("控制物品拾起/丢弃: 关");
        }

        @Subcommand("drop list")
        public void dropListShow(CommandSender sender) {
            sender.sendMessage("-------------------------");
            invManager.dropListShow(sender);
        }

        @Subcommand("drop add")
        public void dropAddWhitelist(@Flags("itemheld") Player player) {
            player.sendMessage("-------------------------");
            invManager.dropAddWhitelist(player.getInventory().getItemInMainHand().getType());
            invManager.dropListShow(player);
        }

        @Subcommand("drop remove")
        public void dropRemoveWhitelist(@Flags("itemheld") Player player) {
            player.sendMessage("-------------------------");
            invManager.dropRemoveWhitelist(player.getInventory().getItemInMainHand().getType());
            invManager.dropListShow(player);
        }

        @Subcommand("pickup list")
        public void pickupListShow(CommandSender sender) {
            sender.sendMessage("-------------------------");
            invManager.pickupListShow(sender);
        }

        @Subcommand("pickup add")
        public void pickupAddWhitelist(@Flags("itemheld") Player player) {
            player.sendMessage("-------------------------");
            invManager.pickupAddWhitelist(player.getInventory().getItemInMainHand().getType());
            invManager.pickupListShow(player);
        }

        @Subcommand("pickup remove")
        public void pickupRemoveWhitelist(@Flags("itemheld") Player player) {
            player.sendMessage("-------------------------");
            invManager.pickupRemoveWhitelist(player.getInventory().getItemInMainHand().getType());
            invManager.pickupListShow(player);
        }

    }

    @Subcommand("game")
    public class GameManagerCommand extends BaseCommand {

        @Subcommand("forcejoin")
        @CommandCompletion("@arenas @players")
        public void joinPlayer(CommandSender sender, Arena arena, VillagerPlayer players) {
            gameManager.sendPlayerToArena(arena, players.getValue());
        }

        @Subcommand("forceleave")
        @CommandCompletion("@players")
        public void leavePlayer(CommandSender sender, VillagerPlayer players) {
            gameManager.forcePlayerQuit(players.getValue());
        }

    }

}

package co.mcsky.VillageDefenseAddon.gameplayfix;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.api.event.game.VillageGameJoinAttemptEvent;
import pl.plajer.villagedefense.api.event.player.VillagePlayerChooseKitEvent;
import pl.plajer.villagedefense.kits.kitapi.KitRegistry;
import pl.plajer.villagedefense.kits.kitapi.basekits.Kit;
import pl.plajer.villagedefense.user.User;
import pl.plajer.villagedefense.user.UserManager;

public class ChooseKitAddon implements Listener {
    private final VillageDefenseAddon plugin;

    public ChooseKitAddon(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void giveSomeTips(VillagePlayerChooseKitEvent e) {
        Player player = e.getPlayer();
        String kit = e.getKit().getName();

        if (kit.contains("骑士")) {
            player.sendMessage(ChatColor.GRAY + "温馨提示: 骑士乃最弱职业，我真的劝大家不要用这个辣鸡职业。");
            e.setCancelled(true);
        }

        if (kit.contains("步枪手") || kit.contains("弓箭手")) {
            player.sendMessage(ChatColor.AQUA + "温馨提示: 远程职业的子弹/箭可以穿过友军噢！不会被友军格挡。");
        }

    }

    @EventHandler
    public void setDefaultKitForPlayers(final VillageGameJoinAttemptEvent event) {
        final UserManager userManager = JavaPlugin.getPlugin(Main.class).getUserManager();
        Kit defaultKit = null;

        for (Kit kit : KitRegistry.getKits()) {
            if (kit.getName().contains("剑士")) {
                defaultKit = kit;
            }
        }

        assert defaultKit != null;
        final Kit finalDefaultKit = defaultKit;
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                User user = userManager.getUser(player);
                user.setKit(finalDefaultKit);
            }
        }.runTaskLater(plugin, 20);
    }

}

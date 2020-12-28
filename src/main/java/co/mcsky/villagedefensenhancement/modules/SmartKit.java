package co.mcsky.villagedefensenhancement.modules;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.api.event.game.VillageGameLeaveAttemptEvent;
import plugily.projects.villagedefense.api.event.game.VillageGameStopEvent;
import plugily.projects.villagedefense.api.event.player.VillagePlayerChooseKitEvent;
import plugily.projects.villagedefense.kits.basekits.PremiumKit;

import java.util.List;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * A class improving stuff about kits.
 */
public class SmartKit implements Listener {

    private int levelRequired;
    private List<String> blacklist;
    private List<String> noTagPlayers;

    public SmartKit() {
        try {
            levelRequired = plugin.config.node("smart-kit", "premium-kit-level-required").getInt(12);
            blacklist = plugin.config.node("smart-kit", "blacklist").getList(String.class, () -> List.of("骑士"));
            noTagPlayers = plugin.config.node("smart-kit", "no-tag-players").getList(String.class, () -> List.of("ChesNez"));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChooseKit(VillagePlayerChooseKitEvent e) {
        String kit = e.getKit().getName();
        Player player = e.getPlayer();

        // Do not allow to select kits in the blacklist
        for (String s : blacklist) {
            if (kit.contains(s)) {
                e.setCancelled(true);
                player.sendMessage(plugin.getMessage(player, "smart-kit.kit-in-blacklist"));
                return;
            }
        }

        // Make (all) PremiumKit like LevelKit!
        if (e.getKit() instanceof PremiumKit) {
            int userLevel = StatsStorage.getUserStats(player, StatsStorage.StatisticType.LEVEL);
            if (userLevel < levelRequired && !player.isOp()) {
                e.setCancelled(true);
                player.sendMessage(plugin.getMessage(player, "smart-kit.premium-level-required",
                                                     "level_required", levelRequired,
                                                     "current_level", userLevel));
            }
        }

        // Display name of kit above the player's head
        if (!noTagPlayers.contains(player.getName())) {
            NametagEdit.getApi().setPrefix(player, plugin.getMessage(player, "smart-kit.tag-format", "kit", kit));
        }
    }

    @EventHandler
    public void onArenaEnd(VillageGameStopEvent event) {
        for (Player player : event.getArena().getPlayers()) {
            NametagEdit.getApi().clearNametag(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(VillageGameLeaveAttemptEvent event) {
        NametagEdit.getApi().clearNametag(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        NametagEdit.getApi().clearNametag(event.getPlayer());
    }

    /**
     * Remove all potion effects upon death.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP);
    }

}

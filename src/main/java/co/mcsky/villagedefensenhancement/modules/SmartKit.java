package co.mcsky.villagedefensenhancement.modules;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.api.event.player.VillagePlayerChooseKitEvent;
import plugily.projects.villagedefense.kits.basekits.PremiumKit;
import plugily.projects.villagedefense.kits.level.RunnerKit;

import java.util.List;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.api;
import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * A class improving stuff about kits.
 */
public class SmartKit implements Listener {

    private int levelRequired;
    private List<String> blacklist;

    public SmartKit() {
        try {
            levelRequired = plugin.config.node("better-kit-selection", "premium-kit-level-required").getInt(12);
            blacklist = plugin.config.node("better-kit-selection", "blacklist")
                                     .getList(String.class, () -> List.of("骑士"));
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
                player.sendMessage(plugin.getMessage(player, "smart-kit-selection.kit-in-blacklist"));
                return;
            }
        }

        // Make (all) PremiumKit like LevelKit!
        if (e.getKit() instanceof PremiumKit) {
            int userLevel = StatsStorage.getUserStats(player, StatsStorage.StatisticType.LEVEL);
            if (userLevel < levelRequired && !player.isOp()) {
                e.setCancelled(true);
                player.sendMessage(plugin.getMessage(player, "smart-kit-selection.premium-level-required",
                                                     "level_required", levelRequired,
                                                     "current_level", userLevel));
            }
        }
    }

    /**
     * Remove Runner's potion effects if the player is not of Runner
     */
    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if (!(api.getUserManager().getUser(player).getKit() instanceof RunnerKit)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP);
        }
    }

}

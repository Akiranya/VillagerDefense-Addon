package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import lombok.CustomLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.api.event.game.VillageGameStopEvent;
import plugily.projects.villagedefense.api.event.wave.VillageWaveEndEvent;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;
import plugily.projects.villagedefense.handlers.language.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Assume that there is always only one arena running, otherwise bugs happen!
 */
@SuppressWarnings("FieldCanBeLocal")
@CustomLog
public class RewardManager extends Module {

    private final CommentedConfigurationNode root;

    private final Map<UUID, Double> damageStats;

    public RewardManager() {
        root = VDA.config().node("reward-manager");

        damageStats = new HashMap<>();

        registerListener();
    }

    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        Set<Player> players = event.getArena().getPlayers();
        for (Player player : players) {
            damageStats.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onWaveEnd(VillageWaveEndEvent event) {

        String arenaId = event.getArena().getId();
        Set<Player> players = event.getArena().getPlayers();
        List<Player> playersLeft = event.getArena().getPlayersLeft();
        int waveNumber = event.getWaveNumber() - 1; // the API always returns real wave number + 1, so we have to minus 1 to get the correct number

        // ---- Give exp to players based on damage when wave ends ----

        CommentedConfigurationNode damageRewardsNode = root.node(arenaId, "damageRewards");
        if (damageRewardsNode.node("enabled").getBoolean()) {
            int unitExp = damageRewardsNode.node("unitExp").getInt();
            int unitDamage = damageRewardsNode.node("unitDamage").getInt();
            for (Player player : players) {
                double damage = damageStats.getOrDefault(player.getUniqueId(), 0D);
                int unitExpReward = 0;
                int totalExpReward = 0;
                try {
                    unitExpReward = (int) (damage / unitDamage * unitExp);
                    totalExpReward = unitExpReward * event.getArena().getVillagers().size();
                } catch (ArithmeticException e) {
                    LOG.reportException(e);
                }
                VDA.api().getChatManager().formatMessage(
                        event.getArena(),
                        VDA.lang().legacy(
                                "msg_damage_summary_when_wave_ends",
                                "wave-number", Integer.toString(waveNumber),
                                "damage-done", Integer.toString((int) damage),
                                "unit-exp", Integer.toString(unitExpReward),
                                "total-exp", Integer.toString(totalExpReward)
                        ),
                        player
                );
                player.giveExp(totalExpReward);
                VDA.api().getUserManager().getUser(player).addStat(StatsStorage.StatisticType.ORBS, totalExpReward);
                player.sendMessage(VDA.api().getChatManager().colorMessage(Messages.ORBS_PICKUP).replace("%number%", Integer.toString(totalExpReward)));
            }
        }

        // ---- Run certain commands when wave ends ----

        CommentedConfigurationNode endWaveNode = root.node(arenaId, "endWave");
        if (endWaveNode.isMap()) {
            try {
                List<String> commandStrings = endWaveNode.node(waveNumber).getList(String.class);
                if (commandStrings != null) {
                    for (String str : commandStrings) {
                        if (str.startsWith("single:")) {
                            // Only run this command once
                            // Support placeholders:
                            //   %wave%
                            //   %map-name%
                            //   %arena-id%
                            //   %player-amount%
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str
                                    .substring(7)
                                    .replace("%wave%", Integer.toString(waveNumber))
                                    .replace("%map-name%", event.getArena().getMapName())
                                    .replace("%player-amount%", Integer.toString(players.size()))
                                    .replace("%player-left%", Integer.toString(playersLeft.size()))
                            );
                        } else {
                            // Run this command for every player
                            // Support placeholders:
                            //   %wave%
                            //   %map-name%
                            //   %arena-id%
                            //   %player-amount%
                            //   %player%
                            for (Player player : players) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str
                                        .replace("%wave%", Integer.toString(waveNumber))
                                        .replace("%map-name%", event.getArena().getMapName())
                                        .replace("%arena-id%", arenaId)
                                        .replace("%player-amount%", Integer.toString(players.size()))
                                        .replace("%player-left%", Integer.toString(playersLeft.size()))
                                        .replace("%player%", player.getName())
                                );
                            }
                        }
                    }
                }
            } catch (SerializationException e) {
                LOG.reportException(e);
            }
        }
    }

    @EventHandler
    public void onGameEnd(VillageGameStopEvent event) {
        String arenaId = event.getArena().getId();
        CommentedConfigurationNode endGameNode = root.node(arenaId, "endGame");
        if (endGameNode.isList()) {
            try {
                List<String> commandStrings = endGameNode.getList(String.class);
                if (commandStrings != null) {
                    for (String str : commandStrings) {
                        if (str.startsWith("single:")) {
                            // Only run this command once
                            // Support placeholders:
                            //   %map-name%
                            //   %arena-id%
                            //   %player-amount%
                            //   %player-left%
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str
                                    .substring(7)
                                    .replace("%map-name%", event.getArena().getMapName())
                                    .replace("%arena-id%", event.getArena().getId())
                                    .replace("%player-amount%", Integer.toString(event.getArena().getPlayers().size()))
                                    .replace("%player-left%", Integer.toString(event.getArena().getPlayersLeft().size()))
                            );
                        } else {
                            // Run this command for every player
                            // Support placeholders:
                            //   %map-name%
                            //   %arena-id%
                            //   %player-amount%
                            //   %player%
                            for (Player player : event.getArena().getPlayers()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str
                                        .replace("%map-name%", event.getArena().getMapName())
                                        .replace("%arena-id%", event.getArena().getId())
                                        .replace("%player-amount%", Integer.toString(event.getArena().getPlayers().size()))
                                        .replace("%player-left%", Integer.toString(event.getArena().getPlayersLeft().size()))
                                        .replace("%player%", player.getName())
                                );
                            }
                        }
                    }
                }
            } catch (SerializationException e) {
                LOG.reportException(e);
            }
        }
    }

    /**
     * Count damage done by the players to the zombie.
     */
    @EventHandler
    public void onZombieDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Monster &&
            event.getDamager() instanceof Player player) {
            damageStats.compute(player.getUniqueId(),
                    (uuid, damage) -> damage == null
                            ? event.getFinalDamage()
                            : damage + event.getFinalDamage()
            );
        }
    }

    @Override
    public void saveConfig() {

    }

}

package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import lombok.CustomLog;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.wave.VillageWaveEndEvent;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;

import java.util.EnumSet;
import java.util.List;

import static org.bukkit.entity.EntityType.ARROW;
import static org.bukkit.entity.EntityType.IRON_GOLEM;
import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.entity.EntityType.SPECTRAL_ARROW;
import static org.bukkit.entity.EntityType.WOLF;

/**
 * Assume that there is always only one arena running, otherwise bugs happen!
 */
@CustomLog
public class RewardManager extends Module {

    @Getter private double damageDivisor;
    @Getter private double totalDamageDealt;
    private EnumSet<EntityType> damageIncludedEntities;

    public RewardManager() {
        CommentedConfigurationNode root = VDA.config().node("reward-manager");

        // A damage divisor, used to convert into rewards
        damageDivisor = root.node("damage-divisor").getDouble(1000);

        // What types of entity should be included to the total damage calculation
        CommentedConfigurationNode node1 = root.node("damage-included-entities");
        try {
            damageIncludedEntities = EnumSet.copyOf(node1.getList(EntityType.class, List.of(
                    PLAYER, WOLF, IRON_GOLEM, ARROW, SPECTRAL_ARROW
            )));
        } catch (SerializationException e) {
            damageIncludedEntities = EnumSet.noneOf(EntityType.class);
            LOG.warn("Failed to read config: " + node1.path() + ". No damage will be logged!", e);
        }

        registerListener();
    }

    public void setDamageDivisor(double damageDivisor) {
        sync(() -> this.damageDivisor = damageDivisor, damageDivisor, "reward-manager", "damage-divisor");
    }

    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        totalDamageDealt = 0;
    }

    @EventHandler
    public void onWaveEnd(VillageWaveEndEvent event) {
        // Take the ceil to ensure at least 1 exp bottle
        final int bottleAmount = (int) Math.ceil(totalDamageDealt / damageDivisor);
        VDA.api().getChatManager().broadcastMessage(event.getArena(), VDA.lang().legacy(
                "msg_damage_summary_when_wave_ends",
                "wave-number", Integer.toString(event.getWaveNumber() - 1),
                "damage-done", Integer.toString((int) totalDamageDealt),
                "bottle-amount", Integer.toString(bottleAmount)
        ));
        // Make villagers glow to help players find them
        event.getArena().getVillagers().forEach(v -> v.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 1)));

        // Create exp bottles!
        Schedulers.sync().runRepeating(task -> {
            if (task.getTimesRan() > bottleAmount) {
                task.stop();
                return;
            }
            for (Villager villager : event.getArena().getVillagers()) {
                villager.launchProjectile(ThrownExpBottle.class, Vector.getRandom());
            }
            // Set the period so that all exp bottles are thrown within 30 sec
        }, 20 * 5, (long) Math.max(1D, 25D / bottleAmount * 20D));
    }

    /**
     * Count damage done by the players to the zombie.
     */
    @EventHandler
    public void onZombieDamageByPlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getEntity() instanceof Zombie && damageIncludedEntities.contains(damager.getType())) {
            totalDamageDealt += event.getFinalDamage();
        }
    }

    /**
     * If the zombie is healed, invalidate equal amount from the total damage done by players. This prevents the case
     * where the player could farm coins by healing and damaging zombies back and forth.
     */
    @EventHandler
    public void onZombieRegainHealthByPlayer(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Zombie) {
            totalDamageDealt -= event.getAmount();
        }
    }

    /**
     * A convenience method to enforce updating values in both class fields and the plugin config files.
     *
     * @param setter setter which sets the class fields
     * @param value  the value to be stored in the config file
     * @param path   the path to which the value to be stored in the config file
     */
    private void sync(Runnable setter, Object value, Object... path) {
        setter.run();
        try {
            VDA.config().node(path).set(value);
        } catch (SerializationException e) {
            LOG.reportException(e);
        }
        VDA.config().save();
    }
}

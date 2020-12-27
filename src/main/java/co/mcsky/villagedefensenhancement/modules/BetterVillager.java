package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.game.VillageGameStartEvent;

import java.util.List;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public class BetterVillager implements Listener {

    private VillageGameStartEvent gameEvent;

    private List<String> cryMessages;
    private int messageIndex;

    public BetterVillager() {
        // Configuration values
        try {
            cryMessages = plugin.config.node("better-villager", "cry-messages")
                                       .getList(String.class, () ->
                                               List.of("村民 -> 有只僵尸在打我，快来人救救我！",
                                                       "村民 -> 我快被僵尸打死啦，快来人呐！",
                                                       "村民 -> 僵尸大军冲进来了，救命呐！"));
        } catch (SerializationException e) {
            plugin.getLogger().severe(e.getMessage());
            return;
        }

        // Register this event
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void initEvent(VillageGameStartEvent event) {
        gameEvent = event;
    }

    /**
     * Prompt players if there is any villager on damage.
     */
    @EventHandler
    public void remindOnDamage(EntityDamageByEntityEvent event) {
        if (gameEvent == null) return;

        Entity entity = event.getEntity();
        if (entity instanceof Villager) {
            // Only remind if the damager is a zombie or wither
            Entity damager = event.getDamager();
            if (damager instanceof Monster && event.getFinalDamage() > 1) {
                ((Villager) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 1));
                String message = cryMessages.get(messageIndex++ % cryMessages.size());
                gameEvent.getArena().getPlayers().forEach(p -> p.sendActionBar(message));
            }
        }
    }

    /**
     * Stop villagers from going through doors.
     */
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Villager) {
            Villager v = (Villager) entity;
            // Make it unable to open/go through doors
            v.getPathfinder().setCanPassDoors(false);
            v.getPathfinder().setCanOpenDoors(false);
        }
    }

}

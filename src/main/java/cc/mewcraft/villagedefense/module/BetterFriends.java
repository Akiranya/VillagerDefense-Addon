package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import lombok.CustomLog;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.utils.Players;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import plugily.projects.villagedefense.api.event.player.VillagePlayerEntityUpgradeEvent;

import java.util.EnumSet;
import java.util.List;

import static org.bukkit.entity.EntityType.IRON_GOLEM;
import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.entity.EntityType.VILLAGER;
import static org.bukkit.entity.EntityType.WITHER;
import static org.bukkit.entity.EntityType.WOLF;
import static org.bukkit.entity.EntityType.ZOMBIE;

/**
 * This class improves the interaction with friendly creatures.
 */
@CustomLog
public class BetterFriends extends Module {

    private final int healPotionCount;
    private EnumSet<EntityType> noCollisionEntities;
    private EnumSet<EntityType> villagerHurtEntities;
    private final int villagerHurtGlowingDuration;

    public BetterFriends() {
        CommentedConfigurationNode root = VDA.config().node("better-friends");

        // Spawn healing potions when upgrading an entity
        healPotionCount = root.node("healing-potion-count-when-upgrade").getInt(10);

        // Projectiles from players can pass through specific entities
        var node1 = root.node("projectile-collision-exempt-entities");
        try {
            noCollisionEntities = EnumSet.copyOf(node1.getList(EntityType.class, List.of(PLAYER, VILLAGER, IRON_GOLEM, WOLF)));
        } catch (SerializationException e) {
            noCollisionEntities = EnumSet.noneOf(EntityType.class);
            LOG.warn("Failed to read config: " + node1.path() + ". Projectile collision exempt will not work!", e);
        }

        // Broadcast villager being taken damage for specific entities
        var node2 = root.node("villager-damage-broadcast-entities");
        try {
            villagerHurtEntities = EnumSet.copyOf(node2.getList(EntityType.class, List.of(ZOMBIE, WITHER)));
        } catch (SerializationException e) {
            villagerHurtEntities = EnumSet.noneOf(EntityType.class);
            LOG.warn("Failed to read config: " + node2.path() + ". Broadcasting villager being taken damage will not work!", e);
        }

        // How long the villager glows when taken damage
        villagerHurtGlowingDuration = root.node("villager-hurt-glowing-duration").getInt(200);

        // Register this event
        registerListener();
    }

    /**
     * New Feature: prompt players if there is any villager on damage.
     */
    @EventHandler
    public void onVillagerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            if (villagerHurtEntities.contains(event.getDamager().getType()) && event.getFinalDamage() > 1) {
                villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, villagerHurtGlowingDuration, 1));
                Players.forEach(p -> p.sendActionBar(
                        VDA.lang().component(p,
                                "msg_villager_being_hurt",
                                "damagee", villager.getCustomName()
                        )));
            }
        }
    }

    /**
     * New Feature: stop villagers from opening/passing doors.
     */
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Villager villager) {
            villager.getPathfinder().setCanPassDoors(false);
            villager.getPathfinder().setCanOpenDoors(false);
        }
    }

    /**
     * New Feature: allow players to leash villagers (this should mimic the vanilla behaviors as if leashing/unleashing
     * a cow, for example).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashFriendlyCreature(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getEntity() instanceof Villager &&
            player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
            player.getOpenInventory().close(); // Close the shop menu
        }
    }

    /**
     * Bug Fix: prevent wolves from sitting.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickWolf(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Wolf) {
            event.setCancelled(true);
        }
    }

    /**
     * New Feature: projectiles from players can pass through friendly creatures.
     */
    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event) {
        if (noCollisionEntities.contains(event.getCollidedWith().getType())) {
            event.setCancelled(true);
        }
    }

    /**
     * New Feature: heal the entity when it is being upgraded.
     */
    @EventHandler
    public void onEntityUpgrade(VillagePlayerEntityUpgradeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack item = ItemStackBuilder.of(Material.SPLASH_POTION)
                    .transformMeta(meta -> {
                        PotionMeta potionMeta = (PotionMeta) meta;
                        potionMeta.setColor(Color.RED);
                        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 10, 2), true);
                    })
                    .build();

            Schedulers.sync().runRepeating(task -> {
                if (task.getTimesRan() >= healPotionCount) {
                    task.stop();
                    return;
                }

                // Throw heal potions onto the entity
                Vector feetV = entity.getLocation().toVector();
                Vector eyeV = livingEntity.getEyeLocation().toVector();
                ThrownPotion thrownPotion = livingEntity.launchProjectile(ThrownPotion.class, feetV.subtract(eyeV));
                thrownPotion.setItem(item);
            }, 20L, 30L);
        }
    }
}

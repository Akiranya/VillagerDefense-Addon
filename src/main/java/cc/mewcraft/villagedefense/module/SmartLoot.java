package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import lombok.CustomLog;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Random;

@CustomLog
public class SmartLoot extends Module {

    @Getter private int meleeVanillaExp;
    @Getter private int rangedVanillaExp;
    @Getter private int meleeKitExp;
    @Getter private int rangedKitExp;
    @Getter private double minimumDamageRequirement;

    public SmartLoot() {
        // Configuration values
        CommentedConfigurationNode root = VDA.config().node("smart-loot");
        meleeVanillaExp = root.node("melee-vanilla-exp").getInt(1);
        meleeKitExp = root.node("melee-kit-exp").getInt(1);
        rangedVanillaExp = root.node("ranged-vanilla-exp").getInt(2);
        rangedKitExp = root.node("ranged-kit-exp").getInt(2);
        minimumDamageRequirement = root.node("minimum-damage-requirement").getDouble(2D);

        // Register this listener
        registerListener();
    }

    public void setMeleeVanillaExp(int meleeVanillaExp) {
        sync(() -> this.meleeVanillaExp = meleeVanillaExp, meleeVanillaExp, "smart-loot", "melee-vanilla-exp");
    }

    public void setRangedVanillaExp(int rangedVanillaExp) {
        sync(() -> this.rangedVanillaExp = rangedVanillaExp, rangedVanillaExp, "smart-loot", "range-vanilla-exp");
    }

    public void setMeleeKitExp(int meleeKitExp) {
        sync(() -> this.meleeKitExp = meleeKitExp, meleeKitExp, "smart-loot", "melee-kit-exp");
    }

    public void setRangedKitExp(int rangedKitExp) {
        sync(() -> this.rangedKitExp = rangedKitExp, rangedKitExp, "smart-loot", "ranged-kit-exp");
    }

    public void setMinimumDamageRequirement(double minimumDamageRequirement) {
        sync(() -> this.minimumDamageRequirement = minimumDamageRequirement, minimumDamageRequirement, "smart-loot", "minimum-damage-requirement");
    }

    /**
     * New Feature: give exp and level to attacker on each damage.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof Monster && event.getDamage() > minimumDamageRequirement) {

            if (damager instanceof Player player) {
                // Give exp & level to melee attacker

                player.giveExp(meleeVanillaExp);
                VDA.api().getUserManager().addExperience(player, meleeKitExp);
                player.sendActionBar(VDA.lang().component(player,
                        "msg_gained_experience",
                        "exp", Double.toString(meleeVanillaExp)));

            } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                // Give exp & level to ranged attacker

                player.giveExp(rangedVanillaExp);
                VDA.api().getUserManager().addExperience(player, rangedKitExp);
                player.sendActionBar(VDA.lang().component(player,
                        "msg_gained_experience",
                        "exp", Double.toString(rangedVanillaExp)));

                // Give loots if the damaged monster is dead
                Schedulers.sync().run(() -> {
                    if (entity.isDead()) {
                        LootContext lootContext = new LootContext.Builder(entity.getLocation())
                                .killer(player)
                                .lootedEntity(entity)
                                .build();
                        LootTable lootTable = ((Monster) entity).getLootTable();
                        if (lootTable != null) {
                            lootTable.populateLoot(new Random(), lootContext).forEach(item -> player.getInventory().addItem(item));
                        }
                    }
                });
            }
        }
    }

    /**
     * A convenience method to enforce updating values in both class fields and the plugin config file.
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

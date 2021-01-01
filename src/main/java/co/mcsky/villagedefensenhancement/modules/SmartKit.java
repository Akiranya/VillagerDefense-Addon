package co.mcsky.villagedefensenhancement.modules;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.api.event.game.VillageGameLeaveAttemptEvent;
import plugily.projects.villagedefense.api.event.game.VillageGameStopEvent;
import plugily.projects.villagedefense.api.event.player.VillagePlayerChooseKitEvent;
import plugily.projects.villagedefense.kits.KitRegistry;
import plugily.projects.villagedefense.kits.basekits.FreeKit;
import plugily.projects.villagedefense.kits.basekits.Kit;
import plugily.projects.villagedefense.kits.basekits.PremiumKit;
import plugily.projects.villagedefense.kits.level.HardcoreKit;
import plugily.projects.villagedefense.kits.level.MediumTankKit;
import plugily.projects.villagedefense.kits.premium.HeavyTankKit;
import plugily.projects.villagedefense.kits.premium.PremiumHardcoreKit;
import plugily.projects.villagedefense.user.UserManager;

import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.api;
import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * A class improving stuff about kits.
 */
public class SmartKit implements Listener {

    private final Random rd;
    private final int levelRequired;
    private final float superArrowChance;

    public SmartKit() {
        rd = new Random();

        // Set default kit to Light Tank
        KitRegistry.setDefaultKit((FreeKit) KitRegistry.getKit(new ItemStack(Material.LEATHER_CHESTPLATE)));

        // Configuration values
        levelRequired = plugin.config.node("smart-kit", "premium-kit-level-required").getInt(12);
        superArrowChance = plugin.config.node("smart-kit", "super-arrow-chance").getFloat(0.25F);

        // Register this listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChooseKit(VillagePlayerChooseKitEvent event) {
        Kit kit = event.getKit();
        Player player = event.getPlayer();

        if (!kit.isUnlockedByPlayer(player)) {
            return;
        }

        // New Feature: make (all) PremiumKit like LevelKit!
        if (event.getKit() instanceof PremiumKit) {
            int userLevel = StatsStorage.getUserStats(player, StatsStorage.StatisticType.LEVEL);
            if (userLevel < levelRequired && !player.isOp()) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessage(player, "smart-kit.premium-level-required",
                                                     "level-required", levelRequired,
                                                     "current-level", userLevel));
                return;
            }
        }

        // New Feature: show the kit name above the player's head
        NametagEdit.getApi().setPrefix(player, plugin.getMessage(player, "smart-kit.tag-format", "kit", kit.getName()));

        // Bug Fix: set max health to default (20) if currently selected kit doesn't modify max health
        UserManager userManager = api.getUserManager();
        if (userManager.getUser(player).isSpectator()) {
            if (!(userManager.getUser(player).getKit() instanceof MediumTankKit ||
                  userManager.getUser(player).getKit() instanceof HeavyTankKit ||
                  userManager.getUser(player).getKit() instanceof HardcoreKit ||
                  userManager.getUser(player).getKit() instanceof PremiumHardcoreKit)) {
                //noinspection ConstantConditions
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20D);
            }
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
     * Bug Fix: Remove all potion effects upon death to prevent some "residual
     * effects" from the player's previous kit.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (PotionEffectType effect : PotionEffectType.values()) {
            player.removePotionEffect(effect);
        }
    }

    /**
     * New Feature: There are chances to shoot super arrows!
     */
    @EventHandler
    public void onBowShootArrow(EntityShootBowEvent event) {
        // All bows are infinite!
        ItemStack bow = event.getBow();
        if (bow != null && !bow.containsEnchantment(Enchantment.ARROW_INFINITE)) {
            bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        }

        if (event.getForce() >= 0.9 && rd.nextFloat() < superArrowChance) {
            // Shoot splash potion!
            Entity projectile = event.getProjectile();
            ThrownPotion thrownPotion = projectile.getWorld().spawn(projectile.getLocation(), ThrownPotion.class);

            ItemStack itemPotion = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) itemPotion.getItemMeta();
            meta.setColor(Color.RED);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 3), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 10, 4), true);
            itemPotion.setItemMeta(meta);

            thrownPotion.setItem(itemPotion);
            thrownPotion.setVelocity(projectile.getVelocity());
            event.setProjectile(thrownPotion);
        }
    }

}

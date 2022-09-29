package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class BetterShooter extends Module {
    private final float superArrowChance;

    public BetterShooter() {
        superArrowChance = VDA.config().node("better-shooter", "super-arrow-chance").getFloat(0.25F);

        // Register this listener
        registerListener();
    }

    /**
     * New Feature: There are chances to shoot super arrows!
     */
    @EventHandler
    public void onBowShootArrow(EntityShootBowEvent event) {
        ItemStack bow = event.getBow();

        // All bows are infinite!
        if (bow != null) {
            bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        }

        if (event.getForce() >= 0.9 && new Random().nextFloat() < superArrowChance) {
            // Shoot splash potion!
            Entity projectile = event.getProjectile();
            ThrownPotion thrownPotion = projectile.getWorld().spawn(projectile.getLocation(), ThrownPotion.class);

            ItemStack itemPotion = ItemStackBuilder.of(Material.SPLASH_POTION)
                    .transformMeta(itemMeta -> {
                        PotionMeta meta = (PotionMeta) itemMeta;
                        meta.setColor(Color.RED);
                        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 3), true);
                        meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 10, 4), true);
                    })
                    .build();

            thrownPotion.setItem(itemPotion);
            thrownPotion.setVelocity(projectile.getVelocity());
            event.setProjectile(thrownPotion);
        }
    }
}

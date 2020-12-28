package co.mcsky.villagedefensenhancement.modules;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import plugily.projects.villagedefense.api.event.wave.VillageWaveStartEvent;
import plugily.projects.villagedefense.arena.Arena;

import java.util.Random;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

/**
 * This class modifies the mechanism of spawning zombies.
 */
public class MoreZombies implements Listener {

    private final Random rd;
    private final double netherProbability;
    private final double drownedProbability;
    private final int extraZombieBase;

    public MoreZombies() {
        rd = new Random();
        netherProbability = plugin.config.node("more-zombies", "nether-probability").getDouble(0.25);
        drownedProbability = plugin.config.node("more-zombies", "drowned-probability").getDouble(0.25);
        extraZombieBase = plugin.config.node("more-zombies", "extra-zombie-base").getInt(32);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Spawn more zombies
     */
    @EventHandler
    public void onWaveStart(VillageWaveStartEvent event) {
        Arena arena = event.getArena();
        int extraAmount = Math.max(extraZombieBase, event.getWaveNumber() * event.getWaveNumber() - 64);
        for (int i = 0; i < extraAmount; i++) {
            switch (rd.nextInt(4)) {
                case 0:
                    arena.spawnFastZombie(rd);
                    break;
                case 1:
                    arena.spawnHardZombie(rd);
                    break;
                case 2:
                    arena.spawnSoftHardZombie(rd);
                    break;
                case 3:
                    arena.spawnKnockbackResistantZombies(rd);
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onZombieSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        EntityEquipment equipment = entity.getEquipment();
        if (entity instanceof Zombie && equipment != null) {
            if (rd.nextDouble() <= netherProbability) {
                equipment.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                equipment.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                equipment.setItemInMainHand(new ItemStack(Material.NETHERITE_SHOVEL));
                return;
            }

            if (rd.nextDouble() <= drownedProbability) {
                ((Zombie) entity).setConversionTime(0);
            }
        }
    }

}

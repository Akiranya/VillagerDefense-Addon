package co.mcsky.VillageDefenseAddon.mobspawning;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZombieHandler {

    void equipMob(LivingEntity livingEntity, SPECIAL_ZOMBIE type) {
        ZombieHandler zombieHandler = new ZombieHandler();
        Zombie mob = (Zombie) livingEntity;
        switch (type) {
            case BABY:
                mob.setBaby(true);
                mob.setHealth(5D);
                break;
            case FAST:
                mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.6D);
                break;
            case HARD:
                zombieHandler.setHelmet(mob, new ItemStack(Material.IRON_HELMET));
                zombieHandler.setChestplate(mob, new ItemStack(Material.IRON_CHESTPLATE));
                zombieHandler.setPants(mob, new ItemStack(Material.IRON_LEGGINGS));
                zombieHandler.setBoots(mob, new ItemStack(Material.IRON_BOOTS));
                break;
            case SOFT_HARD:
                zombieHandler.setHelmet(mob, new ItemStack(Material.GOLDEN_HELMET));
                zombieHandler.setChestplate(mob, new ItemStack(Material.GOLDEN_CHESTPLATE));
                zombieHandler.setPants(mob, new ItemStack(Material.GOLDEN_LEGGINGS));
                zombieHandler.setBoots(mob, new ItemStack(Material.GOLDEN_BOOTS));
                break;
            case HALF_INVISIBLE:
                mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 60, 1));
                zombieHandler.setBoots(mob, new ItemStack(Material.CHAINMAIL_BOOTS));
                break;
            case KNOCKBACK_RESISTANT:
                mob.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
                mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1D);
                zombieHandler.setWeapon(mob, new ItemStack(Material.DIAMOND_SWORD));
                zombieHandler.setChestplate(mob, new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                break;
        }
    }

    private void setWeapon(LivingEntity monster, ItemStack itemStack) {
        EntityEquipment ee = monster.getEquipment();
        ee.setItemInMainHand(itemStack);
        ee.setItemInMainHandDropChance(0);
    }

    private void setHelmet(LivingEntity monster, ItemStack itemStack) {
        EntityEquipment ee = monster.getEquipment();
        ee.setHelmet(itemStack);
        ee.setHelmetDropChance(0);
    }

    private void setChestplate(LivingEntity monster, ItemStack itemStack) {
        EntityEquipment ee = monster.getEquipment();
        ee.setChestplate(itemStack);
        ee.setChestplateDropChance(0);
    }

    private void setPants(LivingEntity monster, ItemStack itemStack) {
        EntityEquipment ee = monster.getEquipment();
        ee.setLeggings(itemStack);
        ee.setLeggingsDropChance(0);
    }

    private void setBoots(LivingEntity monster, ItemStack itemStack) {
        EntityEquipment ee = monster.getEquipment();
        ee.setBoots(itemStack);
        ee.setBootsDropChance(0);
    }
}

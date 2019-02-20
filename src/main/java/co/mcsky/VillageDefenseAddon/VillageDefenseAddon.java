package co.mcsky.VillageDefenseAddon;

import co.mcsky.VillageDefenseAddon.features.ACTManager;
import co.mcsky.VillageDefenseAddon.features.ExtraPlayerStatsUpdater;
import co.mcsky.VillageDefenseAddon.gameplayfix.CollisionFixer;
import co.mcsky.VillageDefenseAddon.gameplayfix.VillagerNoMovement;
import co.mcsky.VillageDefenseAddon.mobspawning.CommandSpawnSpecialMob;
import co.mcsky.VillageDefenseAddon.mobspawning.ListenerSpawnExtraMob;
import co.mcsky.VillageDefenseAddon.usermanager.CommandUserManagerAddon;
import co.mcsky.VillageDefenseAddon.gameplayfix.ChooseKitAddon;
import co.mcsky.VillageDefenseAddon.extrainfo.GameStartAddon;
import co.mcsky.VillageDefenseAddon.extrainfo.VillagerOnDamageReminder;
import co.mcsky.VillageDefenseAddon.extrainfo.WaveEndCustomInfo;
import co.mcsky.VillageDefenseAddon.extrainfo.WaveStartCustomInfo;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.villagedefense.Main;

public class VillageDefenseAddon extends JavaPlugin {

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        String prefix = "[" + this.getName() + "] ";

        new WaveStartCustomInfo(this);
        new WaveEndCustomInfo(this);
        new GameStartAddon(this);
        new ChooseKitAddon(this);
        new ListenerSpawnExtraMob(this);
        new VillagerOnDamageReminder(this);
        new ExtraPlayerStatsUpdater(this);
        new CollisionFixer(this);
        new VillagerNoMovement(this);
        new CommandSpawnSpecialMob(this);

        pl.plajer.villagedefense.user.UserManager userManager = JavaPlugin.getPlugin(Main.class).getUserManager();
        new CommandUserManagerAddon(this, userManager);
        this.getServer().getLogger().info(prefix + "UserManagerAddon enabled!");

        new ACTManager(this);
    }
}

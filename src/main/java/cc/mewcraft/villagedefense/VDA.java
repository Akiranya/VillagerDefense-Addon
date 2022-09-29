package cc.mewcraft.villagedefense;

import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.module.BetterFriends;
import cc.mewcraft.villagedefense.module.BetterShooter;
import cc.mewcraft.villagedefense.module.CustomAnvil;
import cc.mewcraft.villagedefense.module.InventoryManager;
import cc.mewcraft.villagedefense.module.PlayerDispatcher;
import cc.mewcraft.villagedefense.module.RewardManager;
import cc.mewcraft.villagedefense.module.SmartKit;
import cc.mewcraft.villagedefense.module.SmartLoot;
import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import plugily.projects.villagedefense.Main;

public class VDA extends ExtendedJavaPlugin {

    private static VDA plugin;

    private MewMessages lang;
    private MewConfig config;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private CommandManager commandManager;

    private Main api;

    @Getter private SmartKit smartKit;
    @Getter private SmartLoot smartLoot;
    @Getter private CustomAnvil customAnvil;
    @Getter private BetterFriends betterFriends;
    @Getter private BetterShooter betterShooter;
    @Getter private RewardManager rewardManager;
    @Getter private InventoryManager inventoryManager;
    @Getter private PlayerDispatcher playerDispatcher;

    public static VDA instance() {
        return plugin;
    }

    public static MewConfig config() {
        return VDA.instance().config;
    }

    public static MewMessages lang() {
        return VDA.instance().lang;
    }

    public static Main api() {
        return VDA.instance().api;
    }

    @Override
    public void disable() {
        config.save();
    }

    @Override
    public void enable() {
        plugin = this;

        api = getPlugin("VillageDefense", Main.class);

        initConfig();
        initLanguage();
        initModules();
        initCommands();
    }

    public void reload() {
        onDisable();

        config.load();
        config.save();
        initLanguage();
        initModules();
        initCommands();
    }

    private void initConfig() {
        config = new MewConfig();
        config.load();
        config.save();
    }

    private void initModules() {
        smartKit = bind(new SmartKit());
        smartLoot = bind(new SmartLoot());
        customAnvil = bind(new CustomAnvil());
        betterFriends = bind(new BetterFriends());
        betterShooter = bind(new BetterShooter());

        rewardManager = bind(new RewardManager());
        inventoryManager = bind(new InventoryManager());
        playerDispatcher = bind(new PlayerDispatcher());
    }

    private void initLanguage() {
        lang = new MewMessages(this);
    }

    private void initCommands() {
        try {
            commandManager = new CommandManager(this);
        } catch (Exception e) {
            getLogger().severe("Failed to initialise commands. Disabling this plugin.");
            getLogger().severe("See the following errors for more details.");
            e.printStackTrace();
            disable();
        }
    }

}

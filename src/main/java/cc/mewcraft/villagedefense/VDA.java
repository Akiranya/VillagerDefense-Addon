package cc.mewcraft.villagedefense;

import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.module.BetterFriends;
import cc.mewcraft.villagedefense.module.BetterShooter;
import cc.mewcraft.villagedefense.module.CustomAnvil;
import cc.mewcraft.villagedefense.module.InventoryManager;
import cc.mewcraft.villagedefense.module.Module;
import cc.mewcraft.villagedefense.module.PlayerDispatcher;
import cc.mewcraft.villagedefense.module.RewardManager;
import cc.mewcraft.villagedefense.module.SmartKit;
import cc.mewcraft.villagedefense.module.SmartLoot;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import plugily.projects.villagedefense.Main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VDA extends ExtendedJavaPlugin {

    private static VDA plugin;

    public static boolean useNametagEdit;
    public static boolean useVault;

    private MewMessages lang;
    private MewConfig config;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private CommandManager commandManager;

    private Main api;

    private Map<Class<? extends Module>, Module> moduleMap;

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
    }

    @Override
    public void enable() {
        plugin = this;

        api = getPlugin("VillageDefense", Main.class);

        // 3rd party plugin hooks
        useNametagEdit = isPluginPresent("NametagEdit");
        useVault = isPluginPresent("Vault");

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

    @SuppressWarnings("unchecked")
    public <M extends Module> M getModule(Class<M> clazz) {
        return (M) moduleMap.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <M extends Module> Collection<M> getAllModules() {
        return (Collection<M>) moduleMap.values();
    }

    private void initConfig() {
        config = new MewConfig();
        config.load();

        // Options not related to modules, should go here
        useVault = config.node("compatibility", "Vault").getBoolean(false);
        useNametagEdit = config.node("compatibility", "NametagEdit").getBoolean(false);

        config.save();
    }

    private void initModules() {
        moduleMap = new HashMap<>();
        moduleMap.put(SmartKit.class, bind(new SmartKit()));
        moduleMap.put(SmartLoot.class, bind(new SmartLoot()));
        moduleMap.put(CustomAnvil.class, bind(new CustomAnvil()));
        moduleMap.put(BetterFriends.class, bind(new BetterFriends()));
        moduleMap.put(BetterShooter.class, bind(new BetterShooter()));
        moduleMap.put(RewardManager.class, bind(new RewardManager()));
        moduleMap.put(InventoryManager.class, bind(new InventoryManager()));
        moduleMap.put(PlayerDispatcher.class, bind(new PlayerDispatcher()));
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

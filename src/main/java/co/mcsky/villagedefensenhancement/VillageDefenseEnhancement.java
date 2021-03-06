package co.mcsky.villagedefensenhancement;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import co.mcsky.villagedefensenhancement.modules.*;
import co.mcsky.villagedefensenhancement.objects.VillagerPlayer;
import de.themoep.utils.lang.bukkit.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaRegistry;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class VillageDefenseEnhancement extends JavaPlugin {

    public static VillageDefenseEnhancement plugin;
    public static plugily.projects.villagedefense.Main api;

    public LanguageManager lang;
    public Configuration config;
    public PaperCommandManager commandManager;

    private SmartLoot smartLoot;
    private SmartKit smartKitSelection;
    private BetterVillager betterVillager;
    private InfiniteAnvil infiniteAnvil;
    private MoreZombies moreZombies;

    private InventoryManager invManager;
    private PlayerDispatcher gameManager;
    private RewardManager rewardManager;

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        config.save();
    }

    @Override
    public void onEnable() {
        plugin = this;
        api = getPlugin(plugily.projects.villagedefense.Main.class);

        config = new Configuration();
        config.load();

        initializeLanguageManager();
        initializeModules();
        registerCommands();

        config.save();
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        config.load();
        initializeLanguageManager();
        initializeModules();
        config.save();
    }

    public String getMessage(CommandSender sender, String key, Object... replacements) {
        if (replacements.length == 0) {
            return lang.getConfig(sender).get(key);
        } else {
            return lang.getConfig(sender).get(key, Arrays.stream(replacements)
                                                         .map(Object::toString)
                                                         .toArray(String[]::new));
        }
    }

    private void initializeModules() {
        smartLoot = new SmartLoot();
        smartKitSelection = new SmartKit();
        betterVillager = new BetterVillager();
        infiniteAnvil = new InfiniteAnvil();
        moreZombies = new MoreZombies();

        invManager = new InventoryManager();
        gameManager = new PlayerDispatcher();
        rewardManager = new RewardManager();
    }

    private void initializeLanguageManager() {
        lang = new LanguageManager(this, "languages", "zh");
        lang.setPlaceholderPrefix("{");
        lang.setPlaceholderSuffix("}");
        lang.setProvider(sender -> {
            if (sender instanceof Player)
                return ((Player) sender).getLocale();
            return null;
        });
    }

    private void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.getCommandCompletions().registerCompletion("arenas", c -> ArenaRegistry.getArenas()
                                                                                              .stream()
                                                                                              .map(Arena::getId)
                                                                                              .collect(Collectors.toList()));
        commandManager.getCommandContexts().registerContext(VillagerPlayer.class, VillagerPlayer.getContextResolver());
        commandManager.getCommandContexts().registerContext(Arena.class, c -> {
            String name = c.popFirstArg();
            return Optional.ofNullable(ArenaRegistry.getArena(name))
                           .orElseThrow(() -> new InvalidCommandArgument(String.format("没有叫做 %s 的竞技场", name)));
        });
        commandManager.registerDependency(RewardManager.class, rewardManager);
        commandManager.registerDependency(PlayerDispatcher.class, gameManager);
        commandManager.registerDependency(InventoryManager.class, invManager);
        commandManager.registerDependency(SmartLoot.class, smartLoot);
        commandManager.registerCommand(new CommandHandler());
    }

}

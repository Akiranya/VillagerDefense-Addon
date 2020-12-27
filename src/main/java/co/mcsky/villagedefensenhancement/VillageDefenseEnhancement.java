package co.mcsky.villagedefensenhancement;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import co.mcsky.villagedefensenhancement.modules.*;
import co.mcsky.villagedefensenhancement.objects.VillagerPlayer;
import de.themoep.utils.lang.bukkit.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.kits.KitRegistry;
import plugily.projects.villagedefense.kits.basekits.FreeKit;

import java.util.Optional;
import java.util.stream.Collectors;

public class VillageDefenseEnhancement extends JavaPlugin {

    public static VillageDefenseEnhancement plugin;
    public static plugily.projects.villagedefense.Main api;

    public LanguageManager lang;
    public Configuration config;
    public PaperCommandManager commandManager;

    private SmartLoot smartLoot;
    private SmartKitSelection smartKitSelection;
    private BetterVillager betterVillager;
    private BetterUpgrade betterUpgrade;
    private CollisionFixer collisionFixer;
    private InfiniteAnvil infiniteAnvil;
    private MoreZombies moreZombies;

    private InventoryManager invManager;
    private PlayerDispatcher gameManager;

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

        // Set default kit to Light Tank
        KitRegistry.setDefaultKit((FreeKit) KitRegistry.getKit(new ItemStack(Material.LEATHER_CHESTPLATE)));

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

    private void initializeModules() {
        smartLoot = new SmartLoot();
        smartKitSelection = new SmartKitSelection();
        betterVillager = new BetterVillager();
        betterUpgrade = new BetterUpgrade();
        collisionFixer = new CollisionFixer();
        infiniteAnvil = new InfiniteAnvil();
        moreZombies = new MoreZombies();

        invManager = new InventoryManager();
        gameManager = new PlayerDispatcher();
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
                                                                                              .map(Arena::getMapName)
                                                                                              .collect(Collectors.toList()));
        commandManager.getCommandContexts().registerContext(VillagerPlayer.class, VillagerPlayer.getContextResolver());
        commandManager.getCommandContexts().registerContext(Arena.class, c -> {
            String name = c.popFirstArg();
            return Optional.ofNullable(ArenaRegistry.getArena(name))
                           .orElseThrow(() -> new InvalidCommandArgument(String.format("没有叫做 %s 的竞技场", name)));
        });
        commandManager.registerDependency(PlayerDispatcher.class, gameManager);
        commandManager.registerDependency(InventoryManager.class, invManager);
        commandManager.registerDependency(SmartLoot.class, smartLoot);
        commandManager.registerCommand(new CommandHandler());
    }

    /**
     * Get a message from a language config for a certain sender
     *
     * @param sender       The sender to get the string for. (Language is based
     *                     on this)
     * @param key          The language key in the config
     * @param replacements An option array for replacements. (2n)-th will be the
     *                     placeholder, (2n+1)-th the value. Placeholders have
     *                     to be surrounded by percentage signs: %placeholder%
     *
     * @return The string from the config which matches the sender's language
     * (or the default one) with the replacements replaced (or an error message,
     * never null)
     */
    public String getMessage(CommandSender sender, String key,
                             String... replacements) {
        return lang.getConfig(sender).get(key, replacements);
    }

}

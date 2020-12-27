package co.mcsky.villagedefensenhancement;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

import static co.mcsky.villagedefensenhancement.VillageDefenseEnhancement.plugin;

public final class Configuration {

    public static final String configFileName = "config.yml";

    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    public Configuration() {
        // Initialize the loader for config file config.yml
        loader = YamlConfigurationLoader.builder()
                                        .indent(4)
                                        .path(new File(plugin.getDataFolder(), configFileName).toPath())
                                        .nodeStyle(NodeStyle.BLOCK)
                                        .build();
    }

    public CommentedConfigurationNode node(Object... path) {
        if (root == null) {
            plugin.getLogger().severe("Config is not loaded yet!");
            throw new IllegalStateException();
        }
        return root.node(path);
    }

    /**
     * Load the config file contents into memory
     */
    public void load() {
        // Load config from file, assigning the config contents to root
        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            plugin.getLogger().severe("Failed to load" + configFileName + ": " + e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Save file config.yml
     */
    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            plugin.getLogger().severe("Unable to save " + configFileName + ": " + e.getMessage());
        }
    }

}

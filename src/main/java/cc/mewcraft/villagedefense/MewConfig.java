package cc.mewcraft.villagedefense;

import lombok.CustomLog;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@CustomLog
public final class MewConfig {

    public static final String CONFIG_FILE = "config.yml";

    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    public MewConfig() {
        // Initialize the loader for config file config.yml
        loader = YamlConfigurationLoader.builder()
                .indent(4)
                .path(new File(VDA.instance().getDataFolder(), CONFIG_FILE).toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .build();
    }

    public CommentedConfigurationNode node(Object... path) {
        if (root == null) {
            LOG.error("Config is not loaded yet!");
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
            LOG.error("Failed to load file: " + CONFIG_FILE, e);
            LOG.error("Disabling the plugin. Please fix your config first!");
            VDA.instance().disable();
        }
    }

    /**
     * Save file config.yml
     */
    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            LOG.error("Unable to save " + CONFIG_FILE, e);
        }
    }

}

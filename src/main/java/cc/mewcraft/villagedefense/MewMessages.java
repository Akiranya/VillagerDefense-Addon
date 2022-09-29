package cc.mewcraft.villagedefense;

import de.themoep.utils.lang.bukkit.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class MewMessages {

    private final LanguageManager lang;

    public MewMessages(JavaPlugin plugin) {
        this.lang = new LanguageManager(plugin, "languages", "zh");
        this.lang.setPlaceholderPrefix("{");
        this.lang.setPlaceholderSuffix("}");
    }

    public String raw(CommandSender sender, String key, String... subst) {
        if (subst.length == 0) {
            return this.lang.getConfig(sender).get(key);
        } else {
            String[] list = new String[subst.length];
            System.arraycopy(subst, 0, list, 0, subst.length);
            return this.lang.getConfig(sender).get(key, list);
        }
    }

    public String raw(String key, String... subst) {
        return raw(null, key, subst);
    }

    public String legacy(CommandSender sender, String key, String... subst) {
        return ChatColor.translateAlternateColorCodes('&', raw(sender, key, subst));
    }

    public String legacy(String key, String... subst) {
        return legacy(null, key, subst);
    }

    public Component component(CommandSender sender, String key, String... subst) {
        return MiniMessage.get().deserialize(raw(sender, key, subst));
    }

    public Component component(String key, String... subst) {
        return component(null, key, subst);
    }

    public LanguageManager internal() {
        return lang;
    }

    public String toLegacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public String translate(Translatable translatable) {
        return toLegacy(Component.translatable(translatable).asComponent());
    }

}

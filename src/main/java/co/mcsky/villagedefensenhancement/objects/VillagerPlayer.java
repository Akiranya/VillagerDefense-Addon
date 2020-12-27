package co.mcsky.villagedefensenhancement.objects;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class VillagerPlayer {

    private final Set<Player> playerSet;

    public VillagerPlayer(Set<Player> playerSet) {
        this.playerSet = playerSet;
    }

    public static ContextResolver<VillagerPlayer, BukkitCommandExecutionContext> getContextResolver() {
        return c -> {
            String arg = c.popFirstArg();
            if (arg.equalsIgnoreCase("all")) {
                return new VillagerPlayer(new HashSet<>(Bukkit.getOnlinePlayers()));
            } else {
                return new VillagerPlayer(new HashSet<>() {{
                    add(ACFBukkitUtil.findPlayerSmart(c.getIssuer(), arg));
                }});
            }
        };
    }

    public Set<Player> getValue() {
        return playerSet;
    }

}

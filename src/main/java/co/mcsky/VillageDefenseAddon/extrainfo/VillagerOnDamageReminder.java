package co.mcsky.VillageDefenseAddon.extrainfo;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.api.event.game.VillageGameStartEvent;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.handlers.ChatManager;

import java.util.ArrayList;
import java.util.List;

public class VillagerOnDamageReminder implements Listener {

    private final VillageDefenseAddon plugin;
    private VillageGameStartEvent villageGameStartEvent;

    public VillagerOnDamageReminder(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void getEvent(VillageGameStartEvent event) {
        this.villageGameStartEvent = event;
    }

    private Arena getArena() {
        return villageGameStartEvent.getArena();
    }

    @EventHandler
    public void remindWhenVillagerOnDamage(final EntityDamageByEntityEvent event) {
        if (villageGameStartEvent == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Arena arena = getArena();
                List<Villager> villagers = arena.getVillagers();
                ChatManager chatManager = new ChatManager("[村民的呐喊] ");

                List<String> helpMsg = new ArrayList<>();
                helpMsg.add("有只僵尸在打我，快来人救救我！");
                helpMsg.add("我快被僵尸打死啦，快来人帮我杀了它们！");
                helpMsg.add("僵尸大军冲进来了，快来人帮帮我们！");

                if (event.getEntityType().equals(EntityType.VILLAGER)) {
                    // Only fire if victim is from the game
                    if (!villagers.contains(event.getEntity())) return;
                    // Only fire if the damager is a zombie or wither
                    boolean equals = event.getDamager().getType().equals(EntityType.ZOMBIE) ||
                            event.getDamager().getType().equals(EntityType.WITHER);
                    boolean greater = event.getFinalDamage() > 1;
                    if (equals && greater) {
                        int index = (int) (Math.random() * helpMsg.size());
                        chatManager.broadcast(arena, helpMsg.get(index));
                    }
                }
            }
        }.runTask(plugin);

    }

}

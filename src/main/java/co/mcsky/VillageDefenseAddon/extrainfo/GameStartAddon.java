package co.mcsky.VillageDefenseAddon.extrainfo;

import co.mcsky.VillageDefenseAddon.VillageDefenseAddon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.villagedefense.api.event.game.VillageGameStartEvent;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.handlers.ChatManager;

import java.util.ArrayList;
import java.util.List;

public class GameStartAddon implements Listener {
    private final VillageDefenseAddon plugin;

    public GameStartAddon(VillageDefenseAddon plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGameStart(final VillageGameStartEvent event) {

        final Arena arena = event.getArena();
        final List<String> messages = new ArrayList<>();

        // If current map is "airport"
        if (arena.getMapName().equals("airport")) {
            // Set a specified block to redstone block
            World world = event.getArena().getStartLocation().getWorld();
            Location loc = new Location(world, 90, 5, 479);
            Block block = loc.getBlock();
            block.setType(Material.REDSTONE_BLOCK);

            // Some dialogs
            messages.add("村民A: 外面。。外面是什么动静？");
            messages.add("村民B: 丧尸！一定是丧尸又来了！");
            messages.add("村民C: 不，好像不一样！好像..好像是一队全副武装的人！");
            messages.add("村民A: 救援！是救援到了！俺们有救了！");
            messages.add("村民C: 还有通风管道！我听到通风管道也有动静。");
            messages.add("村长: 我们在此也停留了一段时间 只要是有僵尸头颅在的地方都是僵尸的进攻地点 勇者们请多留意那些地方！");
            messages.add("村长: 僵尸死亡会掉落一些值钱的东西，我们一路过来也收集了一些武器，只可惜不会使用。勇者们如果有需要可以拿些钱来换。");
            messages.add("村长: 勇者们啊，我们总算盼到你们了。僵尸正从正门涌进来，拜托你们 一定要解决他们。");
        }

        // If current map is "v"
        if (arena.getMapName().equals("v")) {
            messages.add("村民A: 外面。。外面是什么动静？");
            messages.add("村民B: 丧尸！一定是丧尸又来了！");
            messages.add("村民C: 不，好像不一样！好像..好像是一队全副武装的人！");
            messages.add("村民A: 救援！是救援到了！俺们有救了！");
            messages.add("村长: 我们在此也停留了一段时间 只要是有僵尸头颅在的地方都是僵尸的进攻地点 勇者们请多留意那些地方！");
            messages.add("村长: 僵尸死亡会掉落一些值钱的东西，我们一路过来也收集了一些武器，只可惜不会使用。勇者们如果有需要可以拿些钱来换。");
            messages.add("村长: 勇者们啊，我们总算盼到你们了。僵尸正从正门涌进来，拜托你们 一定要解决他们。");
        }

        // Broadcast the dialogs
        new BukkitRunnable() {
            int index = 0;
            ChatManager chat = new ChatManager("[碎碎念] ");

            public void run() {
                chat.broadcast(arena, messages.get(index));
                index++;
                if (index >= messages.size()) this.cancel();
            }
        }.runTaskTimer(plugin, 40, 60);

        // Just for fun :)
        new BukkitRunnable() {
            List<Player> playersLeft = arena.getPlayersLeft();
            int ran = (int) (Math.random() * playersLeft.size());
            Player p = playersLeft.get(ran);
            int index = 0;

            public void run() {
                List<String> messages = new ArrayList<>();
                messages.add("一天本至尊中五独（毒）掌");
                messages.add("本将要陨落之时遇到了这里的仙人");
                messages.add("仙人告诉我只有守卫好这里的村民才有一线生机。");
                p.chat(messages.get(index));
                index++;
                if (index >= messages.size()) this.cancel();
            }
        }.runTaskTimer(plugin, messages.size() * 60, 40);

    }

}

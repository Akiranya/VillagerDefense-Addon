package cc.mewcraft.villagedefense.module;

import cc.mewcraft.villagedefense.VDA;
import com.nametagedit.plugin.NametagEdit;
import lombok.CustomLog;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.villagedefense.api.StatsStorage;
import plugily.projects.villagedefense.api.event.game.VillageGameLeaveAttemptEvent;
import plugily.projects.villagedefense.api.event.game.VillageGameStopEvent;
import plugily.projects.villagedefense.api.event.player.VillagePlayerChooseKitEvent;
import plugily.projects.villagedefense.arena.ArenaState;
import plugily.projects.villagedefense.kits.KitRegistry;
import plugily.projects.villagedefense.kits.basekits.Kit;
import plugily.projects.villagedefense.kits.basekits.PremiumKit;
import plugily.projects.villagedefense.kits.free.LightTankKit;
import plugily.projects.villagedefense.kits.level.HardcoreKit;
import plugily.projects.villagedefense.kits.level.MediumTankKit;
import plugily.projects.villagedefense.kits.premium.HeavyTankKit;
import plugily.projects.villagedefense.kits.premium.PremiumHardcoreKit;
import plugily.projects.villagedefense.user.UserManager;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A class improving stuff about {@link plugily.projects.villagedefense.kits.basekits.Kit}.
 */
@CustomLog
public class SmartKit extends Module {

    private final int levelRequired;
    private final boolean showKitAboveHead;
    private final Set<Class<? extends Kit>> kitList;

    public SmartKit() {
        kitList = Set.of(HeavyTankKit.class, MediumTankKit.class, HardcoreKit.class, PremiumHardcoreKit.class);

        // Set default kit to Light Tank
        Optional<LightTankKit> lightTankKit = KitRegistry.getKits().stream()
                .filter(kit -> kit instanceof LightTankKit)
                .map(kit -> (LightTankKit) kit)
                .findFirst();
        if (lightTankKit.isPresent()) {
            KitRegistry.setDefaultKit(lightTankKit.get());
        } else {
            LOG.warn("Cannot replace default kit (KnightKit) because the replacement kit (LightTankKit) is not found");
        }

        // Configuration values
        levelRequired = VDA.config().node("smart-kit", "premium-kit-level-required").getInt(12);
        showKitAboveHead = VDA.config().node("smart-kit", "show-kit-above-head").getBoolean(false);

        // Register this listener
        registerListener();
    }

    @EventHandler
    public void onPlayerChooseKit(VillagePlayerChooseKitEvent event) {
        Kit kit = event.getKit();
        Player player = event.getPlayer();
        UserManager userManager = VDA.api().getUserManager();

        // New Feature: don't allow to select kit while arena is fighting.
        if (event.getArena().getArenaState() == ArenaState.IN_GAME && !userManager.getUser(player).isSpectator()) {
            // Only allow to do so during spectating and before the first wave.

            event.setCancelled(true);
            player.sendMessage(VDA.lang().component(player,
                    "error_cannot_select_kit_when_spectating"
            ));
            return;
        }

        if (!kit.isUnlockedByPlayer(player)) {
            return;
        }

        // New Feature: make (all) PremiumKit like LevelKit!
        if (event.getKit() instanceof PremiumKit) {
            int userLevel = StatsStorage.getUserStats(player, StatsStorage.StatisticType.LEVEL);
            if (userLevel < levelRequired && !player.isOp()) {
                event.setCancelled(true);
                player.sendMessage(VDA.lang().component(player,
                        "error_cannot_select_kit_for_not_enough_levels",
                        "level-required", Integer.toString(levelRequired),
                        "current-level", Integer.toString(userLevel)
                ));
                return;
            }
        }

        // New Feature: show the kit name above the player's head
        NametagEdit.getApi().setPrefix(player, VDA.lang().legacy(player,
                "msg_tag_format",
                "kit", kit.getName()
        ));

        // Bug Fix: set max health to default (20) if currently selected kit doesn't modify max health
        if (kitList.contains(userManager.getUser(player).getKit().getClass()) && !kitList.contains(event.getKit().getClass())) {
            Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH), "No such attribute: GENERIC_MAX_HEALTH"
            ).setBaseValue(20D);
        }
    }

    @EventHandler
    public void onArenaEnd(VillageGameStopEvent event) {
        if (VDA.useNametagEdit && showKitAboveHead) {
            for (Player player : event.getArena().getPlayers()) {
                NametagEdit.getApi().clearNametag(player);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(VillageGameLeaveAttemptEvent event) {
        if (VDA.useNametagEdit && showKitAboveHead) {
            NametagEdit.getApi().clearNametag(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (VDA.useNametagEdit && showKitAboveHead) {
            NametagEdit.getApi().clearNametag(event.getPlayer());
        }
    }

    /**
     * Bug Fix: Remove all potion effects upon death to prevent some "residual effects" from the player's previous kit.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (PotionEffectType effect : PotionEffectType.values()) {
            player.removePotionEffect(effect);
        }
    }

}

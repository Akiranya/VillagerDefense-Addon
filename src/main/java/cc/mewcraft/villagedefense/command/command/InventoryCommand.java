package cc.mewcraft.villagedefense.command.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.AbstractCommand;
import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.module.InventoryManager;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class InventoryCommand extends AbstractCommand {

    private final InventoryManager inventoryManager;

    public InventoryCommand(
            VDA plugin,
            CommandManager manager,
            InventoryManager inventoryManager) {
        super(plugin, manager);

        this.inventoryManager = inventoryManager;
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> inventoryLiteral = manager.commandBuilder("vde")
                .permission("vde.command.inventory")
                .literal("inventory");

        Command<CommandSender> toggle = inventoryLiteral
                .literal("toggle")
                .argument(BooleanArgument.optional("status"))
                .handler(context -> {
                    boolean status = context.get("toggle");
                    if (status) {
                        inventoryManager.enable();
                        context.getSender().sendMessage(VDA.lang().component("msg_set_inventory_manager", "status", VDA.lang().raw("msg_enabled")));
                    } else {
                        inventoryManager.disable();
                        context.getSender().sendMessage(VDA.lang().component("msg_set_inventory_manager", "status", VDA.lang().raw("msg_disabled")));
                    }
                })
                .build();

        Command<CommandSender> show = inventoryLiteral
                .literal("show")
                .argument(EnumArgument.of(InventoryManager.Action.class, "action"))
                .handler(context -> {
                    InventoryManager.Action action = context.get("action");
                    inventoryManager.showWhitelist(action, context.getSender());
                })
                .build();

        Command<CommandSender> add = inventoryLiteral
                .literal("add")
                .argument(EnumArgument.of(InventoryManager.Action.class, "action"))
                .argument(MaterialArgument.optional("material"))
                .handler(context -> {
                    InventoryManager.Action action = context.get("action");
                    Optional<Material> material = context.getOptional("material");

                    Material mat = null;
                    if (material.isPresent()) {
                        mat = material.get();
                    } else {
                        if (context.getSender() instanceof Player player) {
                            Material type = player.getInventory().getItemInMainHand().getType();
                            if (!type.isAir()) {
                                mat = type;
                            }
                        }
                    }

                    if (mat != null) {
                        inventoryManager.addWhitelist(action, mat);
                    }
                })
                .build();

        Command<CommandSender> remove = inventoryLiteral
                .literal("remove")
                .argument(EnumArgument.of(InventoryManager.Action.class, "action"))
                .argument(MaterialArgument.<CommandSender>newBuilder("material")
                        .withSuggestionsProvider((context, input) -> {
                            InventoryManager.Action action = context.get("action");
                            return inventoryManager.getWhitelist(action).stream()
                                    .map(Enum::name)
                                    .toList();
                        }))
                .handler(context -> {
                    InventoryManager.Action action = context.get("action");
                    Optional<Material> material = context.getOptional("material");

                    Material mat = null;
                    if (material.isPresent()) {
                        mat = material.get();
                    } else {
                        if (context.getSender() instanceof Player player) {
                            Material type = player.getInventory().getItemInMainHand().getType();
                            if (!type.isAir()) {
                                mat = type;
                            }
                        }
                    }

                    if (mat != null) {
                        inventoryManager.removeWhitelist(action, mat);
                    }
                })
                .build();

        manager.register(List.of(toggle, show, add, remove));
    }
}

package cc.mewcraft.villagedefense.command.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.AbstractCommand;
import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.module.RewardManager;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RewardCommand extends AbstractCommand {

    private final RewardManager rewardManager;

    public RewardCommand(
            VDA plugin,
            CommandManager manager,
            RewardManager rewardManager) {
        super(plugin, manager);

        this.rewardManager = rewardManager;
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> rewardLiteral = manager.commandBuilder("vde")
                .permission("vde.command.reward")
                .literal("reward");

        Command<CommandSender> setDamageDivisor = rewardLiteral
                .literal("set")
                .literal("damage.divisor")
                .argument(DoubleArgument.of("value"))
                .handler(context -> {
                    double value = context.get("value");
                    rewardManager.setDamageDivisor(value);
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_set_total_damage_divisor"
                    ));
                })
                .build();

        Command<CommandSender> getTotalDamage = rewardLiteral
                .literal("get")
                .literal("total.damage")
                .handler(context -> {
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_current_total_damage_dealt",
                            "amount", Double.toString(rewardManager.getTotalDamageDealt())
                    ));
                })
                .build();

        manager.register(List.of(setDamageDivisor, getTotalDamage));
    }
}

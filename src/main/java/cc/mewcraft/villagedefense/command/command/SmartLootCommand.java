package cc.mewcraft.villagedefense.command.command;

import cc.mewcraft.villagedefense.VDA;
import cc.mewcraft.villagedefense.command.AbstractCommand;
import cc.mewcraft.villagedefense.command.CommandManager;
import cc.mewcraft.villagedefense.module.SmartLoot;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

public class SmartLootCommand extends AbstractCommand {

    private final SmartLoot smartLoot;

    public SmartLootCommand(
            VDA plugin,
            CommandManager manager,
            SmartLoot smartLoot) {
        super(plugin, manager);

        this.smartLoot = smartLoot;
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> lootLiteral = manager.commandBuilder("vde")
                .permission("vde.command.loot")
                .literal("loot");

        Command<CommandSender> meleeVanillaExp = lootLiteral
                .literal("melee.vanilla.exp")
                .argument(IntegerArgument.<CommandSender>newBuilder("experience")
                        .withMin(0)
                        .asOptional())
                .handler(context -> {
                    Optional<Integer> experience = context.getOptional("experience");
                    experience.ifPresent(smartLoot::setMeleeVanillaExp);
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_set_melee_attack_vanilla_experience_reward",
                            "amount", Integer.toString(smartLoot.getMeleeVanillaExp())
                    ));
                })
                .build();

        Command<CommandSender> meleeKitExp = lootLiteral
                .literal("melee.kit.exp")
                .argument(IntegerArgument.<CommandSender>newBuilder("experience")
                        .withMin(0)
                        .asOptional())
                .handler(context -> {
                    Optional<Integer> experience = context.getOptional("experience");
                    experience.ifPresent(smartLoot::setMeleeKitExp);
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_set_melee_attack_kit_experience_reward",
                            "amount", Integer.toString(smartLoot.getMeleeKitExp())
                    ));
                })
                .build();

        Command<CommandSender> rangedVanillaExp = lootLiteral
                .literal("ranged.vanilla.exp")
                .argument(IntegerArgument.<CommandSender>newBuilder("experience")
                        .withMin(0)
                        .asOptional())
                .handler(context -> {
                    Optional<Integer> experience = context.getOptional("experience");
                    experience.ifPresent(smartLoot::setRangedVanillaExp);
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_set_ranged_attack_vanilla_experience_reward",
                            "amount", Integer.toString(smartLoot.getRangedVanillaExp())
                    ));
                })
                .build();

        Command<CommandSender> rangedKitExp = lootLiteral
                .literal("ranged.kit.exp")
                .argument(IntegerArgument.<CommandSender>newBuilder("experience")
                        .withMin(0)
                        .asOptional())
                .handler(context -> {
                    Optional<Integer> experience = context.getOptional("experience");
                    experience.ifPresent(smartLoot::setRangedKitExp);
                    context.getSender().sendMessage(VDA.lang().component(
                            "msg_set_ranged_attack_kit_experience_reward",
                            "amount", Integer.toString(smartLoot.getRangedKitExp())
                    ));
                })
                .build();

        Command<CommandSender> setMinDamageRequirement = lootLiteral
                .literal("min.damage.requirement")
                .argument(DoubleArgument.<CommandSender>newBuilder("damage")
                        .withMin(0)
                        .asRequired())
                .handler(context -> {
                    double damage = context.get("damage");
                    smartLoot.setMinimumDamageRequirement(damage);
                })
                .build();

        manager.register(List.of(meleeVanillaExp, meleeKitExp, rangedVanillaExp, rangedKitExp, setMinDamageRequirement));
    }
}

package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import me.lorenzo0111.rocketbans.utils.StringUtils;

public class HelpCommand extends SubCommand {

    public HelpCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(AbstractSender<?> sender, String label, String[] args) {
        String prefix = plugin.getMessage("prefix");
        sender.sendMessage(StringUtils.color(prefix + "&c&m-------------------------------"));
        sender.sendMessage(StringUtils.color(prefix + "&c&lRocket&e&lBans &7v" + plugin.getVersion()));
        sender.sendMessage(prefix);
        sender.sendMessage(StringUtils.color(prefix + "&7Available commands:"));

        for (SubCommand subCommand : command.getSubCommands()) {
            if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                continue;
            }

            sender.sendMessage(StringUtils.color(prefix + "&c/rb " + subCommand.getUsage() + " &7- " + subCommand.getDescription()));
        }

        sender.sendMessage(StringUtils.color(prefix + "&c&m-------------------------------"));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show the help page";
    }
}

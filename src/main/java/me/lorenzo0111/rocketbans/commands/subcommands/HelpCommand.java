package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import org.bukkit.command.CommandSender;

import static me.lorenzo0111.rocketbans.utils.StringUtils.color;

public class HelpCommand extends SubCommand {

    public HelpCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        String prefix = plugin.getMessage("prefix");
        sender.sendMessage(color(prefix + "&c&m-------------------------------"));
        sender.sendMessage(color(prefix + "&c&lRocket&e&lBans &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(prefix);
        sender.sendMessage(color(prefix + "&7Available commands:"));

        for (SubCommand subCommand : command.getSubCommands()) {
            if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                continue;
            }

            sender.sendMessage(color(prefix + "&c/" + label + " " + subCommand.getUsage() + " &7- " + subCommand.getDescription()));
        }

        sender.sendMessage(color(prefix + "&c&m-------------------------------"));
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

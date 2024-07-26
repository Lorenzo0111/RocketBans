package me.lorenzo0111.rocketbans.commands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.exceptions.OnlyPlayersException;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.commands.subcommands.HelpCommand;
import me.lorenzo0111.rocketbans.commands.subcommands.NotFoundCommand;
import me.lorenzo0111.rocketbans.commands.subcommands.ReloadCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RocketBansCommand implements TabExecutor {
    private final RocketBans plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public RocketBansCommand(RocketBans plugin) {
        this.plugin = plugin;

        register(new HelpCommand(this));
        register(new ReloadCommand(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            this.runSubCommand(sender, getSubCommand("help"), label, args);
            return true;
        }

        SubCommand subCommand = getSubCommand(args[0]);

        if (subCommand.getMinArgs() > (args.length - 1)) {
            sender.sendMessage(plugin.getPrefixed("invalid-usage").replace("%usage%", "/" + label + " " + subCommand.getUsage()));
            return true;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(plugin.getPrefixed("no-permission"));
            return true;
        }

        this.runSubCommand(sender, subCommand, label, newArgs);
        return true;
    }

    private void runSubCommand(CommandSender sender, SubCommand command, String label, String[] args) {
        try {
            command.handle(sender, label, args);
        } catch (UsageException e) {
            sender.sendMessage(plugin.getPrefixed("invalid-usage").replace("%usage%", "/" + label + " " + command.getUsage()));
        } catch (OnlyPlayersException e) {
            sender.sendMessage(plugin.getPrefixed("only-players"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getPrefixed("error").replace("%error%", e.getMessage()));
        }
    }

    private SubCommand getSubCommand(String name) {
        for (SubCommand subCommand : this.subCommands) {
            if (subCommand.getName().equalsIgnoreCase(name)) {
                return subCommand;
            }

            for (String alias : subCommand.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return subCommand;
                }
            }
        }

        return new NotFoundCommand(this);
    }

    public RocketBans getPlugin() {
        return plugin;
    }

    public void register(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            List<String> subCommands = new ArrayList<>();

            for (SubCommand subCommand : this.subCommands) {
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    continue;
                }

                if (args.length == 0 || subCommand.getName().startsWith(args[0])) {
                    subCommands.add(subCommand.getName());
                }
            }

            return subCommands;
        }

        SubCommand subCommand = getSubCommand(args[0]);
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        return subCommand.handleTabCompletion(sender, newArgs);
    }
}

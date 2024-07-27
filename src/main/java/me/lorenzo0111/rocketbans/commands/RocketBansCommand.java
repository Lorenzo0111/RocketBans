package me.lorenzo0111.rocketbans.commands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.exceptions.OnlyPlayersException;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.commands.subcommands.*;
import me.lorenzo0111.rocketbans.data.records.Ban;
import me.lorenzo0111.rocketbans.data.records.Mute;
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
        register(new UnbanCommand(this));
        register(new HistoryCommand(this));
        register(new KickCommand(this));
        register(new ExpiringActionCommand<>(this, "ban", Ban.class, (ban, player) -> player.ban(
                ban.reason(),
                ban.expires(),
                ban.executor().toString()
        )));
        register(new ExpiringActionCommand<>(this, "mute", Mute.class, (mute, player) ->
                plugin.getMuteManager().addMute(mute)));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        SubCommand subCommand = getSubCommand(label);

        if (subCommand == null && args.length == 0) {
            this.runSubCommand(sender, getSubCommand("help"), label, args);
            return true;
        }

        if (subCommand != null) {
            this.runSubCommand(sender, subCommand, label, args);
            return true;
        }

        subCommand = getSubCommand(args[0]);
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        this.runSubCommand(sender, subCommand, label, newArgs);
        return true;
    }

    private void runSubCommand(CommandSender sender, SubCommand command, String label, String[] args) {
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            sender.sendMessage(plugin.getPrefixed("no-permission"));
            return;
        }

        if (command.getMinArgs() > args.length) {
            sender.sendMessage(plugin.getPrefixed("invalid-usage").replace("%usage%", "/rb " + command.getUsage()));
            return;
        }

        try {
            command.handle(sender, label, args);
        } catch (UsageException e) {
            sender.sendMessage(plugin.getPrefixed("invalid-usage").replace("%usage%", "/rb " + command.getUsage()));
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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

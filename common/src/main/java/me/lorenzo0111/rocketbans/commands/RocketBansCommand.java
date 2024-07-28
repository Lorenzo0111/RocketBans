package me.lorenzo0111.rocketbans.commands;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.commands.exceptions.OnlyPlayersException;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.commands.subcommands.*;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class RocketBansCommand {
    private final RocketBansPlugin plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public RocketBansCommand(RocketBansPlugin plugin) {
        this.plugin = plugin;

        register(new HelpCommand(this));
        register(new ReloadCommand(this));
        register(new UnbanCommand(this));
        register(new UnmuteCommand(this));
// todo       register(new HistoryCommand(this));
        register(new DeleteCommand(this));
// todo        register(new WarnsCommand(this));
        register(new KickCommand(this));
        register(new ExpiringActionCommand<>(this, "ban", Ban.class, (ban, player) -> plugin.getPlatform().ban(
                player,
                ban.reason(),
                ban.expires(),
                ban.executor()
        )));
        register(new ExpiringActionCommand<>(this, "mute", Mute.class, (mute, player) ->
                plugin.getMuteManager().addMute(mute)));
        register(new ExpiringActionCommand<>(this, "warn", Warn.class, (warn, player) -> {
            int maxWarns = plugin.getConfig().node("warns.max").getInt();
            if (maxWarns == -1) return;

            plugin.getDatabase().get(Warn.class, player.getUniqueId(), true).thenAccept(warns -> {
               if (warns.size() >= maxWarns) {
                   plugin.getPlatform().dispatchCommand(
                           plugin.getConfig().node("warns", "ban-command").getString("")
                                   .replace("%player%", player.getName())
                   );
               }
            });
        }));
    }

    protected void handleCommand(@NotNull AbstractSender<?> sender, @NotNull String label, String[] args) {
        SubCommand subCommand = getSubCommand(label);
        if (subCommand instanceof NotFoundCommand)
            subCommand = null;

        if (subCommand == null && args.length == 0) {
            this.runSubCommand(sender, getSubCommand("help"), label, args);
            return;
        }

        if (subCommand != null) {
            this.runSubCommand(sender, subCommand, label, args);
            return;
        }

        subCommand = getSubCommand(args[0]);
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        this.runSubCommand(sender, subCommand, label, newArgs);
    }

    protected List<String> tabComplete(@NotNull AbstractSender<?> sender, @NotNull String label, String[] args) {
        SubCommand cmd = getSubCommand(label);
        if (cmd instanceof NotFoundCommand)
            cmd = null;

        if (cmd != null) {
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                return new ArrayList<>();
            }

            return cmd.handleTabCompletion(sender, args);
        }

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

        cmd = getSubCommand(args[0]);
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        return cmd.handleTabCompletion(sender, newArgs);
    }

    private void runSubCommand(AbstractSender<?> sender, SubCommand command, String label, String[] args) {
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

    public RocketBansPlugin getPlugin() {
        return plugin;
    }

    public void register(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}

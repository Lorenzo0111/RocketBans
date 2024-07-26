package me.lorenzo0111.rocketbans.commands;

import me.lorenzo0111.rocketbans.RocketBans;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand {
    protected final RocketBans plugin;
    protected final RocketBansCommand command;

    public SubCommand(RocketBansCommand command) {
        this.command = command;
        this.plugin = command.getPlugin();
    }

    public void handle(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("This command is not implemented yet.");
    }

    public void handle(CommandSender sender, String label, String[] args) {
        this.handle(sender, args);
    }

    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    public abstract String getName();

    public abstract String getDescription();

    public String getPermission() {
        return null;
    }

    public String getUsage() {
        return this.getName();
    }

    public int getMinArgs() {
        return 0;
    }

    public String[] getAliases() {
        return new String[0];
    }
}

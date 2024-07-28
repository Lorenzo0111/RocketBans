package me.lorenzo0111.rocketbans.bungee.commands;

import me.lorenzo0111.rocketbans.bungee.platform.entity.BungeeAdapter;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommandExecutor extends Command implements TabExecutor {
    private final RocketBansCommand command;
    private final String label;

    public BungeeCommandExecutor(RocketBansCommand command, String label) {
        super(label);
        this.command = command;
        this.label = label;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        command.handleCommand(BungeeAdapter.sender(sender), label, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return command.handleTab(BungeeAdapter.sender(sender), label, args);
    }
}
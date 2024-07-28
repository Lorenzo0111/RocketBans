package me.lorenzo0111.rocketbans.bungee.commands;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.bungee.RocketBans;
import me.lorenzo0111.rocketbans.bungee.commands.subcommands.HistoryCommand;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;

public class BungeeCommand extends RocketBansCommand {

    public BungeeCommand(RocketBansPlugin plugin) {
        super(plugin);

        register(new HistoryCommand(this));
    }

    public void register(String label) {
        RocketBans plugin = (RocketBans) getPlugin();
        plugin.getProxy().getPluginManager().registerCommand(plugin, new BungeeCommandExecutor(this, label));
    }

}

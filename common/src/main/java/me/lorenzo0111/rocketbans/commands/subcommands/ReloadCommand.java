package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        this.plugin.reload();
        sender.sendMessage(plugin.getPrefixed("reload"));
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "rocketbans.reload";
    }

    @Override
    public String getDescription() {
        return "Reload the plugin";
    }
}

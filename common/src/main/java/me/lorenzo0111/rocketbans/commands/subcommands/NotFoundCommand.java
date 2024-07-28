package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;

public class NotFoundCommand extends SubCommand {

    public NotFoundCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(AbstractSender<?> sender, String[] args) {
        sender.sendMessage(plugin.getPrefixed("not-found"));
    }

    @Override
    public String getName() {
        return "!notfound";
    }

    @Override
    public String getDescription() {
        return null;
    }

}

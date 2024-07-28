package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import me.lorenzo0111.rocketbans.utils.StringUtils;

import java.util.List;

public class UnbanCommand extends SubCommand {

    public UnbanCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(AbstractSender<?> sender, String[] args) {
        AbstractPlayer<?> target = plugin.getPlatform().getPlayer(args[0]);
        plugin.getDatabase().expireAll(Ban.class, target.getUniqueId());

        sender.sendMessage(plugin.getPrefixed("unban").replace("%player%",
                StringUtils.or(target.getName(), args[0])));

        plugin.getPlatform().unban(target);
    }

    @Override
    public List<String> handleTabCompletion(AbstractSender<?> sender, String[] args) {
        return plugin.getPlatform().getBanList()
                .stream()
                .map(AbstractPlayer::getName)
                .toList();
    }

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public String getDescription() {
        return "Unban a player";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "unban <player>";
    }

    @Override
    public String getPermission() {
        return "rocketbans.unban";
    }
}

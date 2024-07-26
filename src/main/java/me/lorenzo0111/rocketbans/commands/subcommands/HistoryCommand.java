package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.data.Ban;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class HistoryCommand extends SubCommand {

    public HistoryCommand(RocketBansCommand command) {
        super(command);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        plugin.getDatabase().getBans(target.getUniqueId(), false)
                .thenAccept(bans -> {
                    if (bans.isEmpty()) {
                        sender.sendMessage(plugin.getPrefixed("no-history"));
                        return;
                    }

                    for (Ban ban : bans) {
                        sender.sendMessage(
                                plugin.getMessage("history")
                                        .replace("%type%", "BAN")
                                        .replace("%status%", ban.active() ? "Active" : "Expired")
                                        .replace("%executor%",
                                                ban.executor().equals(RocketBans.CONSOLE_UUID) ?
                                                        "Console" :
                                                        StringUtils.or(
                                                                Bukkit.getOfflinePlayer(ban.executor()).getName(),
                                                                "Unknown")
                                        )
                                        .replace("%reason%", ban.reason())
                                        .replace("%date%", TimeUtils.formatDate(ban.date().getTime()))
                                        .replace("%duration%", ban.expires() == null ? "Permanent" :
                                                TimeUtils.formatTime(ban.expires().getTime() - ban.date().getTime()))
                        );
                    }
                });
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public String getDescription() {
        return "See a player's history";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "<player>";
    }
}

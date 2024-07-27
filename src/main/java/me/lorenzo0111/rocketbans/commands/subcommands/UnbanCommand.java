package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UnbanCommand extends SubCommand {

    public UnbanCommand(RocketBansCommand command) {
        super(command);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        plugin.getDatabase().expireAll(Ban.class, target.getUniqueId());

        sender.sendMessage(plugin.getPrefixed("unban").replace("%player%",
                StringUtils.or(target.getName(), args[0])));

        ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                .pardon(target.getPlayerProfile());
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                .getEntries().stream()
                .map(entry -> entry.getBanTarget().getName())
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

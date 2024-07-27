package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class UnmuteCommand extends SubCommand {

    public UnmuteCommand(RocketBansCommand command) {
        super(command);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        plugin.getDatabase().expireAll(Mute.class, target.getUniqueId());

        sender.sendMessage(plugin.getPrefixed("unmute").replace("%player%",
                StringUtils.or(target.getName(), args[0])));

        plugin.getMuteManager().removeMutes(target.getUniqueId());
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return plugin.getMuteManager().getMutes()
                .keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .toList();
    }

    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Unmute a player";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "unmute <player>";
    }

    @Override
    public String getPermission() {
        return "rocketbans.unmute";
    }
}

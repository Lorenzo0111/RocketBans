package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.data.records.Kick;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class KickCommand extends SubCommand {

    public KickCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new UsageException();

        StringBuilder reason = new StringBuilder("N/A");

        if (args.length > 1) {
            reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reason.append(args[i]).append(" ");
            }
        }

        reason = new StringBuilder(StringUtils.color(reason.toString().trim()));

        Kick kick = new Kick(
                -1,
                target.getUniqueId(),
                reason.toString(),
                sender instanceof Player ? ((Player) sender).getUniqueId() : RocketBans.CONSOLE_UUID,
                new Timestamp(new Date().getTime())
        );

        plugin.getDatabase().add(kick);
        target.kickPlayer(kick.reason());

        sender.sendMessage(plugin.getPrefixed("kick")
                .replace("%player%", target.getName())
                .replace("%reason%", kick.reason())
        );
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return playerNames();
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return "Kick a player";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "kick <player> [reason]";
    }

    @Override
    public String getPermission() {
        return "rocketbans.kick";
    }
}

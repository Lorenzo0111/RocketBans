package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.data.Ban;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Date;

public class BanCommand extends SubCommand {

    public BanCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new UsageException();

        long duration = -1;
        StringBuilder reason = new StringBuilder("N/A");

        if (args.length > 1) {
            duration = TimeUtils.parseTime(args[1]);
            if (duration <= 0) duration = -1;

            reason = new StringBuilder();
            for (int i = duration == -1 ? 1 : 2; i < args.length; i++) {
                reason.append(args[i]).append(" ");
            }
        }

        Ban ban = new Ban(
                -1,
                target.getUniqueId(),
                reason.toString(),
                sender instanceof Player ? ((Player) sender).getUniqueId() : RocketBans.CONSOLE_UUID,
                new Timestamp(new Date().getTime()),
                duration == -1 ? null : new Timestamp(new Date().getTime() + duration),
                true
        );

        plugin.getDatabase().addBan(ban);
        target.ban(
                ban.reason(),
                ban.expires(),
                sender instanceof Player ? sender.getName() : "Console"
        );

        if (duration == -1)
            sender.sendMessage(plugin.getPrefixed("ban.permanent")
                    .replace("%player%", target.getName())
                    .replace("%reason%", ban.reason())
            );
        else
            sender.sendMessage(plugin.getPrefixed("ban.temp")
                    .replace("%player%", target.getName())
                    .replace("%reason%", ban.reason())
                    .replace("%time%", TimeUtils.formatTime(duration))
            );
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public String getDescription() {
        return "Ban a player";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "<player> [time] [reason]";
    }
}

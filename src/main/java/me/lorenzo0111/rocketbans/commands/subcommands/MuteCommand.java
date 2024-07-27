package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.data.Mute;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Date;

public class MuteCommand extends SubCommand {

    public MuteCommand(RocketBansCommand command) {
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

        reason = new StringBuilder(StringUtils.color(reason.toString().trim()));

        Mute mute = new Mute(
                -1,
                target.getUniqueId(),
                reason.toString(),
                sender instanceof Player ? ((Player) sender).getUniqueId() : RocketBans.CONSOLE_UUID,
                new Timestamp(new Date().getTime()),
                duration == -1 ? null : new Timestamp(new Date().getTime() + duration),
                true
        );

        plugin.getDatabase().addMute(mute).thenAccept(id ->
                plugin.getMuteManager().addMute(mute.withId(id)));

        if (duration == -1)
            sender.sendMessage(plugin.getPrefixed("mute.permanent")
                    .replace("%player%", target.getName())
                    .replace("%reason%", mute.reason())
            );
        else
            sender.sendMessage(plugin.getPrefixed("mute.temp")
                    .replace("%player%", target.getName())
                    .replace("%reason%", mute.reason())
                    .replace("%time%", TimeUtils.formatTime(duration))
            );
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Mute a player";
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

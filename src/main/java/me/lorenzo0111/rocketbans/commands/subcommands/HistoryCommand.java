package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.data.HistoryRecord;
import me.lorenzo0111.rocketbans.data.Table;
import me.lorenzo0111.rocketbans.data.records.Ban;
import me.lorenzo0111.rocketbans.data.records.Kick;
import me.lorenzo0111.rocketbans.data.records.Mute;
import me.lorenzo0111.rocketbans.data.records.Warn;
import me.lorenzo0111.rocketbans.gui.menus.HistoryMenu;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HistoryCommand extends SubCommand {

    public HistoryCommand(RocketBansCommand command) {
        super(command);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (args.length == 2 && args[1].equalsIgnoreCase("-m") && sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<Ban> bans = plugin.getDatabase().get(Ban.class, target.getUniqueId(), false).join();
                List<Mute> mutes = plugin.getDatabase().get(Mute.class, target.getUniqueId(), false).join();
                List<Kick> kicks = plugin.getDatabase().get(Kick.class, target.getUniqueId(), false).join();
                List<Warn> warns = plugin.getDatabase().get(Warn.class, target.getUniqueId(), false).join();

                List<HistoryRecord> merged = new ArrayList<>();
                merged.addAll(bans);
                merged.addAll(mutes);
                merged.addAll(kicks);
                merged.addAll(warns);

                new HistoryMenu(merged).open(player);
            });
            return;
        }

        sendHistory(sender, target, Ban.class);
        sendHistory(sender, target, Mute.class);
        sendHistory(sender, target, Kick.class);
        sendHistory(sender, target, Warn.class);
    }

    private <T extends HistoryRecord> void sendHistory(CommandSender sender, OfflinePlayer target, Class<T> type) {
        plugin.getDatabase().get(type, target.getUniqueId(), false)
                .thenAccept(items -> {
                    Table table = Table.fromClass(type);
                    if (table == null) return;

                    if (items.isEmpty()) {
                        sender.sendMessage(plugin.getPrefixed("no-history")
                                .replace("%type%", table.toString()));
                        return;
                    }

                    for (T item : items) {
                        String message = plugin.getMessage("history")
                                .replace("%type%", table.toString().toUpperCase())
                                .replace("%status%", !(item instanceof ExpiringRecord expiring) || expiring.active() ? "Active" : "Expired")
                                .replace("%executor%",
                                        item.executor().equals(RocketBans.CONSOLE_UUID) ?
                                                "Console" :
                                                StringUtils.or(
                                                        Bukkit.getOfflinePlayer(item.executor()).getName(),
                                                        "Unknown")
                                )
                                .replace("%reason%", item.reason())
                                .replace("%date%", TimeUtils.formatDate(item.date().getTime()))
                                .replace("%duration%", !(item instanceof ExpiringRecord expiring) || expiring.expires() == null ? "Permanent" :
                                        TimeUtils.formatTime(expiring.expires().getTime() - item.date().getTime()));

                        sender.sendMessage(message);
                    }
                });
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return playerNames();
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
        return "history <player> [-m]";
    }

    @Override
    public String getPermission() {
        return "rocketbans.history";
    }
}

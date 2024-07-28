package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.Table;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHistoryCommand extends SubCommand {

    public AbstractHistoryCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(AbstractSender<?> sender, String[] args) {
        AbstractPlayer<?> target = plugin.getPlatform().getPlayer(args[0]);

        if (supportsMenu() && args.length == 2 && args[1].equalsIgnoreCase("-m")
                && sender instanceof AbstractPlayer<?> player && player.isOnline()) {
            plugin.getPlatform().async(() -> {
                List<Ban> bans = plugin.getDatabase().get(Ban.class, target.getUniqueId(), false).join();
                List<Mute> mutes = plugin.getDatabase().get(Mute.class, target.getUniqueId(), false).join();
                List<Kick> kicks = plugin.getDatabase().get(Kick.class, target.getUniqueId(), false).join();
                List<Warn> warns = plugin.getDatabase().get(Warn.class, target.getUniqueId(), false).join();

                List<HistoryRecord> merged = new ArrayList<>();
                merged.addAll(bans);
                merged.addAll(mutes);
                merged.addAll(kicks);
                merged.addAll(warns);

                openMenu(player, merged);
            });
            return;
        }

        sendHistory(sender, target, Ban.class);
        sendHistory(sender, target, Mute.class);
        sendHistory(sender, target, Kick.class);
        sendHistory(sender, target, Warn.class);
    }

    private <T extends HistoryRecord> void sendHistory(AbstractSender<?> sender, AbstractPlayer<?> target, Class<T> type) {
        plugin.getDatabase().get(type, target.getUniqueId(), false)
                .thenAccept(items -> {
                    Table table = Table.fromClass(type);
                    if (table == null) return;

                    if (items.isEmpty()) {
                        sender.sendMessage(plugin.getPrefixed("no-history")
                                .replace("%type%", table.toString())
                                .replace("%player%", StringUtils.or(target.getName(), "Unknown")));
                        return;
                    }

                    for (T item : items) {
                        String message = plugin.getMessage("history")
                                .replace("%type%", table.toString().toUpperCase())
                                .replace("%status%", !(item instanceof ExpiringRecord expiring) || expiring.active() ? "Active" : "Expired")
                                .replace("%executor%",
                                        item.executor().equals(RocketBansAPI.CONSOLE_UUID) ?
                                                "Console" :
                                                StringUtils.or(
                                                        plugin.getPlatform().getPlayer(item.executor()).getName(),
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

    public abstract boolean supportsMenu();
    public abstract void openMenu(AbstractPlayer<?> player, List<HistoryRecord> records);

    @Override
    public List<String> handleTabCompletion(AbstractSender<?> sender, String[] args) {
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

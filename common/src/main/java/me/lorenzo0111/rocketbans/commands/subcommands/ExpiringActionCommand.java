package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.Table;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

public class ExpiringActionCommand<T extends ExpiringRecord> extends SubCommand {
    private final String name;
    private final Class<T> type;
    private final BiConsumer<T, AbstractPlayer<?>> action;

    public ExpiringActionCommand(RocketBansCommand command,
                                 String name, Class<T> type, @Nullable BiConsumer<T, AbstractPlayer<?>> action) {
        super(command);
        this.name = name;
        this.type = type;
        this.action = action;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(AbstractSender<?> sender, String[] args) {
        AbstractPlayer<?> target = plugin.getPlatform().getPlayer(args[0]);
        if (target == null || !target.isOnline()) throw new UsageException();

        long duration = -1;
        StringBuilder reason = new StringBuilder("N/A");

        if (args.length > 1) {
            duration = TimeUtils.parseTime(args[1]);
            if (duration <= 0) duration = -1;

            reason = new StringBuilder();
            for (int i = duration == -1 ? 1 : 2;
                 i < (args[args.length - 1].equalsIgnoreCase("-s") ?
                         args.length - 1 : args.length); i++) {
                reason.append(args[i]).append(" ");
            }
        }

        reason = new StringBuilder(StringUtils.color(reason.toString().trim()));

        Table table = Table.fromClass(this.type);
        if (table == null) return;

        T item = table.create(
                -1,
                target.getUniqueId(),
                reason.toString(),
                sender.getUniqueId(),
                new Timestamp(new Date().getTime()),
                duration == -1 ? null : new Timestamp(new Date().getTime() + duration),
                true
        );

        plugin.getDatabase().add(item)
                .thenAccept(id -> action.accept((T) item.withId(id), target));

        String message;

        if (duration == -1)
            message = plugin.getPrefixed(name + ".permanent")
                    .replace("%player%", target.getName())
                    .replace("%reason%", item.reason());
        else
            message = plugin.getPrefixed(name + ".temp")
                    .replace("%player%", target.getName())
                    .replace("%reason%", item.reason())
                    .replace("%duration%", TimeUtils.formatTime(duration));

        if (args[args.length - 1].equalsIgnoreCase("-s")) {
            sender.sendMessage(message);
        } else {
            plugin.getPlatform().broadcast(message);
        }
    }

    @Override
    public List<String> handleTabCompletion(AbstractSender<?> sender, String[] args) {
        return playerNames();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return StringUtils.capitalize(name) + " a player";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return name + " <player> [time] [reason]";
    }

    @Override
    public String getPermission() {
        return "rocketbans." + name;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"temp" + name};
    }
}

package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.api.data.Table;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DeleteCommand extends SubCommand {

    public DeleteCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        Table table;
        int id;

        try {
            table = Table.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getPrefixed("invalid-type"));
            return;
        }

        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getPrefixed("invalid-id"));
            return;
        }

        plugin.getDatabase().delete(table.getClazz(), id);
        sender.sendMessage(plugin.getPrefixed("deleted"));
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return Arrays.stream(Table.values())
                .map(Table::getName)
                .map(String::toLowerCase)
                .toList();
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Delete an history record";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "delete <type> <id>";
    }

    @Override
    public String getPermission() {
        return "rocketbans.delete";
    }
}

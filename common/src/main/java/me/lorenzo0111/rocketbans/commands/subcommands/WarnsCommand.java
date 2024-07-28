package me.lorenzo0111.rocketbans.commands.subcommands;

import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.gui.menus.HistoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarnsCommand extends SubCommand {

    public WarnsCommand(RocketBansCommand command) {
        super(command);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) throw new UsageException();

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        plugin.getDatabase().get(Warn.class, target.getUniqueId(), true)
                .thenAccept(warns -> new HistoryMenu(new ArrayList<>(warns)).open(player));
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return playerNames();
    }

    @Override
    public String getName() {
        return "warns";
    }

    @Override
    public String getDescription() {
        return "See a player's warns";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "warns <player>";
    }

    @Override
    public String getPermission() {
        return "rocketbans.history";
    }
}

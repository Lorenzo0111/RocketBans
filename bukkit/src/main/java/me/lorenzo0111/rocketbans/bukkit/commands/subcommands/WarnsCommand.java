package me.lorenzo0111.rocketbans.bukkit.commands.subcommands;

import me.lorenzo0111.rocketbans.bukkit.gui.menus.HistoryMenu;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.SubCommand;
import me.lorenzo0111.rocketbans.commands.exceptions.UsageException;
import me.lorenzo0111.rocketbans.api.data.records.Warn;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarnsCommand extends SubCommand {

    public WarnsCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public void handle(AbstractSender<?> sender, String[] args) {
        if (!(sender instanceof AbstractPlayer<?> player)) throw new UsageException();

        AbstractPlayer<?> target = plugin.getPlatform().getPlayer(args[0]);

        plugin.getDatabase().get(Warn.class, target.getUniqueId(), true)
                .thenAccept(warns -> new HistoryMenu(new ArrayList<>(warns)).open((Player) player.getHandle()));
    }

    @Override
    public List<String> handleTabCompletion(AbstractSender<?> sender, String[] args) {
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

package me.lorenzo0111.rocketbans.bukkit.commands.subcommands;

import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.subcommands.AbstractHistoryCommand;
import me.lorenzo0111.rocketbans.bukkit.gui.menus.HistoryMenu;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class HistoryCommand extends AbstractHistoryCommand {

    public HistoryCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public boolean supportsMenu() {
        return true;
    }

    @Override
    public void openMenu(AbstractPlayer<?> player, List<HistoryRecord> records) {
        new HistoryMenu(records).open((Player) player.getHandle());
    }

}

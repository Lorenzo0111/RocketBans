package me.lorenzo0111.rocketbans.velocity.commands.subcommands;

import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.commands.subcommands.AbstractHistoryCommand;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;

import java.util.List;

public class HistoryCommand extends AbstractHistoryCommand {

    public HistoryCommand(RocketBansCommand command) {
        super(command);
    }

    @Override
    public boolean supportsMenu() {
        return false;
    }

    @Override
    public void openMenu(AbstractPlayer<?> player, List<HistoryRecord> records) {
    }

}

package me.lorenzo0111.rocketbans.gui.menus;

import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.gui.items.history.HistoryItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryMenu extends BaseMenu {

    public HistoryMenu(@NotNull List<HistoryRecord> records) {
        super("history", true);

        for (HistoryRecord record : records) {
            this.addItem(new HistoryItem(record));
        }
    }

}

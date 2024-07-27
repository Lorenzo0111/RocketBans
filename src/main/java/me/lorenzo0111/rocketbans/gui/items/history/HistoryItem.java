package me.lorenzo0111.rocketbans.gui.items.history;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.data.Table;
import me.lorenzo0111.rocketbans.gui.items.ConfiguredItem;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class HistoryItem extends ConfiguredItem {
    private final HistoryRecord record;

    public HistoryItem(HistoryRecord record) {
        super("history");
        this.record = record;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        if (clickType.isLeftClick() && record instanceof ExpiringRecord expiring &&
                !expiring.expired()) {
            expiring.expire();
            player.closeInventory();
        }

        if (clickType.equals(ClickType.DROP) && player.hasPermission("rocketbans.delete")) {
            player.closeInventory();
            plugin.getDatabase().delete(record.getClass(), record.id());
            player.sendMessage(plugin.getPrefixed("deleted"));
        }
    }

    @Override
    public String replacePlaceholders(String origin) {
        Table table = Table.fromClass(record.getClass());
        if (table == null) return origin;

        origin = origin
                .replace("%type%", table.getName())
                .replace("%id%", String.valueOf(record.id()))
                .replace("%executor%", StringUtils.or(
                        record.executor().equals(RocketBans.CONSOLE_UUID) ? "Console" :
                                Bukkit.getOfflinePlayer(record.executor()).getName(), "Unknown"))
                .replace("%reason%", record.reason())
                .replace("%date%", TimeUtils.formatDate(record.date().getTime()));

        if (record instanceof ExpiringRecord expiring)
            origin = origin.replace("%duration%",
                    expiring.expires() == null ? "Permanent" :
                            TimeUtils.formatTime(
                                    expiring.expires().getTime() - expiring.date().getTime()
                            )
            ).replace("%active%",
                    expiring.active() ? "Active" : "Expired"
            );
        else origin = origin.replace("%duration%", "Permanent")
                .replace("%active%", "Active");

        return origin;
    }
}

package me.lorenzo0111.rocketbans.listeners;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;

public class PlayerListener implements Listener {
    private final RocketBans plugin;

    public PlayerListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        List<Ban> bans = plugin.getDatabase().get(Ban.class, event.getUniqueId(), true).join();
        if (bans.isEmpty()) return;

        Ban ban = bans.getFirst();
        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                String.join("\n",
                        plugin.getMessages("screens.ban")
                                .stream()
                                .map(s -> s.replace("%id%", String.valueOf(ban.id()))
                                        .replace("%executor%", StringUtils.or(
                                                ban.executor().equals(RocketBans.CONSOLE_UUID) ? "Console" :
                                                        Bukkit.getOfflinePlayer(ban.executor()).getName(), "Unknown"))
                                        .replace("%reason%", ban.reason())
                                        .replace("%date%", TimeUtils.formatDate(ban.date().getTime()))
                                        .replace("%duration%",
                                                ban.expires() == null ? "Permanent" :
                                                        TimeUtils.formatTime(
                                                                ban.expires().getTime() - ban.date().getTime()
                                                        )
                                        )
                                )
                                .toList()
                )
        );
    }
}

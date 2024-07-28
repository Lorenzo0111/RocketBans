package me.lorenzo0111.rocketbans.bungee.listeners;

import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.bungee.RocketBans;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {
    private final RocketBans plugin;

    public PlayerListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PreLoginEvent event) {
        if (!plugin.getBanManager().isBanned(event.getConnection().getUniqueId())) return;

        Ban ban = plugin.getBanManager().getBans().get(event.getConnection().getUniqueId());
        event.setCancelled(true);
        event.setCancelReason(
                new TextComponent(
                        String.join("\n",
                                plugin.getMessages("screens.ban")
                                        .stream()
                                        .map(s -> s.replace("%id%", String.valueOf(ban.id()))
                                                .replace("%executor%", StringUtils.or(
                                                        ban.executor().equals(RocketBans.CONSOLE_UUID) ? "Console" :
                                                                plugin.getPlatform().getPlayer(ban.executor()).getName(), "Unknown"))
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
                )
        );
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;
        if (!plugin.getMuteManager().getMutes().containsKey(player.getUniqueId())) return;

        Mute mute = plugin.getMuteManager().getMutes().get(player.getUniqueId());
        if (mute.expired() && mute.active())
            mute.expire();

        event.setCancelled(true);
        player.sendMessage(new TextComponent(plugin.getPrefixed("mute.deny")));
    }
}

package me.lorenzo0111.rocketbans.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import me.lorenzo0111.rocketbans.utils.TimeUtils;
import me.lorenzo0111.rocketbans.velocity.RocketBans;
import net.kyori.adventure.text.Component;

public class PlayerListener {
    private final RocketBans plugin;

    public PlayerListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(PreLoginEvent event) {
        if (!plugin.getBanManager().isBanned(event.getUniqueId())) return;

        Ban ban = plugin.getBanManager().getBans().get(event.getUniqueId());
        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text(
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
                )
        );
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (!plugin.getMuteManager().getMutes().containsKey(event.getPlayer().getUniqueId())) return;

        Mute mute = plugin.getMuteManager().getMutes().get(event.getPlayer().getUniqueId());
        if (mute.expired() && mute.active())
            mute.expire();

        event.setResult(PlayerChatEvent.ChatResult.denied());
        event.getPlayer().sendMessage(Component.text(plugin.getPrefixed("mute.deny")));
    }
}

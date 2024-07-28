package me.lorenzo0111.rocketbans.bukkit.listeners;

import me.lorenzo0111.rocketbans.bukkit.RocketBans;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class ChannelListener implements PluginMessageListener {
    private final RocketBans plugin;

    public ChannelListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        plugin.getMuteManager().reload();
    }
}

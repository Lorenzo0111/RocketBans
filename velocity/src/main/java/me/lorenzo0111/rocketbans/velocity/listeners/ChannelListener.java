package me.lorenzo0111.rocketbans.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import me.lorenzo0111.rocketbans.velocity.RocketBans;

public class ChannelListener {
    private final RocketBans plugin;

    public ChannelListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player)) return;
        if (!event.getIdentifier().equals(RocketBans.IDENTIFIER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        plugin.getMuteManager().reload();
    }
}

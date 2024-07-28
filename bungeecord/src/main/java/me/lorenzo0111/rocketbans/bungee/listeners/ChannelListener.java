package me.lorenzo0111.rocketbans.bungee.listeners;

import me.lorenzo0111.rocketbans.bungee.RocketBans;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChannelListener implements Listener {
    private final RocketBans plugin;

    public ChannelListener(RocketBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        if (!event.getTag().equalsIgnoreCase("rocketbans:sync")) return;

        event.setCancelled(true);
        plugin.getMuteManager().reload();
    }
}

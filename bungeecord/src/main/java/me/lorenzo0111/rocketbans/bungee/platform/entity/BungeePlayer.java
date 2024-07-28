package me.lorenzo0111.rocketbans.bungee.platform.entity;

import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeePlayer extends AbstractPlayer<ProxiedPlayer> {
    private final ProxiedPlayer player;

    protected BungeePlayer(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        if (player == null) return "Unknown";

        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        if (player == null) return;

        player.sendMessage(new TextComponent(message));
    }

    @Override
    public UUID getUniqueId() {
        if (player != null) return player.getUniqueId();
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (player == null) return false;

        return player.hasPermission(permission);
    }

    @Override
    public ProxiedPlayer getHandle() {
        return player;
    }

    @Override
    public boolean isOnline() {
        return player != null;
    }
}

package me.lorenzo0111.rocketbans.platform.entity;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class BukkitPlayer extends AbstractPlayer<OfflinePlayer> {
    private final OfflinePlayer player;

    protected BukkitPlayer(OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        if (player.isOnline() && player.getPlayer() != null)
            player.getPlayer().sendMessage(message);
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public boolean hasPermission(String permission) {
        if (player.isOnline() && player.getPlayer() != null)
            return player.getPlayer().hasPermission(permission);

        return false;
    }

    @Override
    public OfflinePlayer getHandle() {
        return player;
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
}

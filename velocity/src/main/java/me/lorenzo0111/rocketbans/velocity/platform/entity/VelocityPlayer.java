package me.lorenzo0111.rocketbans.velocity.platform.entity;

import com.velocitypowered.api.proxy.Player;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocityPlayer extends AbstractPlayer<Player> {
    private final Player player;

    protected VelocityPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String getName() {
        if (player == null) return "Unknown";

        return player.getUsername();
    }

    @Override
    public void sendMessage(String message) {
        if (player == null) return;

        player.sendMessage(Component.text(message));
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
    public Player getHandle() {
        return player;
    }

    @Override
    public boolean isOnline() {
        return player != null;
    }
}

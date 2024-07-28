package me.lorenzo0111.rocketbans.velocity.platform.entity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocitySender extends AbstractSender<CommandSource> {
    private final CommandSource sender;

    protected VelocitySender(CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(Component.text(message));
    }

    @Override
    public UUID getUniqueId() {
        return sender instanceof Player player ?
                player.getUniqueId() : RocketBansAPI.CONSOLE_UUID;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public CommandSource getHandle() {
        return sender;
    }
}

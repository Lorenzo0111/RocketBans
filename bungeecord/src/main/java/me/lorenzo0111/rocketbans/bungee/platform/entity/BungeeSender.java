package me.lorenzo0111.rocketbans.bungee.platform.entity;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeSender extends AbstractSender<CommandSender> {
    private final CommandSender sender;

    protected BungeeSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(new TextComponent(message));
    }

    @Override
    public UUID getUniqueId() {
        return sender instanceof ProxiedPlayer player ?
                player.getUniqueId() : RocketBansAPI.CONSOLE_UUID;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public CommandSender getHandle() {
        return sender;
    }
}

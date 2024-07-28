package me.lorenzo0111.rocketbans.platform.entity;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitSender extends AbstractSender<CommandSender> {
    private final CommandSender sender;

    protected BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
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
    public CommandSender getHandle() {
        return sender;
    }
}

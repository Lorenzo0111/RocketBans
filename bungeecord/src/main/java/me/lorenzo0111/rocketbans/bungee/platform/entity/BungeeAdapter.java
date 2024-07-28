package me.lorenzo0111.rocketbans.bungee.platform.entity;

import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BungeeAdapter {

    public static ProxiedPlayer player(@NotNull AbstractPlayer<?> player) {
        return (ProxiedPlayer) player.getHandle();
    }

    @Contract("_ -> new")
    public static @NotNull AbstractPlayer<?> player(ProxiedPlayer player) {
        return new BungeePlayer(player);
    }

    public static CommandSender sender(@NotNull AbstractSender<?> sender) {
        return (CommandSender) sender.getHandle();
    }

    @Contract("null -> new")
    public static @NotNull AbstractSender<?> sender(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return new BungeePlayer(player);
        }

        return new BungeeSender(sender);
    }

}

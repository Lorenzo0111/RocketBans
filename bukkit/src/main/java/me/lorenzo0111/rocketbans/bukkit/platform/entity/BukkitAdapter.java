package me.lorenzo0111.rocketbans.bukkit.platform.entity;

import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BukkitAdapter {

    public static OfflinePlayer player(@NotNull AbstractPlayer<?> player) {
        return (OfflinePlayer) player.getHandle();
    }

    @Contract("_ -> new")
    public static @NotNull AbstractPlayer<?> player(OfflinePlayer player) {
        return new BukkitPlayer(player);
    }

    public static CommandSender sender(@NotNull AbstractSender<?> sender) {
        return (CommandSender) sender.getHandle();
    }

    @Contract("null -> new")
    public static @NotNull AbstractSender<?> sender(CommandSender sender) {
        if (sender instanceof OfflinePlayer player) {
            return new BukkitPlayer(player);
        }

        return new BukkitSender(sender);
    }

}

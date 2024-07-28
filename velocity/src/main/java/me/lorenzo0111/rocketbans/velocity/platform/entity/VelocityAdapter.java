package me.lorenzo0111.rocketbans.velocity.platform.entity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.AbstractSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VelocityAdapter {

    public static Player player(@NotNull AbstractPlayer<?> player) {
        return (Player) player.getHandle();
    }

    @Contract("_ -> new")
    public static @NotNull AbstractPlayer<?> player(Player player) {
        return new VelocityPlayer(player);
    }

    public static CommandSource sender(@NotNull AbstractSender<?> sender) {
        return (CommandSource) sender.getHandle();
    }

    @Contract("null -> new")
    public static @NotNull AbstractSender<?> sender(CommandSource sender) {
        if (sender instanceof Player player) {
            return new VelocityPlayer(player);
        }

        return new VelocitySender(sender);
    }

}

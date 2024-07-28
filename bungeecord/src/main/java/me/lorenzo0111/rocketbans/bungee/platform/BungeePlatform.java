package me.lorenzo0111.rocketbans.bungee.platform;

import me.lorenzo0111.rocketbans.bungee.RocketBans;
import me.lorenzo0111.rocketbans.bungee.platform.entity.BungeeAdapter;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class BungeePlatform implements PlatformAdapter {
    private final RocketBans plugin;

    public BungeePlatform(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public String nativeColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public String nativeColorOf(String message) {
        return ChatColor.of(message).toString();
    }

    @Override
    public void logException(Throwable e) {
        plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred", e);
    }

    @Override
    public void async(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void dispatchCommand(String command) {
        plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
    }

    @Override
    public void broadcast(String message) {
        plugin.getProxy().broadcast(new TextComponent(message));
    }

    @Override
    public void unban(AbstractPlayer<?> player) {
        // TODO
    }

    @Override
    public void ban(AbstractPlayer<?> player, String reason, Date duration, UUID executor) {
        // TODO
    }

    @Override
    public void kick(AbstractPlayer<?> player, String reason) {
        adapt(player).disconnect(new TextComponent(reason));
    }

    @Override
    public List<AbstractPlayer<?>> getBanList() {
        // todo
        return new ArrayList<>();
    }

    @Override
    public List<AbstractPlayer<?>> getPlayerList() {
        return new ArrayList<>(plugin.getProxy().getPlayers().stream()
                .map(BungeeAdapter::player)
                .toList());
    }

    @Override
    public AbstractPlayer<?> getPlayer(String name) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(name);
        return BungeeAdapter.player(player);
    }

    @Override
    public AbstractPlayer<?> getPlayer(UUID uuid) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
        return BungeeAdapter.player(player);
    }

    private ProxiedPlayer adapt(@NotNull AbstractPlayer<?> player) {
        return (ProxiedPlayer) player.getHandle();
    }
}

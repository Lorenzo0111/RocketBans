package me.lorenzo0111.rocketbans.platform;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.platform.entity.BukkitAdapter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class BukkitPlatform implements PlatformAdapter {
    private final RocketBans plugin;

    public BukkitPlatform(RocketBans plugin) {
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }

    @Override
    public void unban(AbstractPlayer<?> player) {
        ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                .pardon(adapt(player).getPlayerProfile());
    }

    @Override
    public void ban(AbstractPlayer<?> player, String reason, Date duration, UUID executor) {
        OfflinePlayer adapt = adapt(player);

        if (adapt instanceof Player online)
            online.ban(
                    reason,
                    duration,
                    executor.toString(),
                    true
            );
        else adapt.ban(
                reason,
                duration,
                executor.toString()
        );

    }

    @Override
    public void kick(AbstractPlayer<?> player, String reason) {
        if (adapt(player) instanceof Player online)
            online.kickPlayer(reason);
    }

    @Override
    public List<AbstractPlayer<?>> getBanList() {
        return new ArrayList<>(((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE))
                .getEntries().stream()
                .map(entry -> getPlayer(entry.getBanTarget().getUniqueId()))
                .toList());
    }

    @Override
    public List<AbstractPlayer<?>> getPlayerList() {
        return new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                .map(BukkitAdapter::player)
                .toList());
    }

    @Override
    @SuppressWarnings("deprecation")
    public AbstractPlayer<?> getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        return BukkitAdapter.player(
                Objects.requireNonNullElseGet(player, () -> Bukkit.getOfflinePlayer(name)));
    }

    @Override
    public AbstractPlayer<?> getPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        return BukkitAdapter.player(
                Objects.requireNonNullElseGet(player, () -> Bukkit.getOfflinePlayer(uuid)));
    }

    private OfflinePlayer adapt(@NotNull AbstractPlayer<?> player) {
        return (OfflinePlayer) player.getHandle();
    }
}

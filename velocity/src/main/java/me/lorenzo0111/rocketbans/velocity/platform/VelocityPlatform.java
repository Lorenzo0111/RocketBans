package me.lorenzo0111.rocketbans.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;
import me.lorenzo0111.rocketbans.velocity.RocketBans;
import me.lorenzo0111.rocketbans.velocity.platform.entity.VelocityAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VelocityPlatform implements PlatformAdapter {
    private final RocketBans plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public VelocityPlatform(RocketBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public String nativeColor(String message) {
        return serializer.serialize(serializer.deserialize(message));
    }

    @Override
    public String nativeColorOf(String message) {
        return message;
    }

    @Override
    public boolean supportsHex() {
        return false;
    }

    @Override
    public void logException(Throwable e) {
        plugin.getLogger().error("An unexpected error occurred", e);
    }

    @Override
    public void async(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public void dispatchCommand(String command) {
        plugin.getServer().getCommandManager().executeAsync(plugin.getServer().getConsoleCommandSource(), command);
    }

    @Override
    public void broadcast(String message) {
        Component component = serializer.deserialize(message);
        for (Player player : plugin.getServer().getAllPlayers()) {
            player.sendMessage(component);
        }
    }

    @Override
    public void unban(AbstractPlayer<?> player) {
        plugin.getDatabase().expireAll(Ban.class, player.getUniqueId());
        plugin.getBanManager().removeBan(player.getUniqueId());
    }

    @Override
    public void ban(AbstractPlayer<?> player, String reason, Date duration, UUID executor) {
        plugin.getDatabase().add(new Ban(
                -1,
                player.getUniqueId(),
                reason,
                executor,
                new Timestamp(new Date().getTime()),
                new Timestamp(duration.getTime()),
                true
        ));
    }

    @Override
    public void kick(AbstractPlayer<?> player, String reason) {
        adapt(player).disconnect(Component.text(reason));
    }

    @Override
    public List<AbstractPlayer<?>> getBanList() {
        return new ArrayList<>(plugin.getBanManager().getBans()
                .keySet().stream()
                .map(this::getPlayer)
                .toList());
    }

    @Override
    public List<AbstractPlayer<?>> getPlayerList() {
        return new ArrayList<>(plugin.getServer().getAllPlayers().stream()
                .map(VelocityAdapter::player)
                .toList());
    }

    @Override
    public AbstractPlayer<?> getPlayer(String name) {
        Player player = plugin.getServer().getPlayer(name).orElse(null);
        return VelocityAdapter.player(player);
    }

    @Override
    public AbstractPlayer<?> getPlayer(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid).orElse(null);
        return VelocityAdapter.player(player);
    }

    private Player adapt(@NotNull AbstractPlayer<?> player) {
        return (Player) player.getHandle();
    }
}

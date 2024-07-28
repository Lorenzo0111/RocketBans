package me.lorenzo0111.rocketbans.platform;

import me.lorenzo0111.rocketbans.platform.entity.AbstractPlayer;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PlatformAdapter {
    String nativeColor(String message);
    String nativeColorOf(String message);
    boolean supportsHex();

    void logException(Throwable exception);
    void async(Runnable runnable);
    void dispatchCommand(String command);
    void broadcast(String message);

    void unban(AbstractPlayer<?> player);
    void ban(AbstractPlayer<?> player, String reason, Date duration, UUID executor);
    void kick(AbstractPlayer<?> player, String reason);
    List<AbstractPlayer<?>> getBanList();

    List<AbstractPlayer<?>> getPlayerList();
    AbstractPlayer<?> getPlayer(String name);
    AbstractPlayer<?> getPlayer(UUID uuid);

    void sendSyncPacket(String data);
}

package me.lorenzo0111.rocketbans;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.entity.AbstractPlayer;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public interface RocketBansPlugin extends RocketBansAPI {

    SQLHandler getDatabase();
    ConfigurationNode getConfig();

    String nativeColor(String message);
    String nativeColorOf(String message);

    void logException(Throwable exception);
    void async(Runnable runnable);

    List<AbstractPlayer> getPlayerList();
}

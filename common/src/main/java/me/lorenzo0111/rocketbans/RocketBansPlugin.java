package me.lorenzo0111.rocketbans;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;
import me.lorenzo0111.rocketbans.data.SQLHandler;
import me.lorenzo0111.rocketbans.platform.PlatformAdapter;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public interface RocketBansPlugin extends RocketBansAPI {

    SQLHandler getDatabase();
    ConfigurationNode getConfiguration();
    PlatformAdapter getPlatform();

    String getMessage(String path);
    List<String> getMessages(String path);
    String getPrefixed(String path);

    void reload();

    String getVersion();
}

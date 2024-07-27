package me.lorenzo0111.rocketbans.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface RocketBansAPI {


    static @NotNull RocketBansAPI get() {
        return (RocketBansAPI) JavaPlugin.getProvidingPlugin(RocketBansAPI.class);
    }
}

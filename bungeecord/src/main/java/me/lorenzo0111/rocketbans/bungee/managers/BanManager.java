package me.lorenzo0111.rocketbans.bungee.managers;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.data.records.Ban;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {
    private final RocketBansPlugin plugin;
    private final Map<UUID, Ban> activeBans = new HashMap<>();

    public BanManager(RocketBansPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.getDatabase().getActive(Ban.class).thenAccept(bans -> {
            activeBans.clear();

            for (Ban ban : bans) {
                activeBans.put(ban.uuid(), ban);
            }
        });
    }

    public void addBan(Ban ban) {
        activeBans.put(ban.uuid(), ban);
    }

    public void removeBan(UUID uuid) {
        activeBans.remove(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return activeBans.containsKey(uuid);
    }

    public Map<UUID, Ban> getBans() {
        return activeBans;
    }
}

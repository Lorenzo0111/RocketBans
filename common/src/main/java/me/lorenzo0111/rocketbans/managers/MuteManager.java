package me.lorenzo0111.rocketbans.managers;

import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.managers.IMuteManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager implements IMuteManager {
    private final RocketBansPlugin plugin;
    private final Map<UUID, Mute> activeMutes = new HashMap<>();

    public MuteManager(RocketBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        plugin.getDatabase().getActive(Mute.class).thenAccept(mutes -> {
            for (Mute mute : mutes) {
                activeMutes.put(mute.uuid(), mute);
            }
        });
    }

    @Override
    public void addMute(Mute mute) {
        activeMutes.put(mute.uuid(), mute);
    }

    @Override
    public void removeMutes(UUID uuid) {
        activeMutes.remove(uuid);
    }

    @Override
    public Map<UUID, Mute> getMutes() {
        return activeMutes;
    }
}

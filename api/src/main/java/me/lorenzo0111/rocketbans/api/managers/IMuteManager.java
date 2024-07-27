package me.lorenzo0111.rocketbans.api.managers;

import me.lorenzo0111.rocketbans.api.data.records.Mute;

import java.util.UUID;

public interface IMuteManager {
    void reload();
    void addMute(Mute mute);
    void removeMutes(UUID uuid);
}

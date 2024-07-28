package me.lorenzo0111.rocketbans.platform.entity;

import me.lorenzo0111.rocketbans.api.RocketBansAPI;

import java.util.UUID;

public abstract class AbstractSender<T> {

    public abstract void sendMessage(String message);

    public UUID getUniqueId() {
        return RocketBansAPI.CONSOLE_UUID;
    }

    public boolean hasPermission(String permission) {
        return true;
    }

    public abstract T getHandle();

}

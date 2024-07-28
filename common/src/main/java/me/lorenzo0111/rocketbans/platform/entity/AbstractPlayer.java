package me.lorenzo0111.rocketbans.platform.entity;

import java.util.UUID;

public abstract class AbstractPlayer<T> extends AbstractSender<T> {

    public abstract String getName();
    @Override public abstract UUID getUniqueId();
    @Override public abstract boolean hasPermission(String permission);
    public abstract boolean isOnline();

}

package me.lorenzo0111.rocketbans.data;

import me.lorenzo0111.rocketbans.RocketBans;

import java.sql.Timestamp;
import java.util.UUID;

public record Mute(
        int id,
        UUID uuid,
        String reason,
        UUID executor,
        Timestamp date,
        Timestamp expires,
        boolean active
) {
    public boolean expired() {
        if (expires == null) return false;

        return expires.before(new Timestamp(System.currentTimeMillis()));
    }

    public void expire() {
        RocketBans.getInstance()
                .getDatabase()
                .expireBan(this.id);
    }
}

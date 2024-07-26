package me.lorenzo0111.rocketbans.data;

import java.sql.Timestamp;
import java.util.UUID;

public record Ban(
        int id,
        UUID uuid,
        String reason,
        UUID executor,
        Timestamp date,
        Timestamp expires
) {
    boolean expired() {
        return System.currentTimeMillis() > expires.getTime();
    }
}

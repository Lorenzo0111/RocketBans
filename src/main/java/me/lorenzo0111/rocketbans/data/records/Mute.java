package me.lorenzo0111.rocketbans.data.records;

import me.lorenzo0111.rocketbans.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.data.HistoryRecord;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
) implements HistoryRecord, ExpiringRecord {
    @Contract("_ -> new")
    public @NotNull Mute withId(int id) {
        return new Mute(id, uuid, reason, executor, date, expires, active);
    }
}

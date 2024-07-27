package me.lorenzo0111.rocketbans.api.data.records;

import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;

public record Mute(
        int id,
        UUID uuid,
        String reason,
        UUID executor,
        Timestamp date,
        @Nullable Timestamp expires,
        boolean active
) implements HistoryRecord, ExpiringRecord {

}

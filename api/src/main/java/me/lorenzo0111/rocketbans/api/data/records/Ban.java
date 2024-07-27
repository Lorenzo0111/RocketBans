package me.lorenzo0111.rocketbans.api.data.records;

import me.lorenzo0111.rocketbans.api.data.ExpiringRecord;
import me.lorenzo0111.rocketbans.api.data.HistoryRecord;

import java.sql.Timestamp;
import java.util.UUID;

public record Ban(
        int id,
        UUID uuid,
        String reason,
        UUID executor,
        Timestamp date,
        Timestamp expires,
        boolean active
) implements HistoryRecord, ExpiringRecord {

}

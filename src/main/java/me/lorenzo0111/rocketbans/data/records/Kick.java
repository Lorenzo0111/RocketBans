package me.lorenzo0111.rocketbans.data.records;

import me.lorenzo0111.rocketbans.data.HistoryRecord;

import java.sql.Timestamp;
import java.util.UUID;

public record Kick(
        int id,
        UUID uuid,
        String reason,
        UUID executor,
        Timestamp date
) implements HistoryRecord {

}

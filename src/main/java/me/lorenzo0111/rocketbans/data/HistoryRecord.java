package me.lorenzo0111.rocketbans.data;

import java.sql.Timestamp;
import java.util.UUID;

public interface HistoryRecord {

    int id();
    UUID uuid();
    String reason();
    UUID executor();
    Timestamp date();

}

package me.lorenzo0111.rocketbans.api;

import me.lorenzo0111.rocketbans.api.data.HistoryRecord;
import me.lorenzo0111.rocketbans.api.managers.IMuteManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface RocketBansAPI {
    UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    <T extends HistoryRecord> void punish(T item);
    <T extends HistoryRecord> void expire(Class<T> type, UUID uuid);
    <T extends HistoryRecord> void expire(Class<T> type, int id);
    CompletableFuture<List<HistoryRecord>> history(@Nullable Class<? extends HistoryRecord> type, UUID uuid);

    IMuteManager getMuteManager();
}

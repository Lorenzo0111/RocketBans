package me.lorenzo0111.rocketbans.data;

import java.sql.Timestamp;
import java.util.UUID;

public interface HistoryRecord {

    int id();
    UUID uuid();
    String reason();
    UUID executor();
    Timestamp date();

    default HistoryRecord withId(int id) {
        Table table = Table.fromClass(this.getClass());
        assert table != null;

        return table.create(
                id,
                this.uuid(),
                this.reason(),
                this.executor(),
                this.date()
        );
    }

}

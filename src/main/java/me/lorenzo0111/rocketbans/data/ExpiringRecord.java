package me.lorenzo0111.rocketbans.data;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.data.records.Mute;
import org.jetbrains.annotations.Contract;

import java.sql.Timestamp;

public interface ExpiringRecord extends HistoryRecord {

    Timestamp expires();
    boolean active();

    default boolean expired() {
        if (expires() == null) return false;

        return expires().before(new Timestamp(System.currentTimeMillis()));
    }

    default void expire() {
        RocketBans.getInstance()
                .getDatabase()
                .expireSingle(this.getClass(), id());

        if (this instanceof Mute mute)
            RocketBans.getInstance()
                    .getMuteManager()
                    .removeMute(mute);
    }

    @Contract("_ -> new")
    @Override
    default HistoryRecord withId(int id) {
        Table table = Table.fromClass(this.getClass());
        assert table != null;

        return table.create(
                id,
                this.uuid(),
                this.reason(),
                this.executor(),
                this.date(),
                this.expires(),
                this.active()
        );
    }

}

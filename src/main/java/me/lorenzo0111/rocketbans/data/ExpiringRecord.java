package me.lorenzo0111.rocketbans.data;

import me.lorenzo0111.rocketbans.RocketBans;
import me.lorenzo0111.rocketbans.data.records.Mute;

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

}

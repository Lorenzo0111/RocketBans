package me.lorenzo0111.rocketbans.api.data;

import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public interface ExpiringRecord extends HistoryRecord {

    @Nullable
    @Contract(pure = true)
    Timestamp expires();

    boolean active();

    default boolean expired() {
        Timestamp expires = expires();
        if (expires == null) return false;

        return expires.before(new Timestamp(System.currentTimeMillis()));
    }

    default void expire() {
        RocketBansProvider.get().expire(this.getClass(), id());
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

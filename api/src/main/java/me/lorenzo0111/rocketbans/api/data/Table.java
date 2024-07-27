package me.lorenzo0111.rocketbans.api.data;

import me.lorenzo0111.rocketbans.api.data.records.Ban;
import me.lorenzo0111.rocketbans.api.data.records.Kick;
import me.lorenzo0111.rocketbans.api.data.records.Mute;
import me.lorenzo0111.rocketbans.api.data.records.Warn;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public enum Table {
    BANS("bans", Ban.class),
    KICKS("kicks", Kick.class),
    MUTES("mutes", Mute.class),
    WARNS("warns", Warn.class);

    private final String name;
    private final Class<? extends HistoryRecord> clazz;

    Table(String name, Class<? extends HistoryRecord> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<? extends HistoryRecord> getClazz() {
        return clazz;
    }

    @SuppressWarnings("unchecked")
    public <T extends HistoryRecord> T create(Object... items) {
        Object[] newItems = new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                break;
            }
            newItems[i] = items[i];
        }

        try {
            Constructor<?> constructor = this.clazz.getConstructors()[0];
            return (T) constructor.newInstance(newItems);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isExpiring() {
        return Arrays.asList(this.clazz.getInterfaces())
                .contains(ExpiringRecord.class);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Table fromClass(Class<? extends HistoryRecord> clazz) {
        for (Table table : Table.values()) {
            if (table.getClazz().equals(clazz)) {
                return table;
            }
        }
        return null;
    }
}

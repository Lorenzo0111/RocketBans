package me.lorenzo0111.rocketbans.utils;

import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    public static long parseTime(String time) {
        long result = 0;
        long current = 0;
        for (char c : time.toCharArray()) {
            if (Character.isDigit(c)) {
                current = current * 10 + Character.getNumericValue(c);
            } else {
                switch (c) {
                    case 'd':
                        result += TimeUnit.DAYS.toMillis(current);
                        break;
                    case 'h':
                        result += TimeUnit.HOURS.toMillis(current);
                        break;
                    case 'm':
                        result += TimeUnit.MINUTES.toMillis(current);
                        break;
                    case 's':
                        result += TimeUnit.SECONDS.toMillis(current);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time format: " + time);
                }
                current = 0;
            }
        }
        return result;
    }

    public static String formatTime(long time) {
        long days = TimeUnit.MILLISECONDS.toDays(time);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString().trim();
    }

}

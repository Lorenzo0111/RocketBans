package me.lorenzo0111.rocketbans.utils;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([ywMmdhs])");

    public static long parseTime(String time) {
        Matcher matcher = TIME_PATTERN.matcher(time);
        long duration = 0;

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 'y':
                    duration += TimeUnit.DAYS.toMillis(value * 365L);
                    break;
                case 'M':
                    duration += TimeUnit.DAYS.toMillis(value * 30L);
                    break;
                case 'w':
                    duration += TimeUnit.DAYS.toMillis(value * 7L);
                    break;
                case 'd':
                    duration += TimeUnit.DAYS.toMillis(value);
                    break;
                case 'h':
                    duration += TimeUnit.HOURS.toMillis(value);
                    break;
                case 'm':
                    duration += TimeUnit.MINUTES.toMillis(value);
                    break;
                case 's':
                    duration += TimeUnit.SECONDS.toMillis(value);
                    break;
            }
        }

        return duration;
    }

    public static String formatTime(long time) {
        long years = TimeUnit.MILLISECONDS.toDays(time) / 365;
        time -= TimeUnit.DAYS.toMillis(years * 365);

        long months = TimeUnit.MILLISECONDS.toDays(time) / 30;
        time -= TimeUnit.DAYS.toMillis(months * 30);

        long weeks = TimeUnit.MILLISECONDS.toDays(time) / 7;
        time -= TimeUnit.DAYS.toMillis(weeks * 7);

        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("y ");
        if (months > 0) sb.append(months).append("M ");
        if (weeks > 0) sb.append(weeks).append("w ");
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatDate(long time) {
        return DATE_FORMAT.format(time);
    }

}

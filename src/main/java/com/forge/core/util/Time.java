package com.forge.core.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Duration parsing and formatting. Accepts inputs like {@code 90},
 * {@code 10m}, {@code 2h30m}, {@code 1d}.
 */
public final class Time {
    private static final Pattern TOKEN = Pattern.compile("(\\d+)([smhdw]?)");

    private Time() {
    }

    /**
     * Parse a duration string to seconds. Bare numbers are seconds.
     *
     * @throws IllegalArgumentException when the input is not a duration
     */
    public static long parseSeconds(String input) {
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Empty duration.");
        }
        Matcher matcher = TOKEN.matcher(text);
        long total = 0;
        int pos = 0;
        while (matcher.find()) {
            if (matcher.start() != pos) {
                throw new IllegalArgumentException("Bad duration: " + input);
            }
            pos = matcher.end();
            long value = Long.parseLong(matcher.group(1));
            total += switch (matcher.group(2)) {
                case "w" -> value * 604800;
                case "d" -> value * 86400;
                case "h" -> value * 3600;
                case "m" -> value * 60;
                default -> value;
            };
        }
        if (pos != text.length() || total <= 0) {
            throw new IllegalArgumentException("Bad duration: " + input);
        }
        return total;
    }

    /** Format seconds as a compact duration, e.g. {@code 2d 3h 5m}. */
    public static String format(long totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }
        long weeks = totalSeconds / 604800;
        long days = (totalSeconds % 604800) / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder out = new StringBuilder();
        if (weeks > 0) {
            out.append(weeks).append('w').append(' ');
        }
        if (days > 0) {
            out.append(days).append('d').append(' ');
        }
        if (hours > 0) {
            out.append(hours).append('h').append(' ');
        }
        if (minutes > 0) {
            out.append(minutes).append('m').append(' ');
        }
        if (seconds > 0 || out.isEmpty()) {
            out.append(seconds).append('s');
        }
        return out.toString().trim();
    }

    /** Format a millisecond timestamp as {@code 2026-10-04 08:30}. */
    public static String formatDate(long epochMillis) {
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(epochMillis));
    }
}

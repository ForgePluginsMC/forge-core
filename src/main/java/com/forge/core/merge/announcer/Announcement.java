package com.forge.core.merge.announcer;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable holder for one configured announcement.
 * Parsed from a single entry of the {@code announcements:} list in
 * {@code announcer.yml}.
 */
final class Announcement {

    enum Mode {
        SEQUENTIAL,
        RANDOM
    }

    enum Delivery {
        CHAT,
        ACTIONBAR,
        BOSSBAR,
        TITLE
    }

    private final String id;
    private final List<String> messages;
    private final long intervalSeconds;
    private final Mode mode;
    private final Delivery delivery;
    private final BossBar.Color bossbarColor;
    private final long bossbarSeconds;
    private final long titleFadeIn;
    private final long titleStay;
    private final long titleFadeOut;

    private Announcement(String id, List<String> messages, long intervalSeconds, Mode mode,
            Delivery delivery, BossBar.Color bossbarColor, long bossbarSeconds,
            long titleFadeIn, long titleStay, long titleFadeOut) {
        this.id = id;
        this.messages = messages;
        this.intervalSeconds = intervalSeconds;
        this.mode = mode;
        this.delivery = delivery;
        this.bossbarColor = bossbarColor;
        this.bossbarSeconds = bossbarSeconds;
        this.titleFadeIn = titleFadeIn;
        this.titleStay = titleStay;
        this.titleFadeOut = titleFadeOut;
    }

    /**
     * Parses one {@code announcements:} list entry. Returns {@code null} when
     * the entry is unusable (no id, no messages, or non-positive interval).
     */
    static @Nullable Announcement fromMap(Map<?, ?> entry) {
        Object rawId = entry.get("id");
        if (!(rawId instanceof String id) || id.isBlank()) {
            return null;
        }
        List<String> messages = stringList(entry.get("messages"));
        if (messages.isEmpty()) {
            return null;
        }
        long interval = longValue(entry.get("interval-seconds"), 300L);
        if (interval <= 0) {
            return null;
        }
        Mode mode = parseEnum(Mode.class, stringValue(entry.get("mode")), Mode.SEQUENTIAL);
        Delivery delivery = parseEnum(Delivery.class, stringValue(entry.get("delivery")), Delivery.CHAT);
        BossBar.Color color = parseEnum(BossBar.Color.class, stringValue(entry.get("bossbar-color")), BossBar.Color.BLUE);
        long bossbarSeconds = Math.max(1L, longValue(entry.get("bossbar-seconds"), 8L));
        long fadeIn = Math.max(0L, longValue(entry.get("title-fade-in"), 10L));
        long stay = Math.max(0L, longValue(entry.get("title-stay"), 70L));
        long fadeOut = Math.max(0L, longValue(entry.get("title-fade-out"), 20L));
        return new Announcement(id, List.copyOf(messages), interval, mode, delivery,
                color, bossbarSeconds, fadeIn, stay, fadeOut);
    }

    private static List<String> stringList(@Nullable Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private static @Nullable String stringValue(@Nullable Object raw) {
        return raw instanceof String s ? s : null;
    }

    private static long longValue(@Nullable Object raw, long fallback) {
        return raw instanceof Number n ? n.longValue() : fallback;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, @Nullable String raw, E fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    String id() {
        return id;
    }

    List<String> messages() {
        return messages;
    }

    long intervalSeconds() {
        return intervalSeconds;
    }

    Mode mode() {
        return mode;
    }

    Delivery delivery() {
        return delivery;
    }

    BossBar.Color bossbarColor() {
        return bossbarColor;
    }

    long bossbarSeconds() {
        return bossbarSeconds;
    }

    long titleFadeIn() {
        return titleFadeIn;
    }

    long titleStay() {
        return titleStay;
    }

    long titleFadeOut() {
        return titleFadeOut;
    }
}

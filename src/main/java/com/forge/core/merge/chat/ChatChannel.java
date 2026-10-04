package com.forge.core.merge.chat;

import org.jspecify.annotations.Nullable;

/** Chat channels. */
public enum ChatChannel {
    GLOBAL("global"),
    LOCAL("local"),
    STAFF("staff");

    private final String key;

    ChatChannel(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String display() {
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    public static @Nullable ChatChannel fromString(@Nullable String input) {
        if (input == null) {
            return null;
        }
        return switch (input.trim().toLowerCase()) {
            case "g", "global" -> GLOBAL;
            case "l", "local" -> LOCAL;
            case "s", "sc", "staff" -> STAFF;
            default -> null;
        };
    }
}

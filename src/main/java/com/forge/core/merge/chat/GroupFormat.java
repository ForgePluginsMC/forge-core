package com.forge.core.merge.chat;

import net.kyori.adventure.text.Component;

/**
 * Per-group chat name formatting. Groups are an ordered config list; the
 * first group whose permission the player holds wins.
 */
public record GroupFormat(String permission, String label, Component prefix, Component suffix, String nameFormat) {

    public static GroupFormat fallback() {
        return new GroupFormat("", "Default", Component.empty(), Component.empty(), "{name}");
    }

    static GroupFormat load(java.util.Map<?, ?> entry) {
        String permission = string(entry.get("permission"), "");
        String label = string(entry.get("label"), permission.isEmpty() ? "Default" : permission);
        Component prefix = ChatText.safe(string(entry.get("prefix"), ""));
        Component suffix = ChatText.safe(string(entry.get("suffix"), ""));
        String nameFormat = string(entry.get("name-format"), "{name}");
        return new GroupFormat(permission, label, prefix, suffix, nameFormat);
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}

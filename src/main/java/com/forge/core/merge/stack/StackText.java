package com.forge.core.merge.stack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

/**
 * MiniMessage parsing with a safe fallback: a malformed admin template
 * degrades to plain text instead of breaking chat or throwing.
 *
 * <p>Separate from {@code com.forge.core.util.Text} because stack name
 * formats need MiniMessage {@link TagResolver}s ({@code <count>},
 * {@code <type>}), which the core helper does not support.
 */
public final class StackText {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private StackText() {
    }

    public static Component parse(@Nullable String input, TagResolver... resolvers) {
        if (input == null) {
            return Component.empty();
        }
        try {
            return MM.deserialize(input, resolvers);
        } catch (RuntimeException e) {
            return Component.text(stripTags(input));
        }
    }

    public static String stripTags(@Nullable String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("<[^>]*>", "");
    }
}

package com.forge.core.merge.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * MiniMessage helpers for chat templates. Bukkit-free.
 */
public final class ChatText {
    private ChatText() {
    }

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Deserialize MiniMessage, falling back to literal text when the input
     * contains malformed tags instead of throwing.
     */
    public static Component safe(String input) {
        try {
            return MM.deserialize(input);
        } catch (Exception e) {
            return Component.text(input);
        }
    }

    /**
     * Converts {@code {placeholder}} config style to MiniMessage
     * {@code <placeholder>} tags.
     */
    public static String bracesToTags(String template) {
        return template.replace("{tag}", "<tag>")
                .replace("{name}", "<name>")
                .replace("{message}", "<message>")
                .replace("{sender}", "<sender>")
                .replace("{recipient}", "<recipient>");
    }

    /**
     * Deserialize a template with tag resolvers, falling back to the given
     * component when the template has malformed tags instead of throwing.
     */
    public static Component render(String template, Component fallback, TagResolver... resolvers) {
        try {
            return resolvers.length == 0 ? MM.deserialize(template)
                    : MM.deserialize(template, TagResolver.resolver(resolvers));
        } catch (Exception e) {
            return fallback;
        }
    }
}

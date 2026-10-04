package com.forge.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * MiniMessage helpers. Every user-facing message goes through here so the
 * prefix, colors and error style stay consistent.
 */
public final class Text {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    /** Standard chat prefix. */
    public static final String PREFIX = "<gray>[<gold>ForgeCore<gray>]<reset> ";

    private Text() {
    }

    /** Deserialize MiniMessage markup. */
    public static Component of(String miniMessage) {
        return MINI.deserialize(miniMessage);
    }

    /** Escape untrusted input (nicknames, sign text, …) before interpolation. */
    public static String escape(String untrusted) {
        return MINI.escapeTags(untrusted);
    }

    /** Strip all formatting, returning plain text. */
    public static String strip(String miniMessage) {
        return PLAIN.serialize(MINI.deserialize(miniMessage));
    }

    /** Send a prefixed informational message. */
    public static void send(CommandSender sender, String miniMessage) {
        sender.sendMessage(MINI.deserialize(PREFIX + miniMessage));
    }

    /** Send a prefixed red error message. */
    public static void error(CommandSender sender, String miniMessage) {
        sender.sendMessage(MINI.deserialize(PREFIX + "<red>" + miniMessage));
    }

    /** Send a prefixed green success message. */
    public static void ok(CommandSender sender, String miniMessage) {
        sender.sendMessage(MINI.deserialize(PREFIX + "<green>" + miniMessage));
    }

    /** Standard usage hint: {@code Usage: /home [player]}. */
    public static void usage(CommandSender sender, String usage) {
        error(sender, "Usage: <gray>" + escape(usage));
    }

    /** Broadcast a prefixed message to the whole server. */
    public static void broadcast(String miniMessage) {
        org.bukkit.Bukkit.broadcast(MINI.deserialize(PREFIX + miniMessage));
    }
}

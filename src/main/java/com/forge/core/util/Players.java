package com.forge.core.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Player lookup helpers with Essentials-style matching: exact name first,
 * then a single case-insensitive prefix match.
 */
public final class Players {
    private Players() {
    }

    /**
     * Find an online player by name. Sends an error to {@code sender} and
     * returns null when nobody (or more than one prefix candidate) matches.
     */
    public static @Nullable Player find(CommandSender sender, String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) {
            return exact;
        }
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        List<Player> matches = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName().toLowerCase(java.util.Locale.ROOT);
            if (playerName.startsWith(lower)) {
                matches.add(player);
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.isEmpty()) {
            Text.error(sender, "Player <white>" + Text.escape(name) + "</white> is not online.");
        } else {
            Text.error(sender, "Multiple players match <white>" + Text.escape(name) + "</white>; be more specific.");
        }
        return null;
    }

    /** Find an online player without messaging anyone; null when ambiguous. */
    public static @Nullable Player findQuiet(String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) {
            return exact;
        }
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        Player match = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(java.util.Locale.ROOT).startsWith(lower)) {
                if (match != null) {
                    return null;
                }
                match = player;
            }
        }
        return match;
    }

    /** Best-effort offline lookup (exact name, has played before). */
    public static @Nullable OfflinePlayer offline(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return player.hasPlayedBefore() ? player : null;
    }

    /** Names of online players, for tab completion. */
    public static List<String> onlineNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    /** Filter a candidate list by the current (partial) argument, case-insensitive. */
    public static List<String> filter(List<String> candidates, String[] args) {
        if (args.length == 0) {
            return candidates;
        }
        String prefix = args[args.length - 1].toLowerCase(java.util.Locale.ROOT);
        if (prefix.isEmpty()) {
            return candidates;
        }
        List<String> matches = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(java.util.Locale.ROOT).startsWith(prefix)) {
                matches.add(candidate);
            }
        }
        return matches;
    }
}

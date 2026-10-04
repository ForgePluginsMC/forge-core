package com.forge.core.cmd.permission;

import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

/** Shared helpers for the permission pack. */
final class PermUtil {
    private PermUtil() {
    }

    /** Resolve a player name to a UUID (online first, then offline). */
    static @Nullable UUID uuidOf(CommandSender sender, String name) {
        OfflinePlayer offline = Players.offline(name);
        if (offline == null) {
            var online = Players.findQuiet(name);
            if (online == null) {
                Text.error(sender, "Unknown player: <white>" + Text.escape(name) + "</white>.");
                return null;
            }
            return online.getUniqueId();
        }
        return offline.getUniqueId();
    }

    static String displayName(UUID uuid) {
        OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name == null ? uuid.toString() : name;
    }
}

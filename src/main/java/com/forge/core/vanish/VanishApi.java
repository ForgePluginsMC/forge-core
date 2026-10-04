package com.forge.core.vanish;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

/**
 * Shared vanish state. The moderation pack's vanish implementation writes
 * here; other packs (e.g. /list) read here. Keeps packs decoupled.
 */
public final class VanishApi {
    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();

    private VanishApi() {
    }

    public static void setVanished(Player player, boolean vanished) {
        if (vanished) {
            VANISHED.add(player.getUniqueId());
        } else {
            VANISHED.remove(player.getUniqueId());
        }
    }

    public static boolean isVanished(Player player) {
        return VANISHED.contains(player.getUniqueId());
    }

    public static void clear(Player player) {
        VANISHED.remove(player.getUniqueId());
    }
}

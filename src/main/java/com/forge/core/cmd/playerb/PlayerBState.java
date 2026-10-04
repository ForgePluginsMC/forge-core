package com.forge.core.cmd.playerb;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.ArmorStand;

/**
 * Session-only shared state for the player-B pack. All structures are
 * concurrent; entries are cleaned up on quit by {@link PlayerBListener}.
 */
final class PlayerBState {
    private PlayerBState() {
    }

    /** Tracker UUID -> target UUID for /compass tracking. */
    static final Map<UUID, UUID> compassTracking = new ConcurrentHashMap<>();

    /** Seated player UUID -> invisible armor-stand seat. */
    static final Map<UUID, ArmorStand> seats = new ConcurrentHashMap<>();

    /** Players with a dispose GUI currently open. */
    static final Set<UUID> disposeOpen = ConcurrentHashMap.newKeySet();
}

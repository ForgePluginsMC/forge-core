package com.forge.core.cmd.playera;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Cuffed players cannot move: block-to-block movement is cancelled. */
final class CuffManager implements Listener {
    private final Set<UUID> cuffed = ConcurrentHashMap.newKeySet();

    /** Toggle; returns the new state. */
    boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (cuffed.contains(uuid)) {
            cuffed.remove(uuid);
            return false;
        }
        cuffed.add(uuid);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock() && cuffed.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cuffed.remove(event.getPlayer().getUniqueId());
    }
}

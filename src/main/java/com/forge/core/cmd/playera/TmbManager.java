package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * "Toggle mob targeting": flagged players are ignored by mobs. The flag
 * persists in userdata; the live set is synced on join and cleared on quit.
 */
final class TmbManager implements Listener {
    private final ForgeCore plugin;
    private final Set<UUID> ignoring = ConcurrentHashMap.newKeySet();

    TmbManager(ForgeCore plugin) {
        this.plugin = plugin;
    }

    /** Toggle; returns the new state. */
    boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        boolean on = !ignoring.contains(uuid);
        if (on) {
            ignoring.add(uuid);
        } else {
            ignoring.remove(uuid);
        }
        plugin.users().get(player).setBoolean("tmb", on);
        plugin.users().save(player.getUniqueId());
        return on;
    }

    /** Sync the live set from persisted data (called on join). */
    void sync(Player player) {
        if (plugin.users().get(player).getBoolean("tmb", false)) {
            ignoring.add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && ignoring.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ignoring.remove(event.getPlayer().getUniqueId());
    }
}

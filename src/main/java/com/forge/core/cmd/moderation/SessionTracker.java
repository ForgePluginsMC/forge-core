package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Records first-seen / last-seen timestamps in userdata for /seen,
 * /lastonline, /whowas and /purge. Runs at LOWEST so the core session
 * handler (MONITOR) still performs the actual save/unload afterwards.
 */
final class SessionTracker implements Listener {
    private final ForgeCore plugin;

    SessionTracker(ForgeCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        UserData data = plugin.users().get(event.getPlayer());
        if (data.getLong("first-seen", 0L) == 0L) {
            data.setLong("first-seen", System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        UserData data = plugin.users().get(event.getPlayer());
        data.setLong("last-seen", System.currentTimeMillis());
    }
}

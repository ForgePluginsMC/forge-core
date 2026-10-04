package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Maintenance mode. The flag persists in the config; while enabled,
 * newcomers without a bypass entry are turned away at pre-login and
 * enabling kicks everyone online who lacks
 * {@code forgecore.maintenance.bypass} (online bypass holders are recorded
 * so they can rejoin).
 */
public final class MaintenanceManager implements Listener {
    private final ForgeCore plugin;
    private boolean enabled;
    private final Set<UUID> bypass = ConcurrentHashMap.newKeySet();

    public MaintenanceManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("maintenance", false);
        for (String raw : plugin.getConfig().getStringList("maintenance-bypass")) {
            try {
                bypass.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // Skip bad rows.
            }
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean enabled() {
        return enabled;
    }

    /** Toggle maintenance mode; returns the new state. */
    public boolean toggle() {
        setEnabled(!enabled);
        return enabled;
    }

    private void setEnabled(boolean on) {
        enabled = on;
        plugin.getConfig().set("maintenance", on);
        if (on) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("forgecore.maintenance.bypass")) {
                    bypass.add(player.getUniqueId());
                }
            }
            List<String> rows = new ArrayList<>();
            for (UUID uuid : bypass) {
                rows.add(uuid.toString());
            }
            plugin.getConfig().set("maintenance-bypass", rows);
        }
        plugin.saveConfig();
        if (on) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!bypass.contains(player.getUniqueId())) {
                    player.kick(Text.of("<red>The server is now in maintenance mode.\n<gray>Please come back later."));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!enabled || event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        if (!bypass.contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Text.of("<red>The server is in maintenance mode.\n<gray>Please come back later."));
        }
    }
}

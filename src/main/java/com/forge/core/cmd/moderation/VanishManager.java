package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.vanish.VanishApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Vanish implementation. Hidden players are invisible to everyone without
 * {@code forgecore.vanish.see}; shared state lives in {@link VanishApi} so
 * other packs (e.g. /list) can respect it. No potions involved.
 */
public final class VanishManager implements Listener {
    private final ForgeCore plugin;

    public VanishManager(ForgeCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Toggle vanish for a player; returns the new state. */
    public boolean toggle(Player player) {
        boolean now = !VanishApi.isVanished(player);
        setVanished(player, now);
        return now;
    }

    /** Apply or remove vanish for a player. */
    public void setVanished(Player player, boolean vanished) {
        VanishApi.setVanished(player, vanished);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player)) {
                continue;
            }
            if (vanished) {
                if (!viewer.hasPermission("forgecore.vanish.see")) {
                    viewer.hidePlayer(plugin, player);
                }
            } else {
                viewer.showPlayer(plugin, player);
            }
        }
    }

    /** Hide already-vanished players from newcomers who may not see them. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        if (joiner.hasPermission("forgecore.vanish.see")) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(joiner) && VanishApi.isVanished(online)) {
                joiner.hidePlayer(plugin, online);
            }
        }
    }

    /** Vanish is session-scoped: drop it on quit. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        VanishApi.clear(event.getPlayer());
    }
}

package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Session-based powertools: bind a command to a held item type; left- or
 * right-clicking with that item runs the command. Bindings clear on logout.
 */
public final class PowertoolManager implements Listener {
    /** Player -> (material -> command). */
    private final Map<UUID, Map<Material, String>> bindings = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> enabled = new ConcurrentHashMap<>();
    private final ForgeCore plugin;

    public PowertoolManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Bind a command to a material for a player; empty command removes it. */
    public void bind(Player player, Material material, String command) {
        bindings.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(material, command);
    }

    /** Remove all bindings for a material; returns true if one existed. */
    public boolean unbind(Player player, Material material) {
        Map<Material, String> map = bindings.get(player.getUniqueId());
        return map != null && map.remove(material) != null;
    }

    /** All bindings for a player (empty map when none). */
    public Map<Material, String> bindings(Player player) {
        return bindings.getOrDefault(player.getUniqueId(), Map.of());
    }

    /** Toggle powertools on/off; returns the new state. */
    public boolean toggle(Player player) {
        boolean now = !enabled.getOrDefault(player.getUniqueId(), true);
        enabled.put(player.getUniqueId(), now);
        return now;
    }

    public boolean isEnabled(Player player) {
        return enabled.getOrDefault(player.getUniqueId(), true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isEnabled(player) || event.getItem() == null) {
            return;
        }
        Map<Material, String> map = bindings.get(player.getUniqueId());
        if (map == null) {
            return;
        }
        String command = map.get(event.getItem().getType());
        if (command == null) {
            return;
        }
        event.setCancelled(true);
        plugin.getServer().dispatchCommand(player, command);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bindings.remove(event.getPlayer().getUniqueId());
        enabled.remove(event.getPlayer().getUniqueId());
    }
}

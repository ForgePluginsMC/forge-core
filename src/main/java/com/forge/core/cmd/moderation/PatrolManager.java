package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.vanish.VanishApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Patrol mode: while active, the player is teleported to the next online,
 * non-vanished player every 10 seconds. Session-only.
 */
public final class PatrolManager implements Listener {
    private final ForgeCore plugin;
    private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> cursor = new ConcurrentHashMap<>();

    public PatrolManager(ForgeCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** True while the player has an active patrol task. */
    public boolean active(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    /** Toggle patrol mode; returns the new state. */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
            cursor.remove(uuid);
            return false;
        }
        BukkitTask next = Bukkit.getScheduler().runTaskTimer(plugin, () -> visit(player), 20L, 200L);
        tasks.put(uuid, next);
        cursor.put(uuid, 0);
        return true;
    }

    private void stop(UUID uuid) {
        BukkitTask task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        cursor.remove(uuid);
    }

    private void visit(Player player) {
        if (!player.isOnline()) {
            stop(player.getUniqueId());
            return;
        }
        List<Player> candidates = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !VanishApi.isVanished(online)) {
                candidates.add(online);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int index = cursor.getOrDefault(player.getUniqueId(), 0) % candidates.size();
        cursor.put(player.getUniqueId(), index + 1);
        Player target = candidates.get(index);
        player.teleport(target.getLocation());
        Text.send(player, "<gray>Patrol: <white>" + Text.escape(target.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        stop(event.getPlayer().getUniqueId());
    }
}

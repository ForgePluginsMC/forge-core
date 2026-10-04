package com.forge.core.afk;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * AFK tracking: marks players AFK after {@code afk-seconds} of inactivity,
 * clears on activity. Manual {@code /afk} toggles through here too.
 */
public final class AfkManager implements Listener {
    private final ForgeCore plugin;
    private final Map<UUID, Long> lastActive = new ConcurrentHashMap<>();
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    public AfkManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        long periodTicks = 20L * 10;
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> check(), periodTicks, periodTicks);
    }

    private long afkMillis() {
        return plugin.getConfig().getLong("afk-seconds", 300) * 1000;
    }

    private void check() {
        long now = System.currentTimeMillis();
        long threshold = afkMillis();
        if (threshold <= 0) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (afk.contains(uuid)) {
                continue;
            }
            long last = lastActive.getOrDefault(uuid, now);
            if (now - last > threshold && !player.hasPermission("forgecore.afk.bypass")) {
                setAfk(player, true);
            }
        }
    }

    /** Record activity; clears AFK status. */
    public void setActive(Player player) {
        UUID uuid = player.getUniqueId();
        lastActive.put(uuid, System.currentTimeMillis());
        if (afk.remove(uuid)) {
            Text.send(player, "<gray>You are no longer AFK.");
        }
    }

    public boolean isAfk(Player player) {
        return afk.contains(player.getUniqueId());
    }

    /** Mark or unmark AFK, with optional broadcast. */
    public void setAfk(Player player, boolean isAfk) {
        UUID uuid = player.getUniqueId();
        if (isAfk) {
            if (afk.add(uuid) && plugin.getConfig().getBoolean("afk-broadcast", true)) {
                Text.broadcast("<gray>" + Text.escape(player.getName()) + " is now AFK.");
            }
        } else {
            afk.remove(uuid);
            lastActive.put(uuid, System.currentTimeMillis());
        }
    }

    /** Drop AFK state when a player leaves. */
    public void forget(Player player) {
        UUID uuid = player.getUniqueId();
        afk.remove(uuid);
        lastActive.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || !event.hasChangedBlock()) {
            return;
        }
        setActive(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> setActive(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        setActive(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        setActive(event.getPlayer());
    }
}

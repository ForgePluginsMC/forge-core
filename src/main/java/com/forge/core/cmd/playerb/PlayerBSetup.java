package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Wires player-B session state: managers, listeners and the compass-tracking
 * ticker (action-bar distance updates every second while tracking).
 */
public final class PlayerBSetup {
    private PlayerBSetup() {
    }

    public static void init(ForgeCore plugin) {
        MsgManager.init(plugin);
        CTextManager.init(plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerBListener(plugin), plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> tickCompass(), 20L, 20L);
    }

    private static void tickCompass() {
        Iterator<Map.Entry<UUID, UUID>> iterator = PlayerBState.compassTracking.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            Player tracker = Bukkit.getPlayer(entry.getKey());
            Player target = Bukkit.getPlayer(entry.getValue());
            if (tracker == null || target == null) {
                iterator.remove();
                continue;
            }
            Location from = tracker.getLocation();
            Location to = target.getLocation();
            if (from.getWorld() == null || to.getWorld() == null
                    || !from.getWorld().getUID().equals(to.getWorld().getUID())) {
                String worldName = to.getWorld() == null ? "?" : to.getWorld().getName();
                tracker.sendActionBar(Text.of(
                        "<gold>" + Text.escape(target.getName()) + "</gold> <gray>in " + Text.escape(worldName) + "</gray>"));
                continue;
            }
            tracker.setCompassTarget(to);
            long distance = Math.round(from.distance(to));
            tracker.sendActionBar(Text.of(
                    "<gold>" + Text.escape(target.getName()) + "</gold> <gray>" + distance + "m</gray>"));
        }
    }
}

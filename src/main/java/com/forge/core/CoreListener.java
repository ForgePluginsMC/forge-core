package com.forge.core;

import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Core session handling: load/unload userdata, MOTD, first spawn,
 * and recording locations for /back and /dback.
 */
final class CoreListener implements Listener {
    private final ForgeCore plugin;

    CoreListener(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.users().get(player);
        plugin.afk().setActive(player);

        boolean firstJoin = !player.hasPlayedBefore();
        if (firstJoin) {
            plugin.economy().set(player.getUniqueId(), plugin.economy().startingBalance());
            if (plugin.getConfig().getBoolean("first-spawn.teleport", true)) {
                var spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
                plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(spawn));
            }
        }

        String motd = plugin.getConfig().getString("motd", "");
        if (!motd.isBlank()) {
            player.sendMessage(Text.of(Placeholders.apply(player, motd)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.tpa().forget(player);
        plugin.mutes().forget(player);
        plugin.afk().forget(player);
        plugin.users().unload(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            return;
        }
        plugin.users().get(event.getPlayer()).setLocation("back", event.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        plugin.users().get(event.getEntity()).setLocation("death", event.getEntity().getLocation());
    }
}

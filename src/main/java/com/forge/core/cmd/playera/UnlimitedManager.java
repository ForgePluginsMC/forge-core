package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Unlimited block placement: placed blocks are refunded to the player. */
public final class UnlimitedManager implements Listener {
    private final ForgeCore plugin;
    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

    public UnlimitedManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Toggle; returns the new state. */
    public boolean toggle(UUID uuid) {
        if (!enabled.remove(uuid)) {
            enabled.add(uuid);
            return true;
        }
        return false;
    }

    public boolean isEnabled(UUID uuid) {
        return enabled.contains(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!enabled.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        var hand = event.getHand();
        if (hand == null) {
            return;
        }
        var item = event.getPlayer().getInventory().getItem(hand);
        // Refund the placed block one tick later to avoid inventory desync.
        plugin.getServer().getScheduler().runTask(plugin,
                () -> item.setAmount(item.getAmount() + 1));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        enabled.remove(event.getPlayer().getUniqueId());
    }
}

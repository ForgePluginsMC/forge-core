package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Session cleanup and interactions for the player-B pack: standing up from
 * /sit, clearing dispose GUIs and dropping compass tracking on quit.
 */
final class PlayerBListener implements Listener {
    PlayerBListener(ForgeCore plugin) {
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        SitCommand.standUp(event.getPlayer(), false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            SitCommand.standUp(player, false);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        SitCommand.standUp(event.getPlayer(), false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        SitCommand.standUp(player, false);
        PlayerBState.compassTracking.remove(uuid);
        PlayerBState.compassTracking.values().removeIf(target -> target.equals(uuid));
        PlayerBState.disposeOpen.remove(uuid);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (PlayerBState.disposeOpen.remove(player.getUniqueId())) {
            event.getInventory().clear();
        }
    }
}

package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Session bookkeeping for the player-a pack: session start timestamp,
 * last-known name, and re-applying god mode, nicknames and mob-ignore flags.
 */
final class PlayerAListener implements Listener {
    private final ForgeCore plugin;
    private final TmbManager tmb;

    PlayerAListener(ForgeCore plugin, TmbManager tmb) {
        this.plugin = plugin;
        this.tmb = tmb;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserData data = plugin.users().get(player);
        data.setLong("session-start", System.currentTimeMillis());
        data.setString("last-name", player.getName());
        if (data.god()) {
            player.setInvulnerable(true);
        }
        Nicks.apply(plugin, player);
        tmb.sync(player);
        plugin.users().save(player.getUniqueId());
    }
}

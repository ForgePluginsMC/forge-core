package com.forge.core.cmd.systemsb.tablist;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Applies tab names on join and serves the animated MOTD on server-list pings. */
final class TablistListener implements Listener {
    private final TablistManager manager;

    TablistListener(TablistManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.applyTablistName(event.getPlayer());
        manager.pushCurrentFrame(event.getPlayer());
    }

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        Component motd = manager.currentMotd(event.getNumPlayers(), event.getMaxPlayers());
        if (!motd.equals(Component.empty())) {
            event.motd(motd);
        }
        int max = manager.motdMaxPlayers();
        if (max >= 0) {
            event.setMaxPlayers(max);
        }
    }
}

package com.forge.core.merge.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Routes async chat through the pipeline. Runs at NORMAL so ForgeCore's
 * MuteManager (LOWEST) cancels muted/silenced players first.
 */
public final class ChatListener implements Listener {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final ChatManager manager;

    public ChatListener(ChatManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String text = PLAIN.serialize(event.message());
        ChatChannel channel = manager.channelOf(player);
        ChatPipeline.Result result = manager.pipeline().process(player, channel, text);
        if (result == null) {
            event.setCancelled(true);
            return;
        }
        java.util.Set<Audience> viewers = event.viewers();
        viewers.clear();
        viewers.addAll(result.viewers());
        event.renderer((source, sourceDisplayName, message, viewer) -> result.line().apply(viewer));
        manager.pingPlayers(result.pings());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.playerJoined(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.playerQuit(event.getPlayer());
    }
}

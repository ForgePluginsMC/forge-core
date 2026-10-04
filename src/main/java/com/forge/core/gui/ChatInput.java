package com.forge.core.gui;

import com.forge.core.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Captures the next chat message from a player as GUI text input.
 *
 * <p>Used by GUIs for commands that need arguments: close the GUI, prompt in
 * chat, intercept the next message, then run the follow-up.
 */
@NullMarked
public final class ChatInput implements Listener {
    private static final Map<UUID, PendingInput> PENDING = new ConcurrentHashMap<>();
    private static @Nullable Plugin plugin;

    private record PendingInput(Consumer<String> callback, long expiresAt) {
    }

    private ChatInput() {
    }

    /** Register the listener. Called once from ForgeCore.onEnable. */
    public static void init(Plugin plugin) {
        ChatInput.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new ChatInput(), plugin);
        // Expire stale requests every 30s.
        Bukkit.getScheduler().runTaskTimer(plugin, () ->
                PENDING.entrySet().removeIf(e -> System.currentTimeMillis() > e.getValue().expiresAt()),
                600L, 600L);
    }

    /** Request the player's next chat message. 60s timeout. */
    public static void request(Player player, Consumer<String> callback) {
        PENDING.put(player.getUniqueId(),
                new PendingInput(callback, System.currentTimeMillis() + 60_000L));
    }

    /** Cancel a pending request (e.g. player typed 'cancel' is handled by caller). */
    public static void cancel(Player player) {
        PENDING.remove(player.getUniqueId());
    }

    /** True if the player has a pending chat input request. */
    public static boolean isPending(Player player) {
        return PENDING.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PendingInput pending = PENDING.remove(player.getUniqueId());
        if (pending == null) {
            return;
        }
        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        Plugin p = plugin;
        if (p == null) {
            return;
        }
        // Run the callback on the main thread.
        Bukkit.getScheduler().runTask(p, () -> {
            try {
                pending.callback().accept(message);
            } catch (Exception e) {
                p.getLogger().warning("Chat input callback failed: " + e);
                Text.send(player, "<red>Something went wrong.");
            }
        });
    }
}

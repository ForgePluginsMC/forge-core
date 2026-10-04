package com.forge.core.help;

import java.util.Arrays;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Intercepts {@code /help forgecore ...} before vanilla help sees it, so
 * players get ForgeCore's clickable paginated help. Everything else passes
 * through to vanilla {@code /help} untouched.
 */
final class HelpListener implements Listener {
    private final HelpManager manager;

    HelpListener(HelpManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("/")) {
            return;
        }
        String[] parts = message.substring(1).split("\\s+");
        if (parts.length >= 2
                && parts[0].equalsIgnoreCase("help")
                && parts[1].equalsIgnoreCase("forgecore")) {
            event.setCancelled(true);
            manager.showHelp(event.getPlayer(), Arrays.copyOfRange(parts, 2, parts.length));
        }
    }
}

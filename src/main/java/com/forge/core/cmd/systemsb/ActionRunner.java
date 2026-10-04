package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Runs declarative action lines shared by the rank and schedule systems.
 *
 * <p>Supported actions:
 * <ul>
 *   <li>{@code command:<cmd>} — dispatched as console; {@code %player%} is replaced.</li>
 *   <li>{@code money:<amount>} — adds money to the player's balance.</li>
 *   <li>{@code msg:<MiniMessage>} — sends the player a message.</li>
 *   <li>{@code broadcast:<MiniMessage>} — broadcasts to the whole server.</li>
 * </ul>
 */
public final class ActionRunner {
    private ActionRunner() {
    }

    /** Run actions for a player; player may be null for console-style runs. */
    public static void run(ForgeCore plugin, @Nullable Player player, List<String> actions) {
        String playerName = player == null ? "" : player.getName();
        for (String raw : actions) {
            String action = raw.trim();
            if (action.regionMatches(true, 0, "command:", 0, 8)) {
                String command = action.substring(8).replace("%player%", playerName).trim();
                if (!command.isEmpty()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            } else if (action.regionMatches(true, 0, "money:", 0, 6)) {
                if (player != null) {
                    try {
                        plugin.economy().add(player.getUniqueId(), Double.parseDouble(action.substring(6).trim()));
                    } catch (NumberFormatException ignored) {
                        plugin.getLogger().warning("Bad money action: " + raw);
                    }
                }
            } else if (action.regionMatches(true, 0, "msg:", 0, 4)) {
                if (player != null) {
                    player.sendMessage(Text.of(Placeholders.apply(player, action.substring(4).trim())));
                }
            } else if (action.regionMatches(true, 0, "broadcast:", 0, 10)) {
                Text.broadcast(Placeholders.apply(player, action.substring(10).trim()));
            }
        }
    }
}

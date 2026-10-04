package com.forge.core.util;

import com.forge.core.ForgeCore;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Placeholder expansion. Uses PlaceholderAPI when it is installed
 * (soft-depend); otherwise applies ForgeCore's built-in placeholders.
 *
 * <p>Built-ins: {@code %player_name%}, {@code %player_uuid%},
 * {@code %forgecore_balance%}, {@code %forgecore_playtime%},
 * {@code %forgecore_nick%}, {@code %forgecore_rank%} (empty unless the
 * rank system set one).
 */
public final class Placeholders {
    private static @Nullable Method papiMethod;

    static {
        try {
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiMethod = papi.getMethod("setPlaceholders", Player.class, String.class);
        } catch (ReflectiveOperationException exception) {
            papiMethod = null;
        }
    }

    private Placeholders() {
    }

    /** Expand placeholders in text for the given player. */
    public static String apply(@Nullable Player player, String text) {
        String out = text;
        Method method = papiMethod;
        if (method != null && player != null) {
            try {
                out = (String) method.invoke(null, player, out);
            } catch (ReflectiveOperationException exception) {
                // Fall through to built-ins.
            }
        }
        if (player != null) {
            ForgeCore plugin = ForgeCore.get();
            out = out.replace("%player_name%", player.getName())
                    .replace("%player_uuid%", player.getUniqueId().toString())
                    .replace("%forgecore_balance%", plugin.economy().format(plugin.economy().get(player.getUniqueId())))
                    .replace("%forgecore_playtime%", Time.format(plugin.users().get(player).playtimeSeconds()))
                    .replace("%forgecore_nick%", plugin.users().get(player).nickOrName(player))
                    .replace("%forgecore_rank%", plugin.users().get(player).getString("rank", ""));
        }
        return out;
    }
}

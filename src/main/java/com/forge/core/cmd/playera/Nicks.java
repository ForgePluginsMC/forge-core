package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Applies stored MiniMessage nicknames to display name and tab list. */
public final class Nicks {
    private Nicks() {
    }

    /** Apply a player's stored nick (or clear back to their real name). */
    public static void apply(ForgeCore plugin, Player player) {
        applyRaw(player, plugin.users().get(player).getString("nick", null));
    }

    /** Apply a raw MiniMessage nick; null clears it. */
    static void applyRaw(Player player, @Nullable String nick) {
        if (nick == null) {
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
            return;
        }
        player.displayName(Text.of(nick));
        String plain = Text.strip(nick);
        if (plain.length() > 16) {
            plain = plain.substring(0, 16);
        }
        player.playerListName(Component.text(plain));
    }
}

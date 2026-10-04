package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.SystemsBSetup;
import com.forge.core.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Applies stored MiniMessage nicknames to the display name. The tab-list
 * entry itself is owned by the tablist system (group prefix/suffix wrapped
 * around the nick), refreshed here for immediacy.
 */
public final class Nicks {
    private Nicks() {
    }

    /** Apply a player's stored nick (or clear back to their real name). */
    public static void apply(ForgeCore plugin, Player player) {
        applyRaw(plugin, player, plugin.users().get(player).getString("nick", null));
    }

    /** Apply a raw MiniMessage nick; null clears it. */
    public static void applyRaw(ForgeCore plugin, Player player, @Nullable String nick) {
        if (nick == null) {
            player.displayName(Component.text(player.getName()));
        } else {
            player.displayName(Text.of(nick));
        }
        SystemsBSetup.tablist().applyTablistName(player);
    }
}

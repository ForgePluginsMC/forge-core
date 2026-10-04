package com.forge.core.cmd.moderation;

import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Audience helper for staff-only messaging (/staffmsg, /helpop, punish alerts). */
final class Staff {
    private Staff() {
    }

    /** Online players holding the staff permission. */
    static List<Player> audience() {
        List<Player> out = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("forgecore.staff")) {
                out.add(player);
            }
        }
        return out;
    }

    /** Send a MiniMessage line to every staff member and the console. */
    static void notify(String miniMessage) {
        Component message = Text.of(miniMessage);
        for (Player player : audience()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }
}

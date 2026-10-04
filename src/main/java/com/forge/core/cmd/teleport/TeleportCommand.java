package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Shared helpers for teleport-pack commands: teleporting with an AFK reset,
 * player-required guards and clean failures.
 */
abstract class TeleportCommand extends ForgeCommand {
    TeleportCommand(ForgeCore plugin) {
        super(plugin);
    }

    /** Teleport a player, reset their AFK timer, and send an optional note. */
    protected void teleport(Player player, Location location, String message) {
        player.teleport(location);
        plugin.afk().setActive(player);
        if (message != null) {
            Text.send(player, message);
        }
    }

    /** Abort with a clean user-facing error. */
    protected CommandRegistry.CommandFailure fail(String message) {
        return new CommandRegistry.CommandFailure(message);
    }

    /** Sender as a Player, or a clean failure when it is not one. */
    protected Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        throw fail("Only players can use that command.");
    }
}

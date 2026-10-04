package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /dback — return to where you died. */
public final class DbackCommand extends TeleportCommand {
    public DbackCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dback";
    }

    @Override
    public String description() {
        return "Teleport back to your death location.";
    }

    @Override
    public String usage() {
        return "/dback";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        Location death = plugin.users().get(player).getLocation("death");
        if (death == null) {
            throw fail("No death location recorded yet.");
        }
        teleport(player, death, "<gray>Returned to where you died.");
    }
}

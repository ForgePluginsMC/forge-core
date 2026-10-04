package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /back — return to where you were before your last teleport. */
public final class BackCommand extends TeleportCommand {
    public BackCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "back";
    }

    @Override
    public String description() {
        return "Teleport back to your previous location.";
    }

    @Override
    public String usage() {
        return "/back";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        Location back = plugin.users().get(player).getLocation("back");
        if (back == null) {
            throw fail("You have nowhere to go back to yet.");
        }
        teleport(player, back, "<gray>Returned to your previous location.");
    }
}

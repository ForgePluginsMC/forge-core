package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /setspawn — set the current world's spawn to your location. */
public final class SetSpawnCommand extends TeleportCommand {
    public SetSpawnCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setspawn";
    }

    @Override
    public String description() {
        return "Set this world's spawn to your location.";
    }

    @Override
    public String usage() {
        return "/setspawn";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        player.getWorld().setSpawnLocation(player.getLocation());
        Text.ok(sender, "Spawn set to your location in <white>" + Text.escape(player.getWorld().getName()) + "</white>.");
    }
}

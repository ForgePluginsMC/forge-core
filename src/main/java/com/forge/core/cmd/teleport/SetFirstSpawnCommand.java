package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /setfirstspawn — set the first-join spawn location in the config. */
public final class SetFirstSpawnCommand extends TeleportCommand {
    public SetFirstSpawnCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setfirstspawn";
    }

    @Override
    public String description() {
        return "Set the first-join spawn location.";
    }

    @Override
    public String usage() {
        return "/setfirstspawn";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        plugin.getConfig().set("first-spawn.location", Locs.serialize(player.getLocation()));
        plugin.saveConfig();
        Text.ok(sender, "First-join spawn set to your location.");
    }
}

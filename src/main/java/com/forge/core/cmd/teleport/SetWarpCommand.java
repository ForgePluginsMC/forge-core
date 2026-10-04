package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /setwarp <name> — create a server warp at your location. */
public final class SetWarpCommand extends TeleportCommand {
    public SetWarpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setwarp";
    }

    @Override
    public String description() {
        return "Create a server warp at your location.";
    }

    @Override
    public String usage() {
        return "/setwarp <name>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        plugin.warps().set(args[0], player.getLocation());
        Text.ok(sender, "Warp <white>" + Text.escape(args[0]) + "</white> set.");
    }
}
